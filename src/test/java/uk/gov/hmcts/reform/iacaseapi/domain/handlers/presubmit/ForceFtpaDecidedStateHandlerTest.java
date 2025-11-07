package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ForceFtpaDecidedStateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ForceFtpaDecidedStateHandler forceFtpaDecidedStateHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.FORCE_FTPA_DECIDED_STATE);

        forceFtpaDecidedStateHandler = new ForceFtpaDecidedStateHandler();
    }

    @Test
    void should_handle_appellant_type_with_reheard_rule35_outcome() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("reheardRule35"));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DECIDED, "Yes");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "reheardRule35");
    }

    @Test
    void should_handle_appellant_type_with_non_reheard_rule35_outcome() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("someOtherOutcome"));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DECIDED, "Yes");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
    }

    @Test
    void should_handle_appellant_type_with_empty_outcome() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DECIDED, "Yes");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
    }

    @Test
    void should_handle_respondent_type_with_reheard_rule35_outcome() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("reheardRule35"));

        PreSubmitCallbackResponse<AsylumCase> response =
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DECIDED, "Yes");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "reheardRule35");
    }

    @Test
    void should_handle_respondent_type_with_non_reheard_rule35_outcome() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("someOtherOutcome"));

        PreSubmitCallbackResponse<AsylumCase> response =
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DECIDED, "Yes");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
    }

    @Test
    void should_handle_respondent_type_with_empty_outcome() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DECIDED, "Yes");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
    }

    @Test
    void should_throw_exception_for_missing_appellant_type() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantType is missing")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_for_unknown_appellant_type() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of("unknown"));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantType is is new and needs handling")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = forceFtpaDecidedStateHandler.canHandle(callbackStage, callback);

                if (event == Event.FORCE_FTPA_DECIDED_STATE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> forceFtpaDecidedStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceFtpaDecidedStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_exception_when_cannot_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() ->
            forceFtpaDecidedStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}