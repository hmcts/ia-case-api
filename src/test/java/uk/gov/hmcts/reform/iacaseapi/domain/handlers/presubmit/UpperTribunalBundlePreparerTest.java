package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpperTribunalBundlePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private UpperTribunalBundlePreparer upperTribunalBundlePreparer;

    @BeforeEach
    public void setup() {
        upperTribunalBundlePreparer = new UpperTribunalBundlePreparer();
    }

    @Test
    void should_throw_error_for_reheard_rule_35_respondent_decision() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPPER_TRIBUNAL_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.REHEARD_RULE35.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            upperTribunalBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot generate an Upper Tribunal bundle because this appeal will not be heard by the Upper Tribunal."));
    }

    @Test
    void should_throw_error_for_remade_rule_32_respondent_decision() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPPER_TRIBUNAL_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.REMADE_RULE32.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            upperTribunalBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot generate an Upper Tribunal bundle because this appeal will not be heard by the Upper Tribunal."));
    }

    @Test
    void should_throw_error_for_reheard_rule_35_appellant_decision() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPPER_TRIBUNAL_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.REHEARD_RULE35.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            upperTribunalBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot generate an Upper Tribunal bundle because this appeal will not be heard by the Upper Tribunal."));
    }

    @Test
    void should_throw_error_for_remade_rule_32_appellant_decision() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPPER_TRIBUNAL_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.REMADE_RULE32.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            upperTribunalBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot generate an Upper Tribunal bundle because this appeal will not be heard by the Upper Tribunal."));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> upperTribunalBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> upperTribunalBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = upperTribunalBundlePreparer.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_UPPER_TRIBUNAL_BUNDLE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
