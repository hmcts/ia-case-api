package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamRequestAuthorizer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamTokenVerification;

@Configuration
@ConfigurationProperties(prefix = "security")
public class AuthCheckerConfiguration {

    private final List<String> authorisedServices = new ArrayList<>();
    private final List<String> authorisedRoles = new ArrayList<>();

    public List<String> getAuthorisedServices() {
        return authorisedServices;
    }

    public List<String> getAuthorisedRoles() {
        return authorisedRoles;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return any -> ImmutableSet.copyOf(authorisedServices);
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return any -> ImmutableSet.copyOf(authorisedRoles);
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return any -> Optional.empty();
    }

    @Bean
    public JWSVerifierFactory jwsVerifierFactory() {

        return new DefaultJWSVerifierFactory();
    }

    @Bean
    public IdamTokenVerification idamTokenVerification(
        @Value("${auth.idam.client.baseUrl}") String idamBaseUrl,
        @Value("${auth.idam.client.jwkUri}") String idamJwkUri,
        RestTemplate restTemplate,
        JWSVerifierFactory jwsVerifierFactory) {

        return new IdamTokenVerification(idamBaseUrl, idamJwkUri, restTemplate, jwsVerifierFactory);
    }

    @Bean("idamRequestAuthorizer")
    @Primary
    public RequestAuthorizer<User> idamRequestAuthorizer(
        Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor,
        UserDetailsProvider userDetailsProvider,
        IdamTokenVerification idamTokenVerification) {

        return new IdamRequestAuthorizer(authorizedRolesExtractor, userDetailsProvider, idamTokenVerification);
    }
}
