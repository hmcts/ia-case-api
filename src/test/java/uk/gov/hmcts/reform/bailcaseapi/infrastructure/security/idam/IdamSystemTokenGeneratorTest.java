package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.Token;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IdamSystemTokenGeneratorTest {

    @Mock
    private IdamApi idamApi;

    @Mock
    private Token token;

    private String systemUserName = "systemUserName";
    private String systemUserPass = "systemUserPass";
    private String idamRedirectUrl = "http://idamRedirectUrl";
    private String systemUserScope = "systemUserScope";
    private String idamClientId = "idamClientId";
    private String idamClientSecret = "idamClientSecret";

    @Test
    void should_return_correct_token_from_idam() {

        String expectedToken = "systemUserTokenHash";

        when(token.getAccessToken()).thenReturn(expectedToken);
        when(idamApi.token(any(Map.class))).thenReturn(token);

        IdamSystemTokenGenerator idamSystemTokenGenerator = new IdamSystemTokenGenerator(
            systemUserName,
            systemUserPass,
            idamRedirectUrl,
            systemUserScope,
            idamClientId,
            idamClientSecret,
            idamApi
        );

        String token = idamSystemTokenGenerator.generate();

        assertEquals(expectedToken, token);

        ArgumentCaptor<Map<String, ?>> requestFormCaptor = ArgumentCaptor.forClass(Map.class);
        verify(idamApi).token(requestFormCaptor.capture());

        Map<String, ?> actualRequestEntity = requestFormCaptor.getValue();

        assertEquals(newArrayList("password"), actualRequestEntity.get("grant_type"));
        assertEquals(newArrayList(idamRedirectUrl), actualRequestEntity.get("redirect_uri"));
        assertEquals(newArrayList(idamClientId), actualRequestEntity.get("client_id"));
        assertEquals(newArrayList(idamClientSecret), actualRequestEntity.get("client_secret"));
        assertEquals(newArrayList(systemUserName), actualRequestEntity.get("username"));
        assertEquals(newArrayList(systemUserPass), actualRequestEntity.get("password"));
        assertEquals(newArrayList(systemUserScope), actualRequestEntity.get("scope"));

    }

    @Test
    void should_throw_exception_when_auth_service_unavailable() {

        when(idamApi.token(any(Map.class))).thenThrow(FeignException.class);

        IdamSystemTokenGenerator idamSystemTokenGenerator = new IdamSystemTokenGenerator(
            systemUserName,
            systemUserPass,
            idamRedirectUrl,
            systemUserScope,
            idamClientId,
            idamClientSecret,
            idamApi
        );

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            idamSystemTokenGenerator::generate
        );
        assertEquals("Could not get system user token from IDAM", thrown.getMessage());
    }
}
