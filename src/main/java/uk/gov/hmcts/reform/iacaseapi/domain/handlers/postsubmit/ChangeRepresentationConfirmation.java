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
                || callback.getEvent() == Event.NOC_REQUEST
                || callback.getEvent() == Event.APPELLANT_IN_PERSON_MANUAL
                || callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED);
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
            if (!isAipManualEvent(callback.getEvent()) && !isMarkAppealAsDetainedEvent(callback.getEvent())) {
                ccdCaseAssignment.applyNoc(callback);
            }

            if (shouldRevokeAppellantAccess(callback.getEvent(), callback.getCaseDetails().getCaseData())
                    || isAipManualEvent(callback.getEvent()) || isMarkAppealAsDetainedEvent(callback.getEvent())) {
                revokeAppellantAccessToCase(String.valueOf(callback.getCaseDetails().getId()));
            }

            if (!isAipManualEvent(callback.getEvent()) && !isMarkAppealAsDetainedEvent(callback.getEvent())) {
                postNotificationSender.send(callback);
            }

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
            } else if (callback.getEvent() == Event.APPELLANT_IN_PERSON_MANUAL) {
                postSubmitResponse.setConfirmationHeader(
                        "# You have updated this case to Appellant in Person - Manual");
                postSubmitResponse.setConfirmationBody(
                        "#### What happens next\n\n"
                                + "This appeal will have to be continued by internal users\n\n"
                );
            } else if (callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED) {
                postSubmitResponse.setConfirmationHeader(
                        "# You have marked the appeal as detained");
                postSubmitResponse.setConfirmationBody(
                        "#### What happens next\n\nAll parties will be notified that the appeal has been marked an detained.\n\n"
                                + "You should update the hearing center, if necessary."
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
            if (isAipManualEvent(callback.getEvent()) || isMarkAppealAsDetainedEvent(callback.getEvent())) {
                log.error("Revoking Appellant's access to appeal with case id {} failed. Cause: {}",
                        callback.getCaseDetails().getId(), e);
                postSubmitResponse.setConfirmationBody(
                        "### Something went wrong\n\n"
                                + "The appellant's case access has not been revoked for this appeal.\n\n"
                );
            } else {
                if (shouldRevokeAppellantAccess(callback.getEvent(), callback.getCaseDetails().getCaseData())
                        || isAipManualEvent(callback.getEvent())) {
                    log.error("Revoking Appellant's access to appeal with case id {} failed. Cause: {}",
                            callback.getCaseDetails().getId(), e);
                } else {
                    log.error("Unable to change representation (apply noc) for case id {}. Cause: {}",
                            callback.getCaseDetails().getId(), e);
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

    private boolean shouldRevokeAppellantAccess(Event event, AsylumCase asylumCase) {
        return event == Event.NOC_REQUEST && HandlerUtils.isAipToRepJourney(asylumCase);
    }

    private boolean isAipManualEvent(Event event) {
        return event == Event.APPELLANT_IN_PERSON_MANUAL;
    }

    private boolean isMarkAppealAsDetainedEvent(Event event) {
        return event == Event.MARK_APPEAL_AS_DETAINED;
    }
}
