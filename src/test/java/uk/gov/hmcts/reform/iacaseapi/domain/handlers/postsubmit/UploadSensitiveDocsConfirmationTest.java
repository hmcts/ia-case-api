package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(JUnitParamsRunner.class)
public class UploadSensitiveDocsConfirmationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    UploadSensitiveDocsConfirmation handler = new UploadSensitiveDocsConfirmation();

    @Test
    @Parameters(method = "generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = handler.canHandle(callback);

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
                if (Event.UPLOAD_SENSITIVE_DOCUMENTS.equals(e)) {
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
        when(callback.getEvent()).thenReturn(Event.UPLOAD_SENSITIVE_DOCUMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1593428851262042L);

        PostSubmitCallbackResponse actualResponse = handler.handle(callback);

        assertTrue(actualResponse.getConfirmationHeader().isPresent());
        assertTrue(actualResponse.getConfirmationBody().isPresent());

        Assert.assertThat(
            actualResponse.getConfirmationHeader().get(),
            containsString("# You have uploaded sensitive documentation")
        );

        Assert.assertThat(
            actualResponse.getConfirmationBody().get(),
            containsString("#### What happens next\r\n\r\n")
        );

        Assert.assertThat(
            actualResponse.getConfirmationBody().get(),
            containsString(
                "You can see the documentation in the [documents tab](/cases/case-details/1593428851262042#documents). "
                    + "Select Edit documents from the Next step dropdown if you need to remove a document."
            )
        );
    }

    @Test
    public void should_throw_exception() {
        assertThatThrownBy(() -> handler.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> handler.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


}