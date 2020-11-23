package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
public class CaseSubmittedConfirmationTest {

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
            callbackResponse.getConfirmationHeader().get())
            .contains("You have submitted your case");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The case officer will now review your appeal");
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    public void it_can_handle_callback(EventScenarios event) {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", event.isFlag());
        when(callback.getEvent()).thenReturn(event.getEvent());

        boolean canHandle = caseSubmittedConfirmation.canHandle(callback);

        Assertions.assertThat(canHandle).isEqualTo(event.isExpected());
    }

    private static List<EventScenarios> generateDifferentEventScenarios() {
        return EventScenarios.builder();
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
}
