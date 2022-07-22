package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringAuthorizedRolesProvider implements AuthorizedRolesProvider {

    @Override
    public Set<String> getRoles() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getAuthorities)
            .orElse(Collections.emptySet())
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }
}
