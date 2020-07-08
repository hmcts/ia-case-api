package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(JUnitParamsRunner.class)
public class CaseSubmittedConfirmationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private Callback<AsylumCase> callback;

    private CaseSubmittedConfirmation caseSubmittedConfirmation =
        new CaseSubmittedConfirmation();

    @Test
    public void should_return_confirmation() {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", true);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);

        PostSubmitCallbackResponse callbackResponse =
            caseSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You have submitted your case")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The case officer will now review your appeal")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @Parameters(method = "generateDifferentEventScenarios")
    public void it_can_handle_callback(EventScenarios event) {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", event.isFlag());
        when(callback.getEvent()).thenReturn(event.getEvent());

        boolean canHandle = caseSubmittedConfirmation.canHandle(callback);

        Assertions.assertThat(canHandle).isEqualTo(event.isExpected());
    }

    private List<EventScenarios> generateDifferentEventScenarios() {
        return EventScenarios.builder();
    }

    @Value
    private static class EventScenarios {
        Event event;
        boolean flag;
        boolean expected;

        private static List<EventScenarios> builder() {
            List<EventScenarios> testScenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.BUILD_CASE)) {
                    testScenarios.add(new EventScenarios(e, true, false));
                    testScenarios.add(new EventScenarios(e, false, true));
                } else if (e.equals(Event.SUBMIT_CASE)) {
                    testScenarios.add(new EventScenarios(e, true, true));
                    testScenarios.add(new EventScenarios(e, false, false));
                } else {
                    testScenarios.add(new EventScenarios(e, true, false));
                    testScenarios.add(new EventScenarios(e, false, false));
                }
            }
            return testScenarios;
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
