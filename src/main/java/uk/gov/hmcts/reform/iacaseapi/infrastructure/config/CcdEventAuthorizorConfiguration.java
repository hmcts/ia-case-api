package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.UserCredentialsProvider;

@Configuration
@ConfigurationProperties(prefix = "security")
public class CcdEventAuthorizorConfiguration {

    private final Map<String, List<Event>> roleEventAccess = new HashMap<>();

    public Map<String, List<Event>> getRoleEventAccess() {
        return roleEventAccess;
    }

    @Bean
    @Primary
    public CcdEventAuthorizor getCcdEventAuthorizor(
        @Qualifier("requestUser") UserCredentialsProvider requestUserCredentialsProvider
    ) {
        return new CcdEventAuthorizor(
            ImmutableMap.copyOf(roleEventAccess),
            requestUserCredentialsProvider
        );
    }
}
