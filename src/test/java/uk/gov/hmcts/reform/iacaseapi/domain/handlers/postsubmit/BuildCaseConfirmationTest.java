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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(JUnitParamsRunner.class)
public class BuildCaseConfirmationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private BuildCaseConfirmation buildCaseConfirmation = new BuildCaseConfirmation();

    @Test
    public void should_return_confirmation() {
        ReflectionTestUtils.setField(buildCaseConfirmation, "isSaveAndContinueEnabled", true);

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);

        PostSubmitCallbackResponse callbackResponse =
            buildCaseConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("Upload saved")
        );

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You still need to submit your case")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(
                "[submit your case]"
                + "(/case/IA/Asylum/" + caseId + "/trigger/submitCase)"
            )
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(
                "[build your case]"
                + "(/case/IA/Asylum/" + caseId + "/trigger/buildCase)"
            )
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> buildCaseConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @Parameters(method = "generateDifferentEventScenarios")
    public void it_can_handle_callback(EventScenarios event) {
        ReflectionTestUtils.setField(buildCaseConfirmation, "isSaveAndContinueEnabled", event.isFlag());
        when(callback.getEvent()).thenReturn(event.getEvent());

        boolean canHandle = buildCaseConfirmation.canHandle(callback);

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
                    testScenarios.add(new EventScenarios(e, true, true));
                } else {
                    testScenarios.add(new EventScenarios(e, true, false));
                }
                testScenarios.add(new EventScenarios(e, false, false));
            }
            return testScenarios;
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> buildCaseConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
