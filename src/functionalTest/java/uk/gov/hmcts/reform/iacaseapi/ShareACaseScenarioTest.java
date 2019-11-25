package uk.gov.hmcts.reform.iacaseapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import java.util.List;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.fixtures.Fixture;
import uk.gov.hmcts.reform.iacaseapi.util.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.iacaseapi.util.MapSerializer;
import uk.gov.hmcts.reform.iacaseapi.util.MapValueExpander;
import uk.gov.hmcts.reform.iacaseapi.verifiers.Verifier;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class ShareACaseScenarioTest {


    @Value("${targetInstance}") private String targetInstance;

    @Autowired private Environment environment;
    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired private MapValueExpander mapValueExpander;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private List<Verifier> verifiers;
    @Autowired private List<Fixture> fixtures;

    @Before
    public void setUp() {
        MapSerializer.setObjectMapper(objectMapper);
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_update_ccd() {

    }

}
