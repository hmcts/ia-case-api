package uk.gov.hmcts.reform.iacaseapi.consumer.roleassignment;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.*;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "am_roleAssignment_queryAssignment", port = "8991")
@ContextConfiguration(classes = {RoleAssignmentConsumerApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class RoleAssignmentQueryConsumerTest {

    @Autowired
    RoleAssignmentApi roleAssignmentApi;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CaseDetails caseDetails;
    @Mock
    private IdamService idamService;

    private RoleAssignmentService roleAssignmentService;

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthorizationToken";

    private final String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a0f";
    private final String assigneeId = "14a21569-eb80-4681-b62c-6ae2ed069e5f";
    private final long caseId = 1212121212121213L;
    private final LocalDateTime validAtDate = LocalDateTime.parse("2021-12-04T00:00:00");

    @BeforeEach
    void setUp() throws Exception {
        Thread.sleep(2000);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        when(userDetails.getAccessToken()).thenReturn(AUTH_TOKEN);
        when(userDetails.getId()).thenReturn(userId);

        when(caseDetails.getId()).thenReturn(caseId);

        roleAssignmentService = new RoleAssignmentService(authTokenGenerator, roleAssignmentApi, userDetails);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_roleAssignment_queryAssignment", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentForQueryRoleAssignments(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("A list of role assignments for the search query")
            .uponReceiving("A query request for roles")
            .path("/am/role-assignments/query")
            .method("POST")
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .body(createRoleAssignmentRequestSearchQueryMultipleRoleAssignments())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(getResponseHeaders())
            .body(createRoleAssignmentResponseSearchQueryResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForQueryRoleAssignments")
    public void verifyQueryRoleAssignments() {
        List<Assignment> queryRoleAssignmentResponse = roleAssignmentService
            .queryRoleAssignments(buildQueryRequest()
            ).getRoleAssignmentResponse();

        assertThat(queryRoleAssignmentResponse.get(0).getActorId(), is(assigneeId));

    }

    private QueryRequest buildQueryRequest() {
        return QueryRequest.builder()
            .roleType(List.of(RoleType.ORGANISATION))
            .roleName(List.of(RoleName.TRIBUNAL_CASEWORKER, RoleName.SENIOR_TRIBUNAL_CASEWORKER))
            .grantType(List.of(GrantType.STANDARD))
            .classification(List.of(PUBLIC, RESTRICTED, PRIVATE))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.PRIMARY_LOCATION, List.of("500A2S")
            ))
            .validAt(validAtDate)
            .build();
    }


    private DslPart createRoleAssignmentResponseSearchQueryResponse() {
        return newJsonBody(o -> o
            .minArrayLike("roleAssignmentResponse", 1, 1,
                roleAssignmentResponse -> roleAssignmentResponse
                    .stringType("id", "14a21569-eb80-4681-b62c-6ae2ed069e6f")
                    .stringValue("actorIdType", "IDAM")
                    .stringValue("actorId", assigneeId)
                    .stringValue("roleType", "ORGANISATION")
                    .stringValue("roleName", "senior-tribunal-caseworker")
                    .stringValue("classification", "PRIVATE")
                    .stringValue("grantType", "STANDARD")
                    .stringValue("roleCategory", "LEGAL_OPERATIONS")
                    .booleanValue("readOnly", false)
                    .object("attributes", attribute -> attribute
                        .stringType("jurisdiction", "IA")
                        .stringType("primaryLocation", "500A2S"))
            )).build();
    }

    private String createRoleAssignmentRequestSearchQueryMultipleRoleAssignments() {
        return "{\n"
            + "\"roleType\": [\"ORGANISATION\"],\n"
            + "\"roleName\": [\"tribunal-caseworker\",\"senior-tribunal-caseworker\"],\n"
            + "\"classification\": [\"PUBLIC\",\"RESTRICTED\",\"PRIVATE\"],\n"
            + "\"grantType\": [\"STANDARD\"],\n"
            + "\"validAt\": \"2021-12-04T00:00:00\",\n"
            + "\"attributes\": {\n"
            + "\"primaryLocation\": [\"500A2S\"],\n"
            + "\"jurisdiction\": [\"IA\"]\n"
            + "}\n"
            + "}";
    }

    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
            "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;"
                + "charset=UTF-8;version=1.0");
        return responseHeaders;
    }

}
