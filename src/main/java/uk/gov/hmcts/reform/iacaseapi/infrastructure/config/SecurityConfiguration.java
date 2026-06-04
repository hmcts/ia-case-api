package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.SpringAuthorizedRolesProvider;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final List<String> anonymousPaths = new ArrayList<>();
    private final Map<String, List<Event>> roleEventAccess = new HashMap<>();

    private final Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter;
    private final ServiceAuthFilter serviceAuthFiler;

    public SecurityConfiguration(Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter,
                                 ServiceAuthFilter serviceAuthFiler) {
        this.idamAuthoritiesConverter = idamAuthoritiesConverter;
        this.serviceAuthFiler = serviceAuthFiler;
    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(anonymousPaths
                .stream()
                .toArray(String[]::new)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(idamAuthoritiesConverter);


        http
            .addFilterBefore(serviceAuthFiler, AbstractPreAuthenticatedProcessingFilter.class)
            .sessionManagement(management -> management.sessionCreationPolicy(STATELESS))
            .exceptionHandling(withDefaults())
            .csrf(csrf -> csrf.disable())
            .formLogin(login -> login.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
            .oauth2ResourceServer(server -> server
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )
            .oauth2Client(withDefaults());

        return http.build();
    }

    @Bean
    public AuthorizedRolesProvider authorizedRolesProvider() {
        return new SpringAuthorizedRolesProvider();
    }

    @Bean
    public CcdEventAuthorizor getCcdEventAuthorizor(AuthorizedRolesProvider authorizedRolesProvider) {
        return new CcdEventAuthorizor(
            ImmutableMap.copyOf(roleEventAccess),
            authorizedRolesProvider
        );
    }

    public Map<String, List<Event>> getRoleEventAccess() {
        return roleEventAccess;
    }

}
