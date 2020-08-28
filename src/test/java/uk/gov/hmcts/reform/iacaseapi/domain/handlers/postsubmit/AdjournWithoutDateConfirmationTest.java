package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class AdjournWithoutDateConfirmationTest {

    @Mock private
    Callback<AsylumCase> callback;

    AdjournWithoutDateConfirmation handler = new AdjournWithoutDateConfirmation();

    @ParameterizedTest
    @MethodSource("generateTestScenarios")
    void given_callback_can_handle(Event event, boolean canBeHandledExpected) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = handler.canHandle(callback);

        assertThat(actualResult).isEqualTo(canBeHandledExpected);
    }

    private static Stream<Arguments> generateTestScenarios() {

        List<Arguments> scenarios = new ArrayList<>();

        for (Event e : Event.values()) {
            if (e.equals(Event.ADJOURN_HEARING_WITHOUT_DATE)) {
                scenarios.add(Arguments.of(e, true));
            } else {
                scenarios.add(Arguments.of(e, false));
            }
        }

        return scenarios.stream();
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
        when(callback.getEvent()).thenReturn(Event.ADJOURN_HEARING_WITHOUT_DATE);

        PostSubmitCallbackResponse callbackResponse = handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# The hearing has been adjourned");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                + "Both parties will be notified and a Notice of Adjournment will be generated.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> handler.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}