package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@ExtendWith(MockitoExtension.class)
class RequestUserAccessTokenProviderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private RequestUserAccessTokenProvider requestUserAccessTokenProvider =
        new RequestUserAccessTokenProvider();

    @BeforeEach
    public void setUp() {

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(httpServletRequest)
        );
    }

    @Test
    void get_access_token_from_http_request() {

        String expectedAccessToken = "access-token";

        when(httpServletRequest.getHeader("Authorization")).thenReturn(expectedAccessToken);

        String actualAccessToken = requestUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    void get_missing_access_token_from_http_request_throws_if_not_a_try_attempt() {

        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> requestUserAccessTokenProvider.getAccessToken())
            .hasMessage("Request access token not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void try_get_access_token_from_http_request() {

        String expectedAccessToken = "access-token";

        when(httpServletRequest.getHeader("Authorization")).thenReturn(expectedAccessToken);

        Optional<String> optionalAccessToken = requestUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedAccessToken, optionalAccessToken.get());
    }

    @Test
    void try_get_missing_access_token_from_http_request_returns_empty() {

        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        Optional<String> optionalAccessToken = requestUserAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }

    @Test
    void when_no_current_http_request_exists_it_throws() {

        RequestContextHolder.resetRequestAttributes();

        assertThatThrownBy(() -> requestUserAccessTokenProvider.tryGetAccessToken())
            .hasMessage("No current HTTP request")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
