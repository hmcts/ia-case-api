package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatethecase;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaseWorkerRefDataWiremockConfig {
    @Autowired
    private WireMockServer wireMockServer;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer caseWorkerRefDataServiceMock() {
        return new WireMockServer();
    }

}
