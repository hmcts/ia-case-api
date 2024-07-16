package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

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
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

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

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_if_aria_desired_state_is_not_present() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("ariaDesiredState is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_change_state_value_from_desired_state() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(PRE_HEARING));

        PreSubmitCallbackResponse<AsylumCase> response =
            progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse);

        assertEquals(State.PRE_HEARING, response.getState());
    }

}