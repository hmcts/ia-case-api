package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import com.launchdarkly.sdk.server.Components;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfiguration {

    @Value("${launchDarkly.sdkKey}")
    private String sdkKey;

    @Value("${launchDarkly.socketTimeout}")
    private Integer connectionTimeout;

    @Value("${launchDarkly.socketTimeout}")
    private Integer socketTimeout;

    @Bean
    public LDConfig ldConfig() {
        return new LDConfig.Builder()
            .http(Components
                      .httpConfiguration()
                      .connectTimeout(Duration.ofMillis(connectionTimeout))
                      .socketTimeout(Duration.ofMillis(socketTimeout))
            )
            .build();
    }

    @Bean
    public LDClientInterface ldClient(LDConfig ldConfig) {
        return new LDClient(sdkKey, ldConfig);
    }

}
