package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;

@Slf4j
@Component
public class ChangeRepresentationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final RoleAssignmentService roleAssignmentService;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final PostNotificationSender<AsylumCase> postNotificationSender;

    public ChangeRepresentationConfirmation(
        CcdCaseAssignment ccdCaseAssignment,
        PostNotificationSender<AsylumCase> postNotificationSender,
        RoleAssignmentService roleAssignmentService
    ) {

        this.ccdCaseAssignment = ccdCaseAssignment;
        this.postNotificationSender = postNotificationSender;
        this.roleAssignmentService = roleAssignmentService;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return (callback.getEvent() == Event.REMOVE_REPRESENTATION
                || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE
                || callback.getEvent() == Event.NOC_REQUEST);
    }

    /**
     * the confirmation message and the error message are coming from ExUI and cannot be customised.
     */
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        try {
            ccdCaseAssignment.applyNoc(callback);

            if (HandlerUtils.isAipToRepJourney(callback.getCaseDetails().getCaseData())) {
                revokeAppellantAccessToCase(String.valueOf(callback.getCaseDetails().getId()));
            }

            postNotificationSender.send(callback);

            if (callback.getEvent() == Event.REMOVE_REPRESENTATION) {
                postSubmitResponse.setConfirmationHeader(
                    "# You have stopped representing this client"
                );
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "We've sent you an email confirming you're no longer representing this client.\n"
                    + "You have been removed from this case and no longer have access to it.\n\n"
                    + "[View case list](/cases)"
                );
            } else if (callback.getEvent() == Event.NOC_REQUEST) {
                postSubmitResponse.setConfirmationHeader(
                    "# You have started representing this client"
                );
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "All parties will be notified."
                );
            } else {
                postSubmitResponse.setConfirmationHeader(
                    "# You have removed the legal representative from this appeal"
                );
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "All parties will be notified."
                );
            }
        } catch (Exception e) {
            log.error("Unable to change representation (apply noc) for case id {} with error message: {}",
                callback.getCaseDetails().getId(), e.getMessage());

            if (HandlerUtils.isAipToRepJourney(callback.getCaseDetails().getCaseData())) {
                log.error("Revoking Appellant's access to appeal with case id {} failed with error message: {}",
                    callback.getCaseDetails().getId(), e.getMessage());
            }

            postSubmitResponse.setConfirmationBody(
                "### Something went wrong\n\n"
                + "You have not stopped representing the appellant in this appeal.\n\n"
                + "Use the [stop representing a client](/case/IA/Asylum/"
                + callback.getCaseDetails().getId()
                + "/trigger/removeRepresentation/removeRepresentationSingleFormPageWithComplex) feature to try again."
            );
        }

        return postSubmitResponse;
    }

    private void revokeAppellantAccessToCase(String caseId) {
        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CREATOR))
            .roleCategory(List.of(RoleCategory.CITIZEN))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(caseId)
            ))
            .build();

        log.debug("Query role assignment with the parameters: {}", queryRequest);

        RoleAssignmentResource roleAssignmentResource = roleAssignmentService
            .queryRoleAssignments(queryRequest);
        log.debug("Found {} Citizen roles in the appeal with case ID {}", roleAssignmentResource.getRoleAssignmentResponse().size(), caseId);

        Optional<Assignment> roleAssignment = roleAssignmentResource.getRoleAssignmentResponse().stream().findFirst();
        if (roleAssignment.isPresent()) {
            log.info("Revoking Appellant's access to appeal with case ID {}", caseId);

            roleAssignmentService.deleteRoleAssignment(roleAssignment.get().getId());

            log.info("Successfully revoked Appellant's access to appeal with case ID {}", caseId);
        } else {
            log.error("Problem revoking Appellant's access to appeal with case ID {}. Role assignment for appellant not found", caseId);
        }
    }
}
