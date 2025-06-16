package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IdamServiceTest {
    @Mock
    private IdamApi idamApi;

    private IdamService idamService;

    private final String systemUserName = "systemUserName";
    private final String systemUserPass = "systemUserPass";
    private final String idamRedirectUrl = "http://idamRedirectUrl";
    private final String systemUserScope = "systemUserScope";
    private final String idamClientId = "idamClientId";
    private final String idamClientSecret = "idamClientSecret";

    @Test
    void getUserDetails() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        idamService = new IdamService(systemUserName,
                                      systemUserPass,
                                      idamRedirectUrl,
                                      systemUserScope,
                                      idamClientId,
                                      idamClientSecret,
                                      idamApi);

        UserInfo expectedUserInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expectedUserInfo);

        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @Test
    void getUserToken() {
        String expectedAccessToken = "ABCDEFG";
        String expectedScope = "systemUserScope";

        idamService = new IdamService(systemUserName,
                                      systemUserPass,
                                      idamRedirectUrl,
                                      systemUserScope,
                                      idamClientId,
                                      idamClientSecret,
                                      idamApi);
        Token expectedToken = new Token(expectedAccessToken, expectedScope);
        when(idamApi.token(anyMap())).thenReturn(expectedToken);

        Token actualUserToken = idamService.getServiceUserToken();
        ArgumentCaptor<ConcurrentHashMap<String, String>> requestFormCaptor =
            ArgumentCaptor.forClass(ConcurrentHashMap.class);
        verify(idamApi).token(requestFormCaptor.capture());

        assertEquals(expectedAccessToken, actualUserToken.getAccessToken());
        assertEquals(expectedScope, actualUserToken.getScope());

        Map<String, ?> actualRequestEntity = requestFormCaptor.getValue();

        assertEquals("password", actualRequestEntity.get("grant_type"));
        assertEquals(idamRedirectUrl, actualRequestEntity.get("redirect_uri"));
        assertEquals(idamClientId, actualRequestEntity.get("client_id"));
        assertEquals(idamClientSecret, actualRequestEntity.get("client_secret"));
        assertEquals(systemUserName, actualRequestEntity.get("username"));
        assertEquals(systemUserPass, actualRequestEntity.get("password"));
        assertEquals(systemUserScope, actualRequestEntity.get("scope"));

    }
}
