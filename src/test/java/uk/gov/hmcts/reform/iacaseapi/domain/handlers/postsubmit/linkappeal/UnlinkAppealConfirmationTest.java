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
public class UnlinkAppealConfirmationTest {

    UnlinkAppealConfirmation unlinkAppealConfirmation = new UnlinkAppealConfirmation();

    @Mock
    Callback<AsylumCase> callback;

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = unlinkAppealConfirmation.canHandle(callback);

        assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private static List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Test
    public void handle() {
        when(callback.getEvent()).thenReturn(Event.UNLINK_APPEAL);

        PostSubmitCallbackResponse actualResponse = unlinkAppealConfirmation.handle(callback);

        assertTrue(actualResponse.getConfirmationHeader().isPresent());
        assertTrue(actualResponse.getConfirmationBody().isPresent());

        assertThat(
            actualResponse.getConfirmationHeader().get())
            .contains("# You have unlinked this appeal");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains("This appeal is now unlinked and will proceed as usual. "
                + "You must update the linked appeal spreadsheet to reflect this change.");

    }

    @Test
    public void should_throw_exception() {
        assertThatThrownBy(() -> unlinkAppealConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> unlinkAppealConfirmation.handle(callback))
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
                if (Event.UNLINK_APPEAL.equals(e)) {
                    scenarios.add(new CanHandleScenario(e, true));
                } else {
                    scenarios.add(new CanHandleScenario(e, false));
                }
            }
            return scenarios;
        }
    }

}
