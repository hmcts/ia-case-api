package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemUserConnectionConfigTest {

    private IdamAuthorizor idamAuthorizor = mock(IdamAuthorizor.class);
    private AccessTokenDecoder accessTokenDecoder = mock(AccessTokenDecoder.class);

    @Captor ArgumentCaptor<String> username;

    @Captor ArgumentCaptor<String> password;

    private final SystemUserConnectionConfig underTest = new SystemUserConnectionConfig(
            "some-user",
            "some-password",
            idamAuthorizor,
            accessTokenDecoder);

    @Test
    public void correctly_delegates_to_idam_authorizer_to_to_access_token() {

        when(idamAuthorizor.exchangeForAccessToken("some-user", "some-password"))
                .thenReturn("expected-token");

        String accessToken = underTest.getAccessToken();

        verify(idamAuthorizor).exchangeForAccessToken(
                username.capture(),
                password.capture());

        assertThat(username.getValue())
                .isEqualTo(username.getValue());

        assertThat(password.getValue())
                .isEqualTo(password.getValue());

        assertThat(accessToken)
                .isEqualTo("expected-token");
    }

    @Test
    public void correctly_delegates_to_idam_authorizer_to_to_access_user_id() {

        when(idamAuthorizor.exchangeForAccessToken("some-user", "some-password"))
                .thenReturn("expected-token");

        when(accessTokenDecoder.decode("expected-token"))
                .thenReturn(singletonMap("id", "expected-id"));

        String id = underTest.getId();

        assertThat(id).isEqualTo("expected-id");
    }
}