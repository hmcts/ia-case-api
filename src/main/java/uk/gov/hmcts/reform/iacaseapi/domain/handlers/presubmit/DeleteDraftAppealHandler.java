package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@Component
@Slf4j
public class DeleteDraftAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final RoleAssignmentService roleAssignmentService;
    private final IdamService idamService;
    private final UserDetailsProvider userDetailsProvider;

    public DeleteDraftAppealHandler(RoleAssignmentService roleAssignmentService,
                                    IdamService idamService,
                                    UserDetailsProvider userDetailsProvider) {
        this.roleAssignmentService = roleAssignmentService;
        this.idamService = idamService;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.DELETE_DRAFT_APPEAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String idamId = userDetailsProvider.getUserDetails().getId();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        long caseId = callback.getCaseDetails().getId();

        RoleAssignmentResource roleAssignmentResource = roleAssignmentService.getCaseRoleAssignmentsForUser(
            caseId, idamId);

        log.info("Found '{}' '[CREATOR]' and '[LEGALREPRESENTATIVE]' case roles in the appeal with case ID {}",
            roleAssignmentResource.getRoleAssignmentResponse().size(), caseId);

        if (roleAssignmentResource.getRoleAssignmentResponse().isEmpty()) {
            response.addError("User doesn't have access to case: " + idamId
                + " caseId: " + caseId);
        } else {
            deleteRoleAssignment(roleAssignmentResource);
        }

        return response;
    }

    private void deleteRoleAssignment(RoleAssignmentResource roleAssignmentResource) {
        Assignment roleAssignment = roleAssignmentResource.getRoleAssignmentResponse().get(0);
        if (roleAssignment.getRoleCategory() == RoleCategory.CITIZEN) {
            log.info("Revoking appellant's access to appeal with role assignment ID {}", roleAssignment.getId());
            roleAssignmentService.deleteRoleAssignment(roleAssignment.getId(), idamService.getServiceUserToken());
            log.info("Successfully revoked appellant's access to appeal with role assignment ID {}", roleAssignment.getId());
        }
    }
}