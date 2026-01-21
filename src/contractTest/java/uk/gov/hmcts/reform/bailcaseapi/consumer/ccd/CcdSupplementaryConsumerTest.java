package uk.gov.hmcts.reform.bailcaseapi.consumer.ccd;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static java.util.Collections.singletonMap;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@PactTestFor(providerName = "ccdDataStoreAPI_supplementaryUpdate", port = "8873")
@TestPropertySource(
    properties = "core_case_data_supplementary_api_url=http://localhost:8873")
public class CcdSupplementaryConsumerTest extends CcdSupplementaryProviderBaseTest {

    @Pact(provider = "ccdDataStoreAPI_supplementaryUpdate", consumer = "bail_caseApi")
    public RequestResponsePact generatePactFragmentForSupplementaryUpdate(PactDslWithProvider builder)
        throws IOException {

        Map<String, Map<String, Object>> payloadData = Maps.newHashMap();
        payloadData.put("$set", singletonMap("HMCTSServiceId", "some-id"));

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("supplementary_data_updates", payloadData);

        // @formatter:off
        return builder
            .given("Supplementary data updated successfully")
            .uponReceiving("A Request to update supplementary data")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/cases/" + CASE_ID + "/supplementary-data")
            .body(createJsonObject(payload))
            .willRespondWith()
            .body(buildSupplementaryDataResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForSupplementaryUpdate")
    public void verifySupplementaryUpdate() {

        ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);

    }

    private DslPart buildSupplementaryDataResponseDsl() {
        return newJsonBody((o) -> {
            o.stringType("HMCTSServiceId",
                    "some-id");
        }).build();
    }
}
