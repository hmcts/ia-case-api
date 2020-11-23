package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.unlinkappeal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
public class UnlinkAppealPreparerHandlerTest {

    private final UnlinkAppealPreparerHandler unlinkAppealPreparerHandler = new UnlinkAppealPreparerHandler();
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = unlinkAppealPreparerHandler.canHandle(scenario.callbackStage, callback);

        Assertions.assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private static List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Test
    public void handle() {
        when(callback.getEvent()).thenReturn(Event.UNLINK_APPEAL);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(new AsylumCase());

        PreSubmitCallbackResponse<AsylumCase> actualResponse =
            unlinkAppealPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(callback, times(1)).getCaseDetails();
        verify(caseDetails, times(1)).getCaseData();

        assertEquals(1, actualResponse.getErrors().size());
        assertTrue(actualResponse.getErrors().contains("This appeal is not linked and so cannot be unlinked"));
    }

    @Test
    public void should_throw_exception() {

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Value
    private static class CanHandleScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean canHandleExpectedResult;

        public static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (PreSubmitCallbackStage cb : PreSubmitCallbackStage.values()) {
                for (Event e : Event.values()) {
                    if (Event.UNLINK_APPEAL.equals(e)
                        && PreSubmitCallbackStage.ABOUT_TO_START.equals(cb)) {
                        scenarios.add(new CanHandleScenario(e, cb, true));
                    } else {
                        scenarios.add(new CanHandleScenario(e, cb, false));
                    }
                }
            }
            return scenarios;
        }
    }

}
