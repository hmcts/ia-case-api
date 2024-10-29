package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
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
public class SendDecisionAndReasonsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {
    private final RoleAssignmentService roleAssignmentService;
    private final UserDetailsProvider userDetailsProvider;

    public SendDecisionAndReasonsConfirmation(RoleAssignmentService roleAssignmentService, UserDetailsProvider userDetailsProvider) {
        this.roleAssignmentService = roleAssignmentService;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SEND_DECISION_AND_REASONS;
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        removeCaseManagerRole(callback);

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've uploaded the Decision and Reasons document");
        postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                        + "Both parties have been notified of the decision. They'll also be able to access the Decision and Reasons document from the Documents tab."
        );

        return postSubmitResponse;
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
