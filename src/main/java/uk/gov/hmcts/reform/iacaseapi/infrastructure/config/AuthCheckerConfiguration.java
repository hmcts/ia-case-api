package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security")
public class AuthCheckerConfiguration {

    private List<String> authorisedServices = new ArrayList<>();
    private List<String> authorisedRoles = new ArrayList<>();

    public List<String> getAuthorisedServices() {
        return Collections.unmodifiableList(authorisedServices);
    }

    public void setAuthorisedServices(List<String> authorisedServices) {
        this.authorisedServices = authorisedServices;
    }

    public List<String> getAuthorisedRoles() {
        return Collections.unmodifiableList(authorisedRoles);
    }

    public void setAuthorisedRoles(List<String> authorisedRoles) {
        this.authorisedRoles = authorisedRoles;
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
}
