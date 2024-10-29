package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.*;

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
public class RemoveAppealFromOnlineConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final RoleAssignmentService roleAssignmentService;
    private final UserDetailsProvider userDetailsProvider;

    public RemoveAppealFromOnlineConfirmation(RoleAssignmentService roleAssignmentService, UserDetailsProvider userDetailsProvider) {
        this.roleAssignmentService = roleAssignmentService;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {

        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        removeCaseManagerRole(callback);

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've removed this appeal from the online service");
        postSubmitResponse.setConfirmationBody(
            "## Do this next\n"
            + "You now need to:</br>"
            + "1.Contact the appellant and the respondent to inform them that the case will proceed offline.</br>"
            + "2.Save all files associated with the appeal to the shared drive.</br>"
            + "3.Email a link to the saved files with the appeal reference number to: BAUArnhemHouse@justice.gov.uk"
        );

        return postSubmitResponse;
    }

    private void removeCaseManagerRole(Callback<AsylumCase> callback) {
        List<String> userRoles = userDetailsProvider.getUserDetails().getRoles();
        List<String> rolesForRemoval = List.of(UserRole.CASE_OFFICER.name());
        if (userRoles.stream().noneMatch(rolesForRemoval::contains)) {
            log.info("User is not a Case Officer. Case Manager role was not removed.");
            return;
        }

        String caseId = String.valueOf(callback.getCaseDetails().getId());
        QueryRequest queryRequest = QueryRequest.builder()
                .roleType(List.of(RoleType.CASE))
                .roleName(List.of(RoleName.CASE_MANAGER))
                .roleCategory(List.of(RoleCategory.LEGAL_OPERATIONS))
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
