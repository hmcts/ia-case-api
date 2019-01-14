package uk.gov.hmcts.reform.iacaseapi.integration.util;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"core_case_data_api_url=http://127.0.0.1:8990"})
@Import(TestConfig.class)
public abstract class SpringBootIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8990);

}