package uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CcdDataServiceTest {

    @Mock
    private CcdDataApi ccdDataApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator serviceAuthorization;

    @InjectMocks
    private CcdDataService ccdDataService;

    private final String userToken = "testUserToken";
    private final String s2sToken = "testS2sToken";
    private final String uid = "testUid";
    private final String caseId = "testCaseId";
    private final Event event = Event.MANAGE_CASE_TTL;

    @BeforeEach
    void setUp() {
        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);
        when(idamService.getSystemUserId(anyString())).thenReturn(uid);
    }

    @Test
    void shouldAuthorizeSuccessfully() {
        ccdDataService.authorize(event, caseId);

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(idamService).getSystemUserId(userToken);
    }

    @Test
    void shouldThrowIdentityManagerResponseExceptionWhenAuthorizationFails() {
        when(idamService.getServiceUserToken()).thenThrow(new IdentityManagerResponseException("Failed", new Throwable()));

        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.authorize(event, caseId));

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization, never()).generate();
        verify(idamService, never()).getSystemUserId(anyString());
    }

    @Test
    void shouldSubmitEventSuccessfully() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> eventData = new HashMap<>();
        String eventToken = "testEventToken";
        SubmitEventDetails submitEventDetails = new SubmitEventDetails();

        when(ccdDataApi.submitEvent(anyString(), anyString(), anyString(), any(CaseDataContent.class)))
            .thenReturn(submitEventDetails);

        SubmitEventDetails result = ccdDataService.submitEvent(userToken, s2sToken, caseId, data, eventData, eventToken);

        verify(ccdDataApi).submitEvent(eq(userToken), eq(s2sToken), eq(caseId), any(CaseDataContent.class));
        assertSame(submitEventDetails, result);
    }

    @Test
    void shouldStartEventSuccessfully() {
        StartEventDetails startEventDetails = new StartEventDetails();

        when(ccdDataApi.startEvent(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(startEventDetails);

        StartEventDetails result = ccdDataService.startEvent(userToken, s2sToken, uid, CcdDataService.CASE_TYPE, caseId, event);

        verify(ccdDataApi).startEvent(userToken, s2sToken, uid, CcdDataService.JURISDICTION, CcdDataService.CASE_TYPE, caseId, event.toString());
        assertSame(startEventDetails, result);
    }
}
