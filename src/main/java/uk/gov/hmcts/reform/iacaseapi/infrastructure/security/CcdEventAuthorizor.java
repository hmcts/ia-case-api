package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

public class CcdEventAuthorizor {

    private final Map<String, List<Event>> roleEventAccess;
    private final UserCredentialsProvider requestUserCredentialsProvider;

    public CcdEventAuthorizor(
        Map<String, List<Event>> roleEventAccess,
        @Qualifier("requestUser") UserCredentialsProvider requestUserCredentialsProvider
    ) {
        this.roleEventAccess = roleEventAccess;
        this.requestUserCredentialsProvider = requestUserCredentialsProvider;
    }

    public void throwIfNotAuthorized(
        Event event
    ) {
        List<String> requiredRoles = getRequiredRolesForEvent(event);
        List<String> userRoles = requestUserCredentialsProvider.getRoles();

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
