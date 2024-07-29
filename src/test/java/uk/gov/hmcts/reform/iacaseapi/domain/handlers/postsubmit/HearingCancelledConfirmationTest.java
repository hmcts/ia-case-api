package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import lombok.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingCancelledConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private HearingCancelledConfirmation handler = new HearingCancelledConfirmation();

    @ParameterizedTest
    @MethodSource("generateTestScenarios")
    void given_callback_can_handle(HearingCancelledConfirmationTest.TestScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.event);

        boolean actualResult = handler.canHandle(callback);

        assertThat(actualResult).isEqualTo(scenario.canBeHandledExpected);
    }

    private static List<HearingCancelledConfirmationTest.TestScenario> generateTestScenarios() {
        return HearingCancelledConfirmationTest.TestScenario.testScenarioBuilder();
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.HEARING_CANCELLED);

        PostSubmitCallbackResponse callbackResponse = handler.handle(callback);

        assertNotNull(callbackResponse);
        Assertions.assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        Assertions.assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Hearing details updated");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next\n\n"
                    + "Add new hearing information as required."
            );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> handler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Value
    private static class TestScenario {
        Event event;
        boolean canBeHandledExpected;

        public static List<HearingCancelledConfirmationTest.TestScenario> testScenarioBuilder() {
            List<HearingCancelledConfirmationTest.TestScenario> testScenarioList = new ArrayList<>();
            for (Event e : Event.values()) {
                HearingCancelledConfirmationTest.TestScenario testScenario;
                if (e.equals(Event.HEARING_CANCELLED)) {
                    testScenario = new HearingCancelledConfirmationTest.TestScenario(e, true);
                } else {
                    testScenario = new HearingCancelledConfirmationTest.TestScenario(e, false);
                }
                testScenarioList.add(testScenario);
            }
            return testScenarioList;
        }
    }
}