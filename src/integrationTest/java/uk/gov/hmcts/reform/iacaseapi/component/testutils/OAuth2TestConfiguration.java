package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import java.util.List;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@EnableWebSecurity
@EnableAutoConfiguration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OAuth2TestConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClientRegistrationRepository clientRegistrationRepository(
        OAuth2ClientProperties testOAuthClientProperties) {
        List<ClientRegistration> clientRegistrations =
            List.copyOf(
                new OAuth2ClientPropertiesMapper(testOAuthClientProperties)
                    .asClientRegistrations()
                    .values());
        return new InMemoryClientRegistrationRepository(clientRegistrations);
    }
}
