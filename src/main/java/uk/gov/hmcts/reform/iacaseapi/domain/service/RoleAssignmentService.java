package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Jurisdiction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RequestedRoles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

@Component
@Slf4j
public class RoleAssignmentService {
    public static final String ROLE_NAME = "tribunal-caseworker";
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private final RoleAssignmentApi roleAssignmentApi;
    private final IdamService idamService;

    public RoleAssignmentService(AuthTokenGenerator serviceAuthTokenGenerator,
                                 RoleAssignmentApi roleAssignmentApi,
                                 UserDetails userDetails, IdamService idamService) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.roleAssignmentApi = roleAssignmentApi;
        this.userDetails = userDetails;
        this.idamService = idamService;
    }

    public void assignRole(long caseDetailsId, String assigneeId) {
        String accessToken = userDetails.getAccessToken();
        String currentUserIdamId = userDetails.getId();
        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        RoleAssignment body =
            getRoleAssignment(caseDetailsId, assigneeId, currentUserIdamId);

        roleAssignmentApi.assignRole(accessToken, serviceAuthorizationToken, body);
    }

    public RoleAssignment getRoleAssignment(long caseDetailsId, String assigneeId, String currentUserIdamId) {

        Map<String, String> attributes = new HashMap<>();
        attributes.put("caseId", Long.toString(caseDetailsId));

        return new RoleAssignment(
            new RoleRequest(
                currentUserIdamId,
                "case-allocation",
                caseDetailsId + "/" + ROLE_NAME,
                true
            ),
            singletonList(new RequestedRoles(
                ActorIdType.IDAM,
                assigneeId,
                RoleType.CASE,
                ROLE_NAME,
                RoleCategory.LEGAL_OPERATIONS,
                Classification.RESTRICTED,
                GrantType.SPECIFIC,
                false,
                attributes
            ))
        );
    }

    public RoleAssignmentResource getCaseRoleAssignmentsForUser(long caseId, String idamUserId) {
        QueryRequest queryRequest = QueryRequest.builder()
                .roleType(List.of(RoleType.CASE))
                .roleCategory(List.of(RoleCategory.PROFESSIONAL, RoleCategory.CITIZEN))
                .roleName(List.of(RoleName.CREATOR, RoleName.LEGAL_REPRESENTATIVE))
                .actorId(List.of(idamUserId))
                .attributes(Map.of(
                        Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                        Attributes.CASE_TYPE, List.of("Asylum"),
                        Attributes.CASE_ID, List.of(String.valueOf(caseId))
                )).build();

        log.info("Query role assignment with the parameters: {}, for case reference: {}", queryRequest, caseId);

        return queryRoleAssignments(queryRequest);
    }

    public RoleAssignmentResource queryRoleAssignments(QueryRequest queryRequest) {
        return roleAssignmentApi.queryRoleAssignments(
            userDetails.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            queryRequest
        );
    }

    public void deleteRoleAssignment(String assignmentId) {
        if (assignmentId != null) {
            roleAssignmentApi.deleteRoleAssignment(
                idamService.getServiceUserToken(),
                serviceAuthTokenGenerator.generate(), assignmentId);
        }
    }

}
