package uk.gov.hmcts.reform.iacaseapi.consumer.roleassignment;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "am_roleAssignment_createAssignment", port = "8991")
@SpringJUnitConfig(classes = {RoleAssignmentConsumerApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class RoleAssignmentApiConsumerTest {

    @Autowired
    RoleAssignmentApi roleAssignmentApi;

    @MockitoBean
    AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserDetails userDetails;
    @Mock
    private IdamService idamService;

    @Mock
    private CaseDetails<CaseData> caseDetails;

    private RoleAssignmentService roleAssignmentService;

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthorizationToken";
    private final String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a0f";
    private final String assigneeId = "14a21569-eb80-4681-b62c-6ae2ed069e5f";
    private final long caseId = 1212121212121213L;

    @BeforeEach
    void setUp() throws Exception {
        Thread.sleep(2000);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        when(userDetails.getAccessToken()).thenReturn(AUTH_TOKEN);
        when(userDetails.getId()).thenReturn(userId);

        when(caseDetails.getId()).thenReturn(caseId);

        roleAssignmentService = new RoleAssignmentService(authTokenGenerator, roleAssignmentApi, userDetails);
    }

    @Pact(provider = "am_roleAssignment_createAssignment", consumer = "ia_caseApi")
    public V4Pact generatePactFragment(PactDslWithProvider builder)
        throws JSONException, JsonProcessingException {
        return builder
            .given("The assignment request is valid with one requested role and replaceExisting flag as true")
            .uponReceiving("A request to add a role")
            .path("/am/role-assignments")
            .method("POST")
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .body(new ObjectMapper()
                .writeValueAsString(roleAssignmentService.getRoleAssignment(caseId, assigneeId, userId)))
            .willRespondWith()            
            .status(201)
            .toPact(V4Pact.class);
    }


    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyAssignRole() {
        roleAssignmentService.assignRole(caseId, assigneeId);

    }

}
