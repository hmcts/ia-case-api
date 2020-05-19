package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@RunWith(JUnitParamsRunner.class)
public class UpdateAppealReferenceNumberHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;
    @Mock
    private Callback<AsylumCase> callback;

    @InjectMocks
    private UpdateAppealReferenceNumberHandler handler;

    @Test
    @Parameters(method = "generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.getEvent());

        boolean result = handler.canHandle(scenario.getCallbackStage(), callback);

        Assertions.assertThat(result).isEqualTo(scenario.isCanHandleExpectedResult());
    }

    public List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Value
    private static class CanHandleScenario {
        PreSubmitCallbackStage callbackStage;
        Event event;
        boolean canHandleExpectedResult;

        private static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (Event event : Event.values()) {
                if (event.equals(Event.EDIT_APPEAL_AFTER_SUBMIT)) {
                    scenarios.add(new CanHandleScenario(ABOUT_TO_SUBMIT, event, true));
                    scenarios.add(new CanHandleScenario(ABOUT_TO_START, event, false));
                } else {
                    scenarios.add(new CanHandleScenario(ABOUT_TO_START, event, false));
                    scenarios.add(new CanHandleScenario(ABOUT_TO_SUBMIT, event, false));
                }
            }
            return scenarios;
        }

    }

    @Test
    public void given_null_args_can_handle_should_throw_exception() {
        Assertions.assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void handle() {
    }
}