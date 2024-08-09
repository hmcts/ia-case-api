package uk.gov.hmcts.reform.iacaseapi.consumer.fee;

import java.math.BigDecimal;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeesRegisterApi;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "feeRegister_lookUp", port = "8991")
@ContextConfiguration(classes = {FeeApiConsumerApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class FeeApiConsumerTest {

    @Autowired
    FeesRegisterApi feesRegisterApi;

    @BeforeEach
    public void prepareTest() throws Exception {
        Thread.sleep(2000);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "ia_caseApi")
    private RequestResponsePact generateFeeWithHearingPact(PactDslWithProvider builder) {
        return getRequestResponsePact(builder, "HearingOral", "FEE0238",
            "Appeal determined with a hearing", BigDecimal.valueOf(140.00)
        );
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "ia_caseApi")
    private RequestResponsePact generateFeeWithoutHearingPact(PactDslWithProvider builder) {
        return getRequestResponsePact(builder, "HearingPaper", "FEE0372",
            "Appeal determined without a hearing", BigDecimal.valueOf(80.00)
        );
    }

    private RequestResponsePact getRequestResponsePact(PactDslWithProvider builder, String keyword, String code,
                                                       String description, BigDecimal feeAmount) {
        return builder
            .given("Fees exist for IA")
            .uponReceiving("A request for IA Fees")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("service", "other", "other")
            .matchQuery("jurisdiction1", "tribunal", "tribunal")
            .matchQuery("jurisdiction2", "immigration and asylum chamber", "immigration and asylum chamber")
            .matchQuery("channel", "default", "default")
            .matchQuery("event", "issue", "issue")
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseDsl(code, description, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private PactDslJsonBody buildFeesResponseDsl(String code, String description, BigDecimal feeAmount) {
        return new PactDslJsonBody()
            .stringType("code", code)
            .stringType("description", description)
            .numberType("version", 2)
            .decimalType("fee_amount", feeAmount);
    }

    @Test
    @PactTestFor(pactMethod = "generateFeeWithHearingPact")
    public void verifyFeesWithHearingPact() {
        FeeResponse feeResponse = feesRegisterApi.findFee("default", "issue", "tribunal",
            "immigration and asylum chamber", "HearingOral",
            "other");
        Assertions.assertEquals("FEE0238", feeResponse.getCode());
    }

    @Test
    @PactTestFor(pactMethod = "generateFeeWithoutHearingPact")
    public void verifyFeesWithoutHearingPact() {
        FeeResponse feeResponse = feesRegisterApi.findFee("default", "issue", "tribunal",
            "immigration and asylum chamber", "HearingPaper",
            "other");
        Assertions.assertEquals("FEE0372", feeResponse.getCode());
    }
}
