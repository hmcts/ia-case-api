package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.linkappeal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(JUnitParamsRunner.class)
public class UnlinkAppealConfirmationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    UnlinkAppealConfirmation unlinkAppealConfirmation = new UnlinkAppealConfirmation();

    @Mock
    Callback<AsylumCase> callback;

    @Test
    @Parameters(method = "generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = unlinkAppealConfirmation.canHandle(callback);

        Assertions.assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
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

    @Test
    public void handle() {
        when(callback.getEvent()).thenReturn(Event.UNLINK_APPEAL);

        PostSubmitCallbackResponse actualResponse = unlinkAppealConfirmation.handle(callback);

        assertTrue(actualResponse.getConfirmationHeader().isPresent());
        assertTrue(actualResponse.getConfirmationBody().isPresent());

        Assert.assertThat(
            actualResponse.getConfirmationHeader().get(),
            containsString("# You have unlinked this appeal")
        );

        Assert.assertThat(
            actualResponse.getConfirmationBody().get(),
            containsString("This appeal is now unlinked and will proceed as usual. "
                + "You must update the linked appeal spreadsheet to reflect this change.")
        );

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

}