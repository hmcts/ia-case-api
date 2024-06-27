package uk.gov.hmcts.reform.iacaseapi.consumer.refdata;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(locations = {"classpath:application.properties"})
@PactTestFor(providerName = "referenceData_professionalExternalUsers", port = "8991")
public class ProfessionalUsersConsumerTest {

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    static final String IDAM_ID_OF_USER_CREATING_CASE = "0a5874a4-3f38-4bbd-ba4c";

    @MockBean
    AuthTokenGenerator serviceAuthTokenGenerator;
    @MockBean
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;
    @Value("${prof.ref.data.url}")
    String refDataApiUrl;
    @Value("${prof.ref.data.path.org.users}")
    String refDataApiPath;

    ProfessionalUsersRetriever professionalUsersRetriever;

    @BeforeEach
    public void setUpTest() {
        professionalUsersRetriever =
            new ProfessionalUsersRetriever(new RestTemplate(), serviceAuthTokenGenerator, userDetails, refDataApiUrl, refDataApiPath);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(AUTHORIZATION_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);

    }

    @Pact(provider = "referenceData_professionalExternalUsers", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentForGetUserOrganisation(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Professional users exist for an Active organisation")
            .uponReceiving("A Request to get users for an active organisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations/users")
            //.query("status=ACTIVE&returnRoles=false")
            .willRespondWith()
            .body(buildOrganisationsResponsePactDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetUserOrganisation")
    public void verifyUserOrganisation() {
        ProfessionalUsersResponse usersResponse = professionalUsersRetriever.retrieve();
        assertThat(usersResponse, is(notNullValue()));
        assertThat(usersResponse.getUsers().get(0).getUserIdentifier(), is("userId"));
    }

    protected DslPart buildOrganisationsResponsePactDsl() {
        return newJsonBody(ob -> ob
            .array("users", pa ->
                pa.object(u -> u.stringType("userIdentifier", "userId"))
            ))
            .build();
    }

}
