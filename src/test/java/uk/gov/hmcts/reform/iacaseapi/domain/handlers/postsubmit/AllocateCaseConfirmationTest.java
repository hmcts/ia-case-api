package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unchecked")
public class AllocateCaseConfirmationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private final AllocateCaseConfirmation handler = new AllocateCaseConfirmation();

    @Before
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.ALLOCATE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
    }

    @Test
    @Parameters(method = "generateTestScenarios")
    public void should_return_confirmation(TestScenario scenario) {
        AsylumCase asylum = new AsylumCase();
        asylum.write(AsylumCaseFieldDefinition.ALLOCATION_TYPE, scenario.allocationType);
        when(caseDetails.getCaseData()).thenReturn(asylum);
        PostSubmitCallbackResponse callbackResponse = handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have allocated the case")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(scenario.confirmationBodyContent1Expected)
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(scenario.confirmationBodyContent2Expected)
        );
    }

    private List<TestScenario> generateTestScenarios() {
        return TestScenario.testScenarioBuilder();
    }

    @Value
    private static class TestScenario {
        String allocationType;
        String confirmationBodyContent1Expected;
        String confirmationBodyContent2Expected;

        public static List<TestScenario> testScenarioBuilder() {
            List<TestScenario> testScenarioList = new ArrayList<>();

            TestScenario scenario1 = new TestScenario(
                "Allocate to me",
                "The tasks for this case will now appear in your task list.",
                "Should you wish, you can write [a case note](/case/IA/Asylum/0/trigger/addCaseNote)."
            );
            TestScenario scenario2 = new TestScenario(
                "Allocate to a colleague",
                "The tasks for this case will now appear in your colleague's task list.",
                "Should you wish, you can [write them a case note](/case/IA/Asylum/0/trigger/addCaseNote)."
                    + " They will be notified that this case has been allocated to them."
            );

            testScenarioList.add(scenario1);
            testScenarioList.add(scenario2);

            return testScenarioList;
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> handler.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = handler.canHandle(callback);

            if (event == Event.ALLOCATE_CASE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }


    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}