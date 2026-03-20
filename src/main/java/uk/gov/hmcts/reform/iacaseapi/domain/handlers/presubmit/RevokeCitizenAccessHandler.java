package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@Component
@Slf4j
public class RevokeCitizenAccessHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final RoleAssignmentService roleAssignmentService;
    private final IdamService idamService;

    public RevokeCitizenAccessHandler(RoleAssignmentService roleAssignmentService,
                                      IdamService idamService) {
        this.roleAssignmentService = roleAssignmentService;
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REVOKE_CITIZEN_ACCESS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        DynamicList revokeAccessDl = asylumCase.read(
                AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException(
                "Dynamic list of users to revoke access from is not present."));
        String idamId = revokeAccessDl.getValue().getCode().split(":")[0];

        long caseId = callback.getCaseDetails().getId();

        RoleAssignmentResource roleAssignmentResource = roleAssignmentService.getCaseRoleAssignmentsForUser(
            caseId, idamId);

        if (roleAssignmentResource.getRoleAssignmentResponse().isEmpty()) {
            return new PreSubmitCallbackResponse<>(asylumCase)
            .withError("User doesn't have access to case idamId: " + idamId
                + " caseId: " + caseId);
        }

        deleteRoleAssignment(roleAssignmentResource.getRoleAssignmentResponse().get(0).getId());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void deleteRoleAssignment(String roleAssignmentId) {
        log.info("Revoking appellant's access to appeal with role assignment ID {}", roleAssignmentId);
        roleAssignmentService.deleteRoleAssignment(roleAssignmentId, idamService.getServiceUserToken());
        log.info("Successfully revoked appellant's access to appeal with role assignment ID {}", roleAssignmentId);
    }

}