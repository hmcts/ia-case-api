package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateInterpreterBookingStatusHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private IaHearingsApiService iaHearingsApiService;
    private UpdateInterpreterBookingStatusHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateInterpreterBookingStatusHandler(iaHearingsApiService);
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void testCanHandle() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        boolean canHandle = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

    @Test
    public void testHandleInterpreterDetails() {

        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(iaHearingsApiService, times(1)).aboutToSubmit(callback);
    }

    @Test
    void should_throw_an_exception_when_hearing_service_is_not_working() {
        when(iaHearingsApiService.aboutToSubmit(callback))
                .thenThrow(new IllegalStateException("hearingService is down"));
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback);
        assertNotNull(response);
        verify(iaHearingsApiService, times(1)).aboutToSubmit(callback);

        assertEquals(Set.of("Hearing cannot be auto updated for Case 0"), response.getErrors());
    }

}