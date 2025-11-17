package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

@Slf4j
public class CcdEventAuthorizor {

    private final Map<String, List<Event>> roleEventAccess;
    private final AuthorizedRolesProvider authorizedRolesProvider;

    public CcdEventAuthorizor(Map<String, List<Event>> roleEventAccess, AuthorizedRolesProvider authorizedRolesProvider) {
        this.roleEventAccess = roleEventAccess;
        this.authorizedRolesProvider = authorizedRolesProvider;
    }

    public void throwIfNotAuthorized(Event event) {

        List<String> requiredRoles = getRequiredRolesForEvent(event);
        Set<String> userRoles = authorizedRolesProvider.getRoles();

        if (requiredRoles.isEmpty()
            || userRoles.isEmpty()
            || Collections.disjoint(requiredRoles, userRoles)) {

            log.warn("User with roles {} not authorized for event '{}' which requires one of roles {}",
                userRoles, event.toString(), requiredRoles);
            throw new AccessDeniedException("Event '" + event.toString() + "' not allowed");
        }
    }

    private List<String> getRequiredRolesForEvent(Event event) {

        return roleEventAccess
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().contains(event))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
