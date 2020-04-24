package uk.gov.hmcts.reform.iacaseapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.parsing.Parser;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeResponse;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class FeesRegisterConsumerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String FEES_LOOKUP_URL = "/fees-register/fees/lookup";

    private static final String ORAL_FEE_QUERY_STRING = "channel=default&event=issue&jurisdiction1=tribunal&jurisdiction2=immigration%20and%20asylum%20chamber&keyword=ABC&service=other";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "fees_register_api", consumer = "ia_case_api")
    public RequestResponsePact executeFeeRegisterApiForOralFeeAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return builder
            .given("Fee-register api returns right fee response")
            .uponReceiving("Provider receives a GET /lookup request from an IA API")
            .path(FEES_LOOKUP_URL)
            .method(HttpMethod.GET.toString())
            .query(ORAL_FEE_QUERY_STRING)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(getOralFeeResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeFeeRegisterApiForOralFeeAndGet200Response")
    public void should_get_oral_fee_and_receive_code_with_200_response(MockServer mockServer) throws JSONException, IOException {

        String actualResponseBody =
            SerenityRest
                .given()
                .log().all(true)
                .when()
                .get(mockServer.getUrl() + FEES_LOOKUP_URL + "?" + ORAL_FEE_QUERY_STRING)
                .then()
                .statusCode(200)
                .and()
                .extract().body()
                .asString();

        FeeResponse feeResponse = objectMapper.readValue(actualResponseBody.getBytes(), FeeResponse.class);

        assertThat(feeResponse).isNotNull();
        assertThat(feeResponse.getCode()).isEqualTo("FEE0238");
        assertThat(feeResponse.getDescription()).isEqualTo("Appeal determined with a hearing");
        assertThat(feeResponse.getVersion()).isEqualTo(2);
        assertThat(feeResponse.getAmount()).isEqualTo(new BigDecimal(140.00));
    }

    private PactDslJsonBody getOralFeeResponse() {

        return new PactDslJsonBody()
            .stringValue("code", "FEE0238")
            .stringValue("description", "Appeal determined with a hearing")
            .integerType("version", 2)
            .decimalType("fee_amount", new BigDecimal(140.00));
    }
}
