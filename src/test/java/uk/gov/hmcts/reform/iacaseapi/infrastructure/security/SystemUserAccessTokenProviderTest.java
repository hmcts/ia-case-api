package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamAuthorizor;

@ExtendWith(MockitoExtension.class)
class SystemUserAccessTokenProviderTest {

    static final String SYSTEM_USERNAME = "system";
    static final String SYSTEM_PASSWORD = "secret";

    @Mock private IdamAuthorizor idamAuthorizor;

    SystemUserAccessTokenProvider systemUserAccessTokenProvider;

    @BeforeEach
    void setUp() {

        systemUserAccessTokenProvider =
            new SystemUserAccessTokenProvider(
                SYSTEM_USERNAME,
                SYSTEM_PASSWORD,
                idamAuthorizor
            );
    }

    @Test
    void get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(expectedAccessToken);

        String actualAccessToken = systemUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    void get_missing_access_token_from_idam_throws_if_not_a_try_attempt() {

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(null);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.getAccessToken())
            .hasMessage("System access token not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void try_get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(expectedAccessToken);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedAccessToken, optionalAccessToken.get());
    }

    @Test
    void try_get_access_token_from_idam_when_it_returns_null() {

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(null);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }
}
