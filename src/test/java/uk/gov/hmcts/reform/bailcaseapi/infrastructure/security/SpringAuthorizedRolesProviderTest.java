package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SpringAuthorizedRolesProviderTest {

    private final AuthorizedRolesProvider authorizedRolesProvider = new SpringAuthorizedRolesProvider();
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void should_return_empty_list_when_authentication_is_null() {

        assertEquals(Collections.emptySet(), authorizedRolesProvider.getRoles());
    }

    @Test
    void should_return_empty_list_when_authorities_are_empty_null() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Collections.emptySet(), authorizedRolesProvider.getRoles());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_return_empty_list_when_authorities_return_some_roles() {
        List grantedAuthorities =
            Lists.newArrayList(new SimpleGrantedAuthority("ccd-role"), new SimpleGrantedAuthority("ccd-admin"));
        when(authentication.getAuthorities()).thenReturn(grantedAuthorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertEquals(Sets.newHashSet("ccd-role", "ccd-admin"), authorizedRolesProvider.getRoles());
    }

    @AfterAll
    public static void cleanUp() {
        SecurityContextHolder.clearContext();
    }
}
