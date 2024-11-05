package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class RevokeCaseAccessHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final RoleAssignmentService roleAssignmentService;

    public RevokeCaseAccessHandler(RoleAssignmentService roleAssignmentService) {
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REVOKE_CASE_ACCESS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        String caseId = String.valueOf(callback.getCaseDetails().getId());
        Optional<String> userIdToRevokeAccessFrom
            = asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, String.class);

        log.info("Revoke case roles for the appeal with case ID {} and userId {}", caseId, userIdToRevokeAccessFrom);

        if (userIdToRevokeAccessFrom.isEmpty()) {
            response.addError("User ID is required to revoke case access");
            return response;
        }

        RoleAssignmentResource roleAssignmentResource = getRoleAssignmentsForUser(caseId);
        log.info("Found '{}' '[CREATOR]' and '[LEGALREPRESENTATIVE]' case roles in the appeal with case ID {}",
                roleAssignmentResource.getRoleAssignmentResponse().size(), caseId);

        if (roleAssignmentResource.getRoleAssignmentResponse().isEmpty()) {
            response.addError("User ID doesn't have access to case: " + userIdToRevokeAccessFrom
                    + " caseId:" + caseId);
        } else {

            deleteRoleAssignment(roleAssignmentResource, userIdToRevokeAccessFrom.get(), caseId);
        }

        return response;
    }

    private RoleAssignmentResource getRoleAssignmentsForUser(String caseId) {
        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CREATOR, RoleName.LEGAL_REPRESENTATIVE))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(caseId)
            )).build();

        log.info("Query role assignment with the parameters: {}, for case reference: {}",
                queryRequest, caseId);

        return roleAssignmentService.queryRoleAssignments(queryRequest);
    }

    private void deleteRoleAssignment(RoleAssignmentResource roleAssignmentResource, String userId, String caseId) {

        roleAssignmentResource.getRoleAssignmentResponse().forEach(roleAssignment ->
            log.info("Role Assignment: Role assigned - {}, IDAM User ID - {}, IDAM User ID Type - {}, ",
                roleAssignment.getRoleName(),
                roleAssignment.getActorId(),
                roleAssignment.getActorIdType())
        );

        Optional<Assignment> roleAssignment = roleAssignmentResource.getRoleAssignmentResponse().stream()
                .filter(ra -> ra.getActorId().equals(userId)).findFirst();

        if (roleAssignment.isPresent()) {
            log.info("Revoking {}'s access to appeal with case ID {}",
                    roleAssignment.get().getRoleName().equals(RoleName.CREATOR) ? "Appellant" : "Legal Representative",
                    caseId);

            roleAssignmentService.deleteRoleAssignment(roleAssignment.get().getId());

            log.info("Successfully revoked User's access to appeal with case ID {}", caseId);
        } else {
            log.error("Problem revoking User's access to appeal with case ID {}. Role assignment for appellant not found", caseId);
        }
    }
}
