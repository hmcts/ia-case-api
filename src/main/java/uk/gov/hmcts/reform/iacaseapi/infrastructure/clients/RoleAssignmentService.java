package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.*;

@Component
public class RoleAssignmentService {
    public static final String ROLE_NAME = "tribunal-caseworker";
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final RoleAssignmentApi roleAssignmentApi;

    private final UserDetailsProvider userDetailsProvider;

    public RoleAssignmentService(AuthTokenGenerator serviceAuthTokenGenerator,
                                 RoleAssignmentApi roleAssignmentApi,
                                 UserDetailsProvider userDetailsProvider) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.roleAssignmentApi = roleAssignmentApi;
        this.userDetailsProvider = userDetailsProvider;
    }

    public void assignRole(CaseDetails<AsylumCase> caseDetails) {
        UserDetails userDetails = userDetailsProvider.getUserDetails();
        String accessToken = userDetails.getAccessToken();
        String currentUserIdamId = userDetails.getId();
        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("caseId", Long.toString(caseDetails.getId()));

        RoleAssignment body = new RoleAssignment(
                new RoleRequest(
                        currentUserIdamId,
                        "case-allocation",
                        caseDetails.getId() + "/" + ROLE_NAME,
                        true
                ),
                singletonList(new RequestedRoles(
                        ActorIdType.IDAM,
                        currentUserIdamId,
                        RoleType.CASE,
                        ROLE_NAME,
                        RoleCategory.STAFF,
                        Classification.PUBLIC, // ticket says restricted
                        GrantType.SPECIFIC,
                        false,
                        attributes
                ))

        );

        roleAssignmentApi.assignRole(accessToken, serviceAuthorizationToken, body);
    }
}
