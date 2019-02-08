package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

public class CcdEventAuthorizor {

    private final Map<String, List<Event>> roleEventAccess;
    private final UserDetailsProvider userDetailsProvider;

    public CcdEventAuthorizor(
        Map<String, List<Event>> roleEventAccess,
        UserDetailsProvider userDetailsProvider
    ) {
        this.roleEventAccess = roleEventAccess;
        this.userDetailsProvider = userDetailsProvider;
    }

    public void throwIfNotAuthorized(
        Event event
    ) {
        List<String> requiredRoles = getRequiredRolesForEvent(event);
        List<String> userRoles =
            userDetailsProvider
                .getUserDetails()
                .getRoles();

        if (requiredRoles.isEmpty()
            || userRoles.isEmpty()
            || Collections.disjoint(requiredRoles, userRoles)) {

            throw new AccessDeniedException(
                "Event '" + event.toString() + "' not allowed"
            );
        }
    }

    private List<String> getRequiredRolesForEvent(
        Event event
    ) {
        return roleEventAccess
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().contains(event))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
