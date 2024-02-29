package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
class IdamServiceTest {

    public static final String SOME_SYSTEM_USER = "some system user";
    public static final String SYSTEM_USER_PASS = "some system user password";
    public static final String REDIRECT_URL = "some redirect url";
    public static final String SCOPE = "some scope";
    public static final String CLIENT_ID = "some client id";
    public static final String CLIENT_SECRET = "some client secret";

    @Mock
    private IdamApi idamApi;

    private IdamService idamService;

    @BeforeEach
    public void setUp() {

        idamService = new IdamService(
                SOME_SYSTEM_USER,
                SYSTEM_USER_PASS,
                REDIRECT_URL,
                SCOPE,
                CLIENT_ID,
                CLIENT_SECRET,
                idamApi
        );
    }

    @Test
    void getUserToken() {


        when(idamApi.token(anyMap())).thenReturn(new Token("some user token", SCOPE));

        String actual = idamService.getServiceUserToken();

        assertThat(actual).isEqualTo("Bearer some user token");

        Map<String, String> expectedIdamApiParameter = new ConcurrentHashMap<>();
        expectedIdamApiParameter.put("grant_type", "password");
        expectedIdamApiParameter.put("redirect_uri", REDIRECT_URL);
        expectedIdamApiParameter.put("client_id", CLIENT_ID);
        expectedIdamApiParameter.put("client_secret", CLIENT_SECRET);
        expectedIdamApiParameter.put("username", SOME_SYSTEM_USER);
        expectedIdamApiParameter.put("password", SYSTEM_USER_PASS);
        expectedIdamApiParameter.put("scope", SCOPE);

        verify(idamApi).token(eq(expectedIdamApiParameter));
    }

    @Test
    void getUserDetails() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
                expectedEmailAddress,
                expectedId,
                expectedRoles,
                expectedName,
                expectedForename,
                expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);

        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }
}
