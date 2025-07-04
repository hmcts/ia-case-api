package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleAssignmentServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private RoleAssignmentApi roleAssignmentApi;
    @InjectMocks
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private UserDetails userDetails;
    @Mock
    private IdamService idamService;
    @Mock
    private CaseDetails<CaseData> caseDetails;
    @Captor
    private ArgumentCaptor<QueryRequest> queryRequestCaptor;
    private final String userId = "userId";
    private final long caseId = 1234567890L;
    private final String accessToken = "accessToken";
    private final String systemAccessToken = "systemAccessToken";
    private final String serviceToken = "serviceToken";
    private final String assignmentId = "assignmentId";

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(idamService.getServiceUserToken()).thenReturn(systemAccessToken);

        when(userDetails.getAccessToken()).thenReturn(accessToken);
        when(userDetails.getId()).thenReturn(userId);

        when(caseDetails.getId()).thenReturn(caseId);
    }

    @Test
    void createsCaseRole() {

        roleAssignmentService.assignRole(caseDetails.getId(), "assigneeId");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("caseId", Long.toString(caseId));
        verify(roleAssignmentApi).assignRole(
            accessToken,
            serviceToken,
            new RoleAssignment(
                new RoleRequest(
                    userId,
                    "case-allocation",
                    caseId + "/tribunal-caseworker",
                    true
                ),
                singletonList(new RequestedRoles(
                    ActorIdType.IDAM,
                    "assigneeId",
                    RoleType.CASE,
                    "tribunal-caseworker",
                    RoleCategory.LEGAL_OPERATIONS,
                    Classification.RESTRICTED,
                    GrantType.SPECIFIC,
                    false,
                    attributes
                ))

            )
        );
    }

    @Test
    void queryRoleAssignmentTest() {
        roleAssignmentService.queryRoleAssignments(QueryRequest.builder().build());

        verify(roleAssignmentApi).queryRoleAssignments(
            eq(accessToken),
            eq(serviceToken),
            any(QueryRequest.class)
        );

    }

    @Test
    void deleteRoleAssignmentTest() {
        roleAssignmentService.deleteRoleAssignment(assignmentId, systemAccessToken);

        verify(roleAssignmentApi).deleteRoleAssignment(
            systemAccessToken,
            serviceToken,
            assignmentId
        );
    }

    @Test
    void getCaseRoleAssignmentsForUserTest() {

        roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, userId);

        verify(roleAssignmentApi).queryRoleAssignments(
                eq(accessToken),
                eq(serviceToken),
                queryRequestCaptor.capture()
        );

        QueryRequest queryRequest = queryRequestCaptor.getValue();
        assertEquals(List.of(RoleType.CASE), queryRequest.getRoleType());
        assertEquals(List.of(RoleCategory.PROFESSIONAL, RoleCategory.CITIZEN), queryRequest.getRoleCategory());
        assertEquals(List.of(RoleName.CREATOR, RoleName.LEGAL_REPRESENTATIVE), queryRequest.getRoleName());
        assertEquals(List.of(userId), queryRequest.getActorId());
        Map<Attributes, List<String>> attributes = queryRequest.getAttributes();
        assertEquals(List.of(Jurisdiction.IA.name()), attributes.get(Attributes.JURISDICTION));
        assertEquals(List.of(String.valueOf(caseId)), attributes.get(Attributes.CASE_ID));
        assertEquals(List.of("Asylum"), attributes.get(Attributes.CASE_TYPE));

    }

    @Test
    void getCaseRolesForUserTest() {
        Assignment assignment1 = Assignment.builder()
            .roleName(RoleName.CTSC_TEAM_LEADER)
            .roleType(RoleType.CASE)
            .actorId(userId)
            .build();
        Assignment assignment2 = Assignment.builder()
            .roleName(RoleName.CTSC)
            .roleType(RoleType.CASE)
            .actorId(userId)
            .build();
        Assignment assignment3 = Assignment.builder()
            .roleName(RoleName.HEARING_CENTRE_ADMIN)
            .roleType(RoleType.CASE)
            .actorId(userId)
            .build();

        when(roleAssignmentApi.getRoleAssignments(
            accessToken,
            serviceToken,
            userId
        )).thenReturn(new RoleAssignmentResource(List.of(assignment1, assignment2, assignment3)));

        List<String> roles = roleAssignmentService.getAmRolesFromUser(userId, accessToken);

        verify(roleAssignmentApi).getRoleAssignments(
            accessToken,
            serviceToken,
            userId
        );
        assertTrue(roles.contains(RoleName.CTSC_TEAM_LEADER.getValue()));
        assertTrue(roles.contains(RoleName.CTSC.getValue()));
        assertTrue(roles.contains(RoleName.HEARING_CENTRE_ADMIN.getValue()));
    }

    @Test
    void removeCaseManagerRoleShouldRemoveRole() {
        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .grantType(List.of(GrantType.SPECIFIC))
            .roleName(List.of(RoleName.CASE_MANAGER))
            .roleCategory(List.of(RoleCategory.LEGAL_OPERATIONS))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of("1234123412341234")
            ))
            .build();
        Assignment assignment = Assignment.builder()
                .actorId("987987987987")
                .id(assignmentId)
                .build();
        RoleAssignmentResource roleAssignmentResource = new RoleAssignmentResource(List.of(assignment));

        when(roleAssignmentApi.queryRoleAssignments(accessToken, serviceToken, queryRequest))
            .thenReturn(roleAssignmentResource);

        roleAssignmentService.removeCaseManagerRole("1234123412341234", systemAccessToken);

        verify(roleAssignmentApi).queryRoleAssignments(accessToken, serviceToken, queryRequest);
        verify(roleAssignmentApi).deleteRoleAssignment(systemAccessToken, serviceToken, assignmentId);

    }

    @Test
    void removeCaseManagerRoleShouldLogErrorWhenRoleAssignmentNotFound() {
        QueryRequest queryRequest = QueryRequest.builder()
                .roleType(List.of(RoleType.CASE))
                .grantType(List.of(GrantType.SPECIFIC))
                .roleName(List.of(RoleName.CASE_MANAGER))
                .roleCategory(List.of(RoleCategory.LEGAL_OPERATIONS))
                .attributes(Map.of(
                        Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                        Attributes.CASE_TYPE, List.of("Asylum"),
                        Attributes.CASE_ID, List.of("1234123412341234")
                ))
                .build();

        RoleAssignmentResource roleAssignmentResource = new RoleAssignmentResource(List.of());

        when(userDetails.getRoles()).thenReturn(List.of(UserRole.IDAM_JUDGE.getId()));

        when(roleAssignmentApi.queryRoleAssignments(accessToken, serviceToken, queryRequest))
                .thenReturn(roleAssignmentResource);

        roleAssignmentService.removeCaseManagerRole("1234123412341234", systemAccessToken);

        verify(roleAssignmentApi).queryRoleAssignments(accessToken, serviceToken, queryRequest);
        verify(roleAssignmentApi, times(0)).deleteRoleAssignment(systemAccessToken, serviceToken, assignmentId);

    }
}
