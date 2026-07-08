package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AddStatutoryTimeframe24WeeksHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase updatedAsylumCase;
    @Mock
    private UpdateStatutoryTimeframe24WeeksService updateStatutoryTimeframe24WeeksService;

    @InjectMocks
    private AddStatutoryTimeframe24WeeksHandler addStatutoryTimeframe24WeeksHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @CsvSource({"AIP,NO", "REP,YES"})
    void should_append_new_statutory_timeframe_24_weeks_to_existing_statutory_timeframe_24_weeks(JourneyType journeyType, YesOrNo isVirtualHearing) {
        when(updateStatutoryTimeframe24WeeksService.updateAsylumCase(any(AsylumCase.class), any(YesOrNo.class)))
            .thenReturn(updatedAsylumCase);
        when(updatedAsylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse.getData()).isEqualTo(updatedAsylumCase);
        verify(updateStatutoryTimeframe24WeeksService).updateAsylumCase(asylumCase, YesOrNo.YES);
        verify(updatedAsylumCase).write(AsylumCaseFieldDefinition.IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.NO);
        verify(updatedAsylumCase).write(AsylumCaseFieldDefinition.IS_VIRTUAL_HEARING, isVirtualHearing);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = addStatutoryTimeframe24WeeksHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS)) {
                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
