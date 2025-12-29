package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdamUserDetailsProvider;

@Configuration
public class UserDetailsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserDetailsProvider getRequestUserDetailsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        IdamApi idamApi,
        IdamService idamService
    ) {
        return new IdamUserDetailsProvider(
            requestUserAccessTokenProvider,
            idamApi,
            idamService
        );
    }

    @Bean("requestUserDetails")
    @RequestScope
    public UserDetails getRequestUserDetails(UserDetailsProvider userDetailsProvider) {

        return userDetailsProvider.getUserDetails();
    }
}
