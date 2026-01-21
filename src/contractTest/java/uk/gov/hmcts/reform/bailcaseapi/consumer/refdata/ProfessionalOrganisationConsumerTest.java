package uk.gov.hmcts.reform.bailcaseapi.consumer.refdata;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(locations = {"classpath:application.properties"})
@PactTestFor(providerName = "referenceData_organisationalExternalUsers", port = "8991")
public class ProfessionalOrganisationConsumerTest {

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
    @Value("${prof.ref.data.path.org.organisation}")
    String refDataApiPath;

    ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    @BeforeEach
    public void setUpTest() {
        professionalOrganisationRetriever =
            new ProfessionalOrganisationRetriever(new RestTemplate(), serviceAuthTokenGenerator,
                                                  userDetailsProvider, refDataApiUrl,
                                                  refDataApiPath);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(AUTHORIZATION_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);

    }


    @Pact(provider = "referenceData_organisationalExternalUsers", consumer = "bail_caseApi")
    public RequestResponsePact generatePactFragmentForGetUserOrganisation(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Organisation with Id exists")
            .uponReceiving("A Request to get organisation for user")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations")
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetUserOrganisation")
    public void verifyUserOrganisation() {
        OrganisationEntityResponse userOrganisation = professionalOrganisationRetriever.retrieve();
        assertThat(userOrganisation, is(notNullValue()));
        assertThat(userOrganisation.getOrganisationIdentifier(), is("someOrganisationIdentifier"));
    }

    private DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o ->
            o.stringType("name", "theKCompany")
                .stringType("organisationIdentifier", "BJMSDFDS80808")
                .stringType("companyNumber", "companyNumber")
                .stringType("organisationIdentifier", "someOrganisationIdentifier")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", Boolean.TRUE)
                .stringType("status", "ACTIVE")
                .minArrayLike("contactInformation", 1, 1,
                    sh ->
                        sh.stringType("addressLine1", "addressLine1")
                            .stringType("addressLine2", "addressLine2")
                            .stringType("country", "UK")
                            .stringType("postCode", "SM12SX")
                    )
        ).build();
    }

}
