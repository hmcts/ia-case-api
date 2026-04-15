package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AddStatutoryTimeframe24WeeksPreStartHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @InjectMocks private AddStatutoryTimeframe24WeeksPreStartHandler addStatutoryTimeframe24WeeksPreStartHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(callback.getEvent()).thenReturn(Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE))
            .thenReturn(Optional.of(LocalDate.of(2026, 6, 1).toString()));
    }

    @Test
    void should_return_error_when_submission_is_before_live_date() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE))
            .thenReturn(Optional.of(LocalDate.of(2026, 4, 1).toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains("This event cannot be run on a case created before 01/05/2026"));
    }

    @Test
    void should_not_return_error_when_submission_is_after_live_date() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_return_error_when_tribunal_received_date_is_before_live_date() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.TRIBUNAL_RECEIVED_DATE))
            .thenReturn(Optional.of(LocalDate.of(2026, 4, 1).toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains("This event cannot be run on a case created before 01/05/2026"));
    }

    @Test
    void should_not_return_error_when_tribunal_received_date_is_after_live_date() {
        when(asylumCase.read(AsylumCaseFieldDefinition.TRIBUNAL_RECEIVED_DATE))
            .thenReturn(Optional.of(LocalDate.of(2026, 6, 1).toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_return_error_when_appellant_in_detention() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains("This event cannot be run on a detained case"));
    }

    @Test
    void should_not_return_error_when_appellant_in_not_detention() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE))
            .thenReturn(Optional.of(LocalDate.of(2026, 6, 1).toString()));
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_return_error_when_appeal_out_of_country() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains("This event cannot be run on an out of country case"));
    }

    @Test
    void should_not_return_error_when_appeal_in_country() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_return_error_when_state_is_unsupported() {
        when(callback.getCaseDetails().getState()).thenReturn(State.CASE_BUILDING);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains("This event cannot be run on this case"));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = addStatutoryTimeframe24WeeksPreStartHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START && event.equals(Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS)) {
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
        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksPreStartHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksPreStartHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksPreStartHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addStatutoryTimeframe24WeeksPreStartHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
