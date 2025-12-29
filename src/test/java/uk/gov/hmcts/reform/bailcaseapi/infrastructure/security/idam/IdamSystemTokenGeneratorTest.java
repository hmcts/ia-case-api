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
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamSystemTokenGenerator;

@ExtendWith(MockitoExtension.class)
class IdamSystemTokenGeneratorTest {

    @Mock
    private IdamService idamService;

    @Mock
    private Token token;

    @Test
    void should_return_correct_token_from_idam() {
        String expectedToken = "systemUserTokenHash";
        when(token.getAccessToken()).thenReturn(expectedToken);
        when(idamService.getServiceUserToken()).thenReturn(token);

        IdamSystemTokenGenerator idamSystemTokenGenerator =
            new IdamSystemTokenGenerator(idamService);
        String idamToken = idamSystemTokenGenerator.generate();

        verify(idamService).getServiceUserToken();
        assertEquals(expectedToken, idamToken);
    }

    @Test
    void should_throw_exception_when_auth_service_unavailable() {

        when(idamService.getServiceUserToken()).thenThrow(FeignException.class);

        IdamSystemTokenGenerator idamSystemTokenGenerator = new IdamSystemTokenGenerator(idamService);

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            idamSystemTokenGenerator::generate
        );
        assertEquals("Could not get system user token from IDAM", thrown.getMessage());
    }
}
