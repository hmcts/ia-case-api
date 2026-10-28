package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CommonRefDataDynamicListProvider provider;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    private RecordAttendeesAndDurationMidEventHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handler = new RecordAttendeesAndDurationMidEventHandler(provider, locationRefDataService, iaHearingsApiService);
    }

    @Test
    void should_return_case_data_unchanged() {
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
        verifyNoInteractions(provider, locationRefDataService, iaHearingsApiService);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == MID_EVENT && event == RECORD_ATTENDEES_AND_DURATION) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
