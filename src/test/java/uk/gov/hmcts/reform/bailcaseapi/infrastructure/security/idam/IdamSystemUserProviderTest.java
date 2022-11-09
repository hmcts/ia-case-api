package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
class IdamSystemUserProviderTest {

    @Mock
    private IdamApi idamApi;

    @Mock
    private UserInfo userInfo;

    private String token = "Bearer someHash";

    @Test
    void should_return_correct_user_id() {

        String expectedUserId = "someUserID";
        when(userInfo.getUid()).thenReturn(expectedUserId);
        when(idamApi.userInfo(token)).thenReturn(userInfo);

        IdamSystemUserProvider idamSystemUserProvider = new IdamSystemUserProvider(idamApi);

        String userId = idamSystemUserProvider.getSystemUserId(token);

        assertEquals(expectedUserId, userId);

        verify(idamApi).userInfo(token);
    }

    @Test
    void should_throw_exception_when_auth_service_unavailable() {

        when(idamApi.userInfo(token)).thenThrow(FeignException.class);

        IdamSystemUserProvider idamSystemUserProvider = new IdamSystemUserProvider(idamApi);

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            () -> idamSystemUserProvider.getSystemUserId(token)
        );
        assertEquals("Could not get system user id from IDAM", thrown.getMessage());
    }
}

