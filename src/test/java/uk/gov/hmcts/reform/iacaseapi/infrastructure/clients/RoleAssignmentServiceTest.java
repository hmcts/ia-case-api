package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RequestedRoles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;

class RoleAssignmentServiceTest {

    @Test
    void createsCaseRole() {
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        String serviceToken = "serviceToken";
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        RoleAssignmentApi roleAssignmentApi = mock(RoleAssignmentApi.class);
        UserDetails userDetails = mock(UserDetails.class);
        String accessToken = "accessToken";
        String userId = "userId";
        when(userDetails.getAccessToken()).thenReturn(accessToken);
        when(userDetails.getId()).thenReturn(userId);

        RoleAssignmentService roleAssignmentService =
                new RoleAssignmentService(authTokenGenerator, roleAssignmentApi, userDetails);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> caseDetails = mock(CaseDetails.class);
        long caseId = 1234567890L;
        when(caseDetails.getId()).thenReturn(caseId);

        roleAssignmentService.assignRole(caseDetails.getId());

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
                    userId,
                    RoleType.CASE,
                    "tribunal-caseworker",
                    RoleCategory.STAFF,
                    Classification.RESTRICTED,
                    GrantType.SPECIFIC,
                    false,
                    attributes
                ))

            )
        );
    }

}
