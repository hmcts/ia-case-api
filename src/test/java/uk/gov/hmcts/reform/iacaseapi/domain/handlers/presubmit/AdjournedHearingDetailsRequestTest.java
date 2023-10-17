package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdjournedHearingDetailsRequestTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCaseCallbackApiDelegator apiDelegator;

    private final String hearingsApiEndpoint = "hearings-end-point";
    private final String aboutToStartPath = "/about-to-start";
    private final String midEventPath = "/mid-event";

    AdjournedHearingDetailsRequest request;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        request = new AdjournedHearingDetailsRequest(
            apiDelegator,
            hearingsApiEndpoint,
            aboutToStartPath,
            midEventPath
        );
    }

    @Test
    void ifCanNotHandleEvent_whenTryingToHandle_throwIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> request.handle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void givenHearingDetailsNull_thenDelegateToHearingsApiStartEvent() {
        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING)).thenReturn(Optional.empty());
        when(apiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = request.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(apiDelegator, times(1)).delegate(callback, hearingsApiEndpoint + aboutToStartPath);
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    void givenHearingDetailsNotNull_thenDelegateToHearingsApiMidEvent() {
        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING)).thenReturn(Optional.of(new DynamicList("hearing 1")));

        when(apiDelegator.delegate(callback, hearingsApiEndpoint + midEventPath)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = request.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        verify(apiDelegator, times(1)).delegate(callback, hearingsApiEndpoint + midEventPath);
        assertEquals(asylumCase, callbackResponse.getData());
    }
}

