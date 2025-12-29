package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;

@ExtendWith(MockitoExtension.class)
class CcdEventAuthorizorTest {

    @Mock
    private AuthorizedRolesProvider authorizedRolesProvider;

    private String role = "caseworker-ia";
    private Map<String, List<Event>> roleEventAccess = new ImmutableMap.Builder<String, List<Event>>()
        .put(role, newArrayList(Event.UNKNOWN))
        .build();

    private CcdEventAuthorizor ccdEventAuthorizor;

    @Test
    void should_not_throw_exception_when_event_is_allowed() {

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);

        when(authorizedRolesProvider.getRoles()).thenReturn(newHashSet(role));

        ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN);
    }

    @Test
    void should_throw_exception_when_provider_returns_empty_list() {

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);

        when(authorizedRolesProvider.getRoles()).thenReturn(newHashSet());

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );
        assertEquals("Event 'unknown' not allowed", thrown.getMessage());
    }

    @Test
    void should_throw_exception_when_access_map_is_empty() {

        Map<String, List<Event>> roleEventAccess = new ImmutableMap.Builder<String, List<Event>>().build();

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );
        assertEquals("Event 'unknown' not allowed", thrown.getMessage());
    }

    @Test
    void should_throw_exception_when_access_map_for_role_is_empty() {

        Map<String, List<Event>> roleEventAccess = new ImmutableMap.Builder<String, List<Event>>()
            .put(role, newArrayList())
            .build();

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );
        assertEquals("Event 'unknown' not allowed", thrown.getMessage());
    }
}
