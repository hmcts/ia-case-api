package uk.gov.hmcts.reform.bailcaseapi.consumer.refdata;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(locations = {"classpath:application.properties"}, properties = {"location.ref.data.url=http://localhost:8991"})
@PactTestFor(providerName = "referenceData_court_venues", port = "8991")
@ContextConfiguration(classes = {RefDataConsumerApplication.class})
public class LocationRefDataConsumerTest {

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Value("${hmcts_service_id}")
    private String serviceId;

    @Autowired
    LocationRefDataApi locationRefDataApi;

    @Pact(provider = "referenceData_court_venues", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, JsonProcessingException {

        return builder
            .given("Service ID")
            .uponReceiving("A request for court venues")
            .path("/refdata/location/court-venues/services")
            .matchQuery("service_code", "some-id")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .willRespondWith()
            .body(buildLocationResponsePactDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyLocationsFetch() {

        CourtLocationCategory courtLocationCategory = locationRefDataApi.getCourtVenues(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            serviceId);
        assertEquals("serviceCode", courtLocationCategory.getServiceCode());
        assertEquals("courtTypeId", courtLocationCategory.getCourtTypeId());
        assertEquals("courtType", courtLocationCategory.getCourtType());
        assertEquals("siteName", courtLocationCategory.getCourtVenues().get(0).getSiteName());
        assertEquals("epimmsId", courtLocationCategory.getCourtVenues().get(0).getEpimmsId());
    }

    private DslPart buildLocationResponsePactDsl() {
        return newJsonBody(o -> {
            o.stringType("service_code", "serviceCode")
                .stringType("court_type_id", "courtTypeId")
                .stringType("court_type", "courtType")
                .minArrayLike("court_venues", 1, 1,
                    sh -> {
                        sh.stringType("site_name", "siteName")
                            .stringType("court_name", "courtName")
                            .stringType("epimms_id", "epimmsId");

                    });
        }).build();
    }
}
