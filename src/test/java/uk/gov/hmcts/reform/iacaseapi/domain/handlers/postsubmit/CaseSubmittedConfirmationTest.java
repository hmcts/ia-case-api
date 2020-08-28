package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class CaseSubmittedConfirmationTest {

    @Mock private
    Callback<AsylumCase> callback;

    CaseSubmittedConfirmation caseSubmittedConfirmation =
        new CaseSubmittedConfirmation();

    @Test
    void should_return_confirmation() {
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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    void it_can_handle_callback(Event event, boolean flag, boolean expected) {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", flag);
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = caseSubmittedConfirmation.canHandle(callback);

        Assertions.assertThat(canHandle).isEqualTo(expected);
    }


    private static Stream<Arguments> generateDifferentEventScenarios() {

        List<Arguments> scenarios = new ArrayList<>();

        for (Event e : Event.values()) {
            if (e.equals(Event.BUILD_CASE)) {
                scenarios.add(Arguments.of(e, true, false));
                scenarios.add(Arguments.of(e, false, true));
            } else if (e.equals(Event.SUBMIT_CASE)) {
                scenarios.add(Arguments.of(e, true, true));
                scenarios.add(Arguments.of(e, false, false));
            } else {
                scenarios.add(Arguments.of(e, true, false));
                scenarios.add(Arguments.of(e, false, false));
            }
        }

        return scenarios.stream();
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
