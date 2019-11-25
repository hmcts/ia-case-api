package uk.gov.hmcts.reform.iacaseapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class ShareACaseScenarioTest {
}
