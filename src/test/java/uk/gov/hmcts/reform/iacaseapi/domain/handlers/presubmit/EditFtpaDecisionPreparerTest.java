package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_FTPA_CURRENT_DECISION_TEXT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CurrentFtpaDecisionResolver;

@ExtendWith(MockitoExtension.class)
class EditFtpaDecisionPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CurrentFtpaDecisionResolver resolver;

    private EditFtpaDecisionPreparer preparer;

    @BeforeEach
    void setUp() {
        preparer = new EditFtpaDecisionPreparer(resolver);
    }

    private void givenValidCallback() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_FTPA_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_write_current_decision_label() {
        givenValidCallback();
        when(resolver.getApplicantType(asylumCase)).thenReturn(Optional.of("appellant"));
        when(resolver.isDecided(asylumCase, "appellant")).thenReturn(true);
        when(resolver.getCurrentOutcome(asylumCase, "appellant")).thenReturn(Optional.of("granted"));

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(response.getErrors()).isEmpty();
        verify(resolver).clearUpdateFtpaDecisionFields(asylumCase);
        verify(asylumCase).write(UPDATE_FTPA_CURRENT_DECISION_TEXT, "Permission granted");
    }

    @Test
    void should_error_when_no_ftpa_decision() {
        givenValidCallback();
        when(resolver.getApplicantType(asylumCase)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(response.getErrors()).containsExactly(EditFtpaDecisionPreparer.NO_FTPA_DECISION_ERROR);
        verify(resolver, never()).clearUpdateFtpaDecisionFields(asylumCase);
    }

    @Test
    void should_only_handle_about_to_start_update_ftpa_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                assertThat(preparer.canHandle(stage, callback)).isEqualTo(
                    stage == PreSubmitCallbackStage.ABOUT_TO_START && event == Event.UPDATE_FTPA_DECISION);
            }
        }
    }

    @Test
    void should_throw_if_cannot_handle() {
        assertThatThrownBy(() -> preparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> preparer.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> preparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
