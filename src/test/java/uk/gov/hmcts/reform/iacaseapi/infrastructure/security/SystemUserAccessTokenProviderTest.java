package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamAuthorizor;

@RunWith(MockitoJUnitRunner.class)
public class SystemUserAccessTokenProviderTest {

    private static final String SYSTEM_USERNAME = "system";
    private static final String SYSTEM_PASSWORD = "secret";

    @Mock private IdamAuthorizor idamAuthorizor;

    private SystemUserAccessTokenProvider systemUserAccessTokenProvider;

    @Before
    public void setUp() {

        systemUserAccessTokenProvider =
            new SystemUserAccessTokenProvider(
                SYSTEM_USERNAME,
                SYSTEM_PASSWORD,
                idamAuthorizor
            );
    }

    @Test
    public void get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(expectedAccessToken);

        String actualAccessToken = systemUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    public void try_get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(expectedAccessToken);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedAccessToken, optionalAccessToken.get());
    }

    @Test
    public void try_get_access_token_from_idam_when_it_returns_null() {

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(null);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }
}
