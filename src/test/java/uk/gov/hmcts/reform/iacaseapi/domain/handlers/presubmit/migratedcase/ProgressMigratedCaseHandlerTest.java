package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE_SELECTED_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DESIRED_STATE_CORRECT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PROGRESS_MIGRATED_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ProgressMigratedCaseHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ProgressMigratedCaseHandler progressMigratedCaseHandler;

    @BeforeEach
    public void setUp() {
        progressMigratedCaseHandler = new ProgressMigratedCaseHandler();
        when(callback.getEvent()).thenReturn(PROGRESS_MIGRATED_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = progressMigratedCaseHandler.canHandle(callbackStage, callback);

                if (callback.getEvent() == Event.PROGRESS_MIGRATED_CASE && callbackStage == ABOUT_TO_START) {
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
        assertThatThrownBy(() -> progressMigratedCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_if_desired_state_correct_is_not_present() {
        when(asylumCase.read(DESIRED_STATE_CORRECT, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("desiredStateCorrect is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_aria_desired_state_is_not_present() {
        when(asylumCase.read(DESIRED_STATE_CORRECT, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("desiredStateCorrect is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_change_the_desired_state_selected_value() {
        when(asylumCase.read(ARIA_DESIRED_STATE_SELECTED_VALUE, String.class)).thenReturn(Optional.of("Listing"));
        when(asylumCase.read(DESIRED_STATE_CORRECT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(PRE_HEARING));

        progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).write(ARIA_DESIRED_STATE_SELECTED_VALUE, "Pre hearing");
    }

    @Test
    void should_not_change_the_desired_state_selected_value() {
        when(asylumCase.read(ARIA_DESIRED_STATE_SELECTED_VALUE, String.class)).thenReturn(Optional.of("Listing"));
        when(asylumCase.read(DESIRED_STATE_CORRECT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(0)).write(ARIA_DESIRED_STATE_SELECTED_VALUE, "Listing");
    }

}