package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CcdDataServiceTest {
    @Mock
    private CcdDataApi ccdDataApi;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator serviceAuthorization;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> startEventCaseDetails;
    @Mock
    private AsylumCase startEventAsylumCase;

    private final String token = "token";
    private final String serviceToken = "Bearer serviceToken";
    private final String userId = "userId";
    private final String eventToken = "eventToken";
    private final long caseId = 1234123412341234L;
    private final String caseReference = String.valueOf(caseId);
    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";
    private final String eventId = Event.RE_TRIGGER_WA_TASKS.toString();
    private final UserInfo userInfo = new UserInfo(
        "email",
        userId,
        Collections.emptyList(),
        "name",
        "givenName",
        "familyName");

    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {

        ccdDataService =
            new CcdDataService(ccdDataApi, idamService, serviceAuthorization);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getJurisdiction()).thenReturn(jurisdiction);

        when(startEventCaseDetails.getCaseData()).thenReturn(startEventAsylumCase);
        when(idamService.getServiceUserToken()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(idamService.getUserInfo(token)).thenReturn(userInfo);
    }

    @Test
    void service_should_throw_on_unable_to_generate_system_user_token() {
        when(idamService.getServiceUserToken()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.retriggerWaTasks(caseReference));
    }

    @Test
    void service_should_throw_on_unable_to_generate_s2s_token() {
        when(serviceAuthorization.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.retriggerWaTasks(caseReference));
    }

    @Test
    void service_should_throw_on_unable_to_get_system_user_uid() {
        when(idamService.getUserInfo(token)).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.retriggerWaTasks(caseReference));
    }

    @Test
    void service_should_trigger_wa_task_reconfig() {
        StartEventDetails startEventResponse = getStartEventResponse();
        when(ccdDataApi.startEvent(
            token, serviceToken, userId, jurisdiction, caseType,
            caseReference, eventId)).thenReturn(startEventResponse);

        // set CaseData for the subEvent request
        CaseDataContent caseDataContent = getCaseDataContent();
        when(ccdDataApi.submitEvent(token, serviceToken, caseReference,
            caseDataContent)).thenReturn(getSubmitEventResponse());

        SubmitEventDetails submitEventDetails =
            ccdDataService.retriggerWaTasks(caseReference);

        assertNotNull(submitEventDetails);
        assertEquals(caseId, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals("CALLBACK_COMPLETED", submitEventDetails.getCallbackResponseStatus());

        verify(ccdDataApi, times(1))
            .startEvent(token, serviceToken, userId,
                jurisdiction, caseType, caseReference, eventId);
        verify(ccdDataApi, times(1))
            .submitEvent(token, serviceToken, caseReference, caseDataContent);
    }


    @Test
    void service_should_error_on_invalid_ccd_case_reference() {
        when(
            ccdDataApi.startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, caseReference, eventId)).thenThrow(FeignException.class);

        assertThatThrownBy(() -> ccdDataService.retriggerWaTasks(caseReference))
            .isExactlyInstanceOf(FeignException.class);

    }

    private StartEventDetails getStartEventResponse() {
        return new StartEventDetails(Event.RE_TRIGGER_WA_TASKS, eventToken, startEventCaseDetails);
    }

    private SubmitEventDetails getSubmitEventResponse() {
        return new SubmitEventDetails(caseId, jurisdiction, State.APPEAL_SUBMITTED, new HashMap<>(),
            200, "CALLBACK_COMPLETED");
    }

    private CaseDataContent getCaseDataContent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.RE_TRIGGER_WA_TASKS.toString());

        return new CaseDataContent(caseReference, new HashMap<>(),
            eventData, eventToken, true);
    }
}
