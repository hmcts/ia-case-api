package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class RevokeCaseAccessHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final RoleAssignmentService roleAssignmentService;
    private final CcdCaseAssignment ccdCaseAssignment;

    public RevokeCaseAccessHandler(RoleAssignmentService roleAssignmentService,
                                   CcdCaseAssignment ccdCaseAssignment) {
        this.roleAssignmentService = roleAssignmentService;
        this.ccdCaseAssignment = ccdCaseAssignment;
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

        long caseId = callback.getCaseDetails().getId();

        String userIdToRevokeAccessFrom = asylumCase.read(
                AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        "Legal Representative IDAM user ID is not present."));

        String legalRepOrgId = asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ORG_ID, String.class)
                .orElseThrow(() -> new IllegalStateException("Legal Representative Organisation ID is not present."));

        RoleAssignmentResource roleAssignmentResource = getRoleAssignmentsForUser(caseId, userIdToRevokeAccessFrom);
        log.info("Found '{}' '[CREATOR]' and '[LEGALREPRESENTATIVE]' case roles in the appeal with case ID {}",
                roleAssignmentResource.getRoleAssignmentResponse().size(), caseId);

        if (roleAssignmentResource.getRoleAssignmentResponse().isEmpty()) {
            response.addError("Legal representative doesn't have access to case: " + userIdToRevokeAccessFrom
                    + " caseId: " + caseId);
        } else {
            deleteCaseRoleAssignments(caseId, roleAssignmentResource, userIdToRevokeAccessFrom, legalRepOrgId);
        }

        return response;
    }

    private RoleAssignmentResource getRoleAssignmentsForUser(long caseId, String idamUserId) {
        QueryRequest queryRequest = QueryRequest.builder()
                .roleType(List.of(RoleType.CASE))
                .roleCategory(List.of(RoleCategory.PROFESSIONAL))
                .roleName(List.of(RoleName.CREATOR, RoleName.LEGAL_REPRESENTATIVE))
                .actorId(List.of(idamUserId))
                .attributes(Map.of(
                        Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                        Attributes.CASE_TYPE, List.of("Asylum"),
                        Attributes.CASE_ID, List.of(String.valueOf(caseId))
                )).build();

        log.info("Query role assignment with the parameters: {}, for case reference: {}", queryRequest, caseId);

        return roleAssignmentService.queryRoleAssignments(queryRequest);
    }

    private void deleteCaseRoleAssignments(
            long caseId,
            RoleAssignmentResource roleAssignmentResource,
            String userIdToRevokeAccessFrom,
            String organisationId
    ) {
        roleAssignmentResource.getRoleAssignmentResponse().forEach(roleAssignment -> {
            // TODO delete log
            log.info("Role Assignment: Role assignment ID - {}, Role assigned - {}, IDAM User ID - {}, "
                            + "IDAM User ID Type - {}, ",
                    roleAssignment.getId(),
                    roleAssignment.getRoleName(),
                    roleAssignment.getActorId(),
                    roleAssignment.getActorIdType());

            if (roleAssignment.getRoleCategory() == RoleCategory.PROFESSIONAL) {

                ccdCaseAssignment.revokeLegalRepAccessToCase(caseId, userIdToRevokeAccessFrom, organisationId);

                log.info("Successfully revoked Legal representative's access to appeal with case ID {},"
                        + " role assignment ID {}", caseId, roleAssignment.getId());
            }
        });
    }

}