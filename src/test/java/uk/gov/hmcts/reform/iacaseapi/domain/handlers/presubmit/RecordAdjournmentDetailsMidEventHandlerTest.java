package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class RecordAdjournmentDetailsMidEventHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private RecordAdjournmentDetailsMidEventHandler handler;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        handler = new RecordAdjournmentDetailsMidEventHandler();

    }

    @Test
    void should_populate_hearing_values() {
        DynamicList hearingChannel = new DynamicList(new Value("INTER", "In Person"), null);
        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CHANNEL)).thenReturn(Optional.of(hearingChannel));
        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of("60"));
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(NEXT_HEARING_FORMAT, Optional.of(hearingChannel));
        verify(asylumCase, times(1)).write(NEXT_HEARING_DURATION, Optional.of("60"));
        verify(asylumCase, times(1)).write(NEXT_HEARING_LOCATION, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
    }

    @Test
    void should_not_populate_hearing_values() {
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(NEXT_HEARING_FORMAT, Optional.empty());
        verify(asylumCase, times(1)).write(NEXT_HEARING_DURATION, Optional.empty());
        verify(asylumCase, times(1)).write(NEXT_HEARING_LOCATION, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
    }



    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
                () -> handler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SEND_DIRECTION);
        assertThatThrownBy(
                () -> handler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (event.equals(RECORD_ADJOURNMENT_DETAILS)
                        && callbackStage == MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

}
