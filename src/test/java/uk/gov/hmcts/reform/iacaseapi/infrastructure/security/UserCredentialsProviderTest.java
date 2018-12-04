package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserCredentialsProviderTest {

    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private AccessTokenDecoder accessTokenDecoder;

    private UserCredentialsProvider userCredentialsProvider;

    @Before
    public void setUp() {

        userCredentialsProvider =
            new UserCredentialsProvider(
                accessTokenProvider,
                accessTokenDecoder
            );
    }

    @Test
    public void get_access_token() {

        String accessToken = "access-token";

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        String actualAccessToken = userCredentialsProvider.getAccessToken();

        assertEquals(accessToken, actualAccessToken);
    }

    @Test
    public void try_get_access_token() {

        String accessToken = "access-token";

        when(accessTokenProvider.tryGetAccessToken()).thenReturn(Optional.of(accessToken));

        Optional<String> actualAccessToken = userCredentialsProvider.tryGetAccessToken();

        assertTrue(actualAccessToken.isPresent());
        assertEquals(accessToken, actualAccessToken.get());
    }

    @Test
    public void try_get_user_id() {

        String expectedUserId = "123";
        String accessToken = "access-token";
        Map<String, String> accessTokenClaims = Collections.singletonMap("id", expectedUserId);

        when(accessTokenProvider.tryGetAccessToken()).thenReturn(Optional.of(accessToken));
        when(accessTokenDecoder.decode(accessToken)).thenReturn(accessTokenClaims);

        Optional<String> actualUserId = userCredentialsProvider.tryGetId();

        assertTrue(actualUserId.isPresent());
        assertEquals(expectedUserId, actualUserId.get());
    }

    @Test
    public void try_get_user_id_returns_empty_optional_if_access_token_missing() {

        when(accessTokenProvider.tryGetAccessToken()).thenReturn(Optional.empty());

        Optional<String> actualUserId = userCredentialsProvider.tryGetId();

        assertFalse(actualUserId.isPresent());
    }

    @Test
    public void get_user_id_throws_if_access_token_missing_and_not_try_attempt() {

        when(accessTokenProvider.tryGetAccessToken()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCredentialsProvider.getId())
            .hasMessage("User id not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void get_user_roles() {

        String accessToken = "access-token";
        Map<String, String> accessTokenClaims = Collections.singletonMap("data", "\"this\",\"that\"");

        when(accessTokenProvider.tryGetAccessToken()).thenReturn(Optional.of(accessToken));
        when(accessTokenDecoder.decode(accessToken)).thenReturn(accessTokenClaims);

        List<String> actualUserRoles = userCredentialsProvider.getRoles();

        assertEquals(2, actualUserRoles.size());
        assertEquals("this", actualUserRoles.get(0));
        assertEquals("that", actualUserRoles.get(1));
    }

    @Test
    public void get_user_roles_returns_empty_list_if_access_token_missing() {

        when(accessTokenProvider.tryGetAccessToken()).thenReturn(Optional.empty());

        List<String> actualUserRoles = userCredentialsProvider.getRoles();

        assertTrue(actualUserRoles.isEmpty());
    }
}
