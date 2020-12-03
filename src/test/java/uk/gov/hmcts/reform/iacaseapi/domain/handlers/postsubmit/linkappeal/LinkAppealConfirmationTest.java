package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.linkappeal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class LinkAppealConfirmationTest {

    @Mock
    Callback<AsylumCase> callback;

    LinkAppealConfirmation linkAppealConfirmation = new LinkAppealConfirmation();


    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = linkAppealConfirmation.canHandle(callback);

        assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private static List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Test
    void handle() {
        when(callback.getEvent()).thenReturn(Event.LINK_APPEAL);

        PostSubmitCallbackResponse actualResponse = linkAppealConfirmation.handle(callback);

        assertTrue(actualResponse.getConfirmationHeader().isPresent());
        assertTrue(actualResponse.getConfirmationBody().isPresent());

        assertThat(
            actualResponse.getConfirmationHeader().get())
            .contains("# You have linked this appeal");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains("#### What happens next\r\n\r\n");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains(
                "The appeal will now be considered as part of a set of linked appeals. "
                    + "You must update the linked appeal spreadsheet to reflect this change."
            );

    }

    @Test
    void should_throw_exception() {
        assertThatThrownBy(() -> linkAppealConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> linkAppealConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Value
    private static class CanHandleScenario {
        Event event;
        boolean canHandleExpectedResult;

        public static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (Event.LINK_APPEAL.equals(e)) {
                    scenarios.add(new CanHandleScenario(e, true));
                } else {
                    scenarios.add(new CanHandleScenario(e, false));
                }
            }
            return scenarios;
        }
    }
}
