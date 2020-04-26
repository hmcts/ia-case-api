package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(JUnitParamsRunner.class)
public class MinorTagHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;

    private final MinorTagHandler minorTagHandler = new MinorTagHandler();
    public static final String APPELLANT_ADULT = LocalDate.of(1979, 2, 1).toString();
    public static final String APPELLANT_MINOR = LocalDate.now().toString();

    @Test
    @Parameters(method = "generateCanHandleTestScenario")
    public void it_can_handle_callback(CanHandleTestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean canHandleActual = minorTagHandler.canHandle(scenario.callbackStage, callback);

        assertThat(canHandleActual).isEqualTo(scenario.canHandledExpected);
    }

    private List<CanHandleTestScenario> generateCanHandleTestScenario() {
        return CanHandleTestScenario.builder();
    }

    @Value
    private static class CanHandleTestScenario {
        PreSubmitCallbackStage callbackStage;
        Event event;
        boolean canHandledExpected;

        private static List<CanHandleTestScenario> builder() {
            List<CanHandleTestScenario> scenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.SUBMIT_APPEAL)) {
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_SUBMIT, e, true));
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_START, e, false));
                    scenarios.add(new CanHandleTestScenario(MID_EVENT, e, false));
                } else {
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_SUBMIT, e, false));
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_START, e, false));
                    scenarios.add(new CanHandleTestScenario(MID_EVENT, e, false));
                }
            }
            return scenarios;
        }
    }

    @Test
    public void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> minorTagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void handle() {
    }
}