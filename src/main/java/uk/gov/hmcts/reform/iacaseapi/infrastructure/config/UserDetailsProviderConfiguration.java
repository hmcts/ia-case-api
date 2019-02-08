package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.SystemUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamUserDetailsProvider;

@Configuration
public class UserDetailsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserDetailsProvider getRequestUserDetailsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        RestTemplate restTemplate,
        @Value("${auth.idam.client.baseUrl}") String baseUrl,
        @Value("${auth.idam.client.detailsUri}") String detailsUri
    ) {
        return new IdamUserDetailsProvider(
            requestUserAccessTokenProvider,
            restTemplate,
            baseUrl,
            detailsUri
        );
    }

    @Bean("systemUser")
    public UserDetailsProvider getSystemUserDetailsProvider(
        SystemUserAccessTokenProvider systemUserAccessTokenProvider,
        RestTemplate restTemplate,
        @Value("${auth.idam.client.baseUrl}") String baseUrl,
        @Value("${auth.idam.client.detailsUri}") String detailsUri
    ) {
        return new IdamUserDetailsProvider(
            systemUserAccessTokenProvider,
            restTemplate,
            baseUrl,
            detailsUri
        );
    }
}
