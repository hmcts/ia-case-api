package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class IaHearingsApiServiceTest {

    private static final String IA_HEARINGS_API_URL = "http://127.0.0.1";
    private static final String ABOUT_TO_START_PATH = "/ccdAboutToStart";
    private static final String MID_EVENT_PATH = "/ccdMidEvent";
    private static final String ABOUT_TO_SUBMIT_PATH = "/ccdAboutToSubmit";

    @Mock
    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    IaHearingsApiService iaHearingsApiService;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        iaHearingsApiService = new IaHearingsApiService(
            asylumCaseCallbackApiDelegator,
            IA_HEARINGS_API_URL,
            ABOUT_TO_START_PATH,
            MID_EVENT_PATH,
            ABOUT_TO_SUBMIT_PATH
        );
    }

    @Test
    void should_delegate_aboutToStart_call() {
        iaHearingsApiService.aboutToStart(callback);
        verify(asylumCaseCallbackApiDelegator).delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_START_PATH);
    }

    @Test
    void should_delegate_midEvent_call() {
        iaHearingsApiService.midEvent(callback);
        verify(asylumCaseCallbackApiDelegator).delegate(callback, IA_HEARINGS_API_URL + MID_EVENT_PATH);
    }

    @Test
    void should_delegate_aboutToSubmit_call() {
        iaHearingsApiService.aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
    }

    @Test
    void createHearing_should_successfully_call_hearings_service() {
        when(asylumCaseCallbackApiDelegator
            .delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH)).thenReturn(asylumCase);

        iaHearingsApiService.createHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CREATE_HEARING_REQUIRED, NO);
    }

    @Test
    void createHearing_should_set_request_status_when_call_fails() {
        when(callback.getEvent()).thenReturn(REVIEW_HEARING_REQUIREMENTS);
        doThrow(AsylumCaseServiceResponseException.class)
            .when(asylumCaseCallbackApiDelegator).delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH);

        iaHearingsApiService.createHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CREATE_HEARING_REQUIRED, YES);
    }

    @Test
    void updateHearing_should_successfully_call_hearings_service() {
        when(asylumCaseCallbackApiDelegator
            .delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH)).thenReturn(asylumCase);

        iaHearingsApiService.updateHearing(callback);

        verify(asylumCase, times(1)).write(UPDATE_HMC_REQUEST_SUCCESS, YES);
    }

    @Test
    void updateHearing_should_set_request_status_when_call_fails() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        doThrow(AsylumCaseServiceResponseException.class)
            .when(asylumCaseCallbackApiDelegator).delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH);

        iaHearingsApiService.updateHearing(callback);

        verify(asylumCase, times(1)).write(UPDATE_HMC_REQUEST_SUCCESS, NO);
        verify(asylumCase, times(1)).write(MANUAL_UPDATE_HEARING_REQUIRED, YES);
    }

    @Test
    void deleteHearing_should_successfully_call_hearings_service() {
        when(asylumCaseCallbackApiDelegator
            .delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH)).thenReturn(asylumCase);

        iaHearingsApiService.deleteHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CANCEL_HEARINGS_REQUIRED, NO);
    }

    @Test
    void deleteHearing_should_set_request_status_when_call_fails() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        doThrow(AsylumCaseServiceResponseException.class)
            .when(asylumCaseCallbackApiDelegator).delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH);

        iaHearingsApiService.deleteHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
    }
}
