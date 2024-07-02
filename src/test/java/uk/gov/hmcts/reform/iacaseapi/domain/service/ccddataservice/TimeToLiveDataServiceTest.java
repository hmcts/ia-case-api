package uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TimeToLiveDataServiceTest {

    @Mock
    private CcdDataApi ccdDataApi;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator serviceAuthorization;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private TTL ttl;
    @Mock
    private StartEventDetails startEventDetails;
    @Mock
    private SubmitEventDetails submitEventDetails;

    private static final String USER_TOKEN = "userToken";
    private static final String S2S_TOKEN = "s2sToken";
    private static final String UID = "uid";
    private static final String CASE_TYPE = "Asylum";
    private static final String JURISDICTION = "IA";
    private static final String EVENT_TOKEN = "eventToken";
    private static final long CASE_ID = 1;
    private static final int ONE_HUNDRED_YEARS = 36524;


    private TimeToLiveDataService timeToLiveDataService;

    @BeforeEach
    void setup() {
        timeToLiveDataService = new TimeToLiveDataService(ccdDataApi, idamService, serviceAuthorization);
    }

    @Test
    void should_update_the_clock() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);
        when(idamService.getSystemUserId(USER_TOKEN)).thenReturn(UID);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));

        when(ccdDataApi.startEvent(USER_TOKEN, S2S_TOKEN, UID, JURISDICTION, CASE_TYPE, "1", Event.MANAGE_CASE_TTL.toString()))
            .thenReturn(startEventDetails);

        when(startEventDetails.getToken()).thenReturn(EVENT_TOKEN);
        when(startEventDetails.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        CaseDataContent submitRequest = new CaseDataContent(
            "1",
            Map.of("TTL", ttl),
            Map.of("id", Event.MANAGE_CASE_TTL.toString()),
            EVENT_TOKEN,
            true);

        ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

        when(ccdDataApi.submitEvent(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq("1"),
            caseDataContentArgumentCaptor.capture())).thenReturn(submitEventDetails);

        assertEquals(submitEventDetails, timeToLiveDataService.updateTheClock(callback, ONE_HUNDRED_YEARS));
        assertEquals(submitRequest, caseDataContentArgumentCaptor.getValue());

        verify(ccdDataApi, times(1)).submitEvent(USER_TOKEN, S2S_TOKEN, "1", caseDataContentArgumentCaptor.getValue());
    }
}
