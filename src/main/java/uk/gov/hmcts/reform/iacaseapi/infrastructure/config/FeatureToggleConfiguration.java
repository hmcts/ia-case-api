package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDClientInterface;
import com.launchdarkly.client.LDConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfiguration {

    @Value("${launchDarkly.sdkKey}")
    private String sdkKey;

    @Value("${launchDarkly.connectionTimeout}")
    private Integer connectionTimeout;

    @Value("${launchDarkly.socketTimeout}")
    private Integer socketTimeout;

    @Bean
    public LDConfig ldConfig() {
        return new LDConfig.Builder()
            .connectTimeout(connectionTimeout)
            .socketTimeout(socketTimeout)
            .build();
    }

    @Bean
    public LDClientInterface ldClient(LDConfig ldConfig) {
        return new LDClient(sdkKey, ldConfig);
    }

}
