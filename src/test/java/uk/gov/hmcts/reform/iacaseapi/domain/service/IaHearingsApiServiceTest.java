package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

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

    IaHearingsApiService iaHearingsApiService;

    @BeforeEach
    void setup() {
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
        verify(asylumCaseCallbackApiDelegator).delegate(callback, IA_HEARINGS_API_URL + ABOUT_TO_SUBMIT_PATH);
    }
}
