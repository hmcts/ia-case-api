package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class EndAppealConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public static final String HEARING_CANCEL_SUCCEED = "#### What happens next\n\n"
        + "A notification has been sent to all parties.<br><br>"
        + "Any hearings requested or listed in List Assist have been automatically cancelled.";

    public static final String HEARING_CANCEL_FAILED = "#### What happens next\n\n"
        + "A notification has been sent to all parties.<br><br>"
        + "The hearing could not be automatically cancelled.<br><br>"
        + "[Cancel the hearing on the Hearings tab](/cases/case-details/%s/hearings)";

    public static final String NOTIFICATION_FAILED = "![Respondent notification failed confirmation]"
        + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)\n"
        + "#### Do this next\n\n"
        + "Contact the respondent to tell them what has changed, including any action they need to take.\n";

    private final RoleAssignmentService roleAssignmentService;
    private final UserDetailsProvider userDetailsProvider;

    public EndAppealConfirmation(RoleAssignmentService roleAssignmentService, UserDetailsProvider userDetailsProvider) {
        this.roleAssignmentService = roleAssignmentService;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.END_APPEAL;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String hoEndAppealInstructStatus =
            asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, String.class).orElse("");

        removeCaseManagerRole(callback);

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (hoEndAppealInstructStatus.equalsIgnoreCase("FAIL")) {
            postSubmitResponse.setConfirmationBody(NOTIFICATION_FAILED);
        } else {
            populateConfirmationPage(callback, postSubmitResponse, asylumCase);
        }

        return postSubmitResponse;
    }

    private static void populateConfirmationPage(Callback<AsylumCase> callback, PostSubmitCallbackResponse postSubmitResponse,
                                  AsylumCase asylumCase) {
        postSubmitResponse.setConfirmationHeader("# You have ended the appeal");

        if (asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED).isPresent()) {
            postSubmitResponse.setConfirmationBody(
                String.format(HEARING_CANCEL_FAILED, callback.getCaseDetails().getId())
            );
        } else {
            postSubmitResponse.setConfirmationBody(HEARING_CANCEL_SUCCEED);
        }
    }

    private void removeCaseManagerRole(Callback<AsylumCase> callback) {
        List<String> userRoles = userDetailsProvider.getUserDetails().getRoles();
        List<String> rolesForRemoval = List.of(UserRole.CASE_OFFICER.name(),
            UserRole.ADMIN_OFFICER.name(),
            UserRole.JUDGE.name(),
            UserRole.JUDICIARY.name());
        if (userRoles.stream().noneMatch(rolesForRemoval::contains)) {
            log.info("User is not a Case Officer, Admin Officer or Judicial user. Case Manager role was not removed.");
            return;
        }

        String caseId = String.valueOf(callback.getCaseDetails().getId());
        QueryRequest queryRequest = QueryRequest.builder()
                .roleType(List.of(RoleType.CASE))
                .roleName(List.of(RoleName.CASE_MANAGER))
                .roleCategory(List.of(
                    RoleCategory.LEGAL_OPERATIONS,
                    RoleCategory.ADMIN,
                    RoleCategory.JUDICIAL))
                .attributes(Map.of(
                    Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                    Attributes.CASE_TYPE, List.of("Asylum"),
                    Attributes.CASE_ID, List.of(caseId)
                ))
                .build();
        log.debug("Query role assignment with the parameters: {}", queryRequest);

        RoleAssignmentResource roleAssignmentResource = roleAssignmentService.queryRoleAssignments(queryRequest);
        Optional<Assignment> roleAssignment = roleAssignmentResource.getRoleAssignmentResponse().stream().findFirst();

        if (roleAssignment.isPresent()) {
            String actorId = roleAssignment.get().getActorId();
            log.info("Removing Case Manager role from user {} for case ID {}", actorId, caseId);

            roleAssignmentService.deleteRoleAssignment(roleAssignment.get().getId());

            log.info("Successfully removed Case Manager role from user {} for case ID {}", actorId, caseId);
        } else {
            log.error("Problem removing Case Manager role for case ID {}. Role assignment for user not found", caseId);
        }
    }
}
