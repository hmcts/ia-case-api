package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class UploadSensitiveDocsConfirmationTest {

    UploadSensitiveDocsConfirmation handler = new UploadSensitiveDocsConfirmation();
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = handler.canHandle(callback);

        assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private static List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Test
    void handle() {
        when(callback.getEvent()).thenReturn(Event.UPLOAD_SENSITIVE_DOCUMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1593428851262042L);

        PostSubmitCallbackResponse actualResponse = handler.handle(callback);

        assertTrue(actualResponse.getConfirmationHeader().isPresent());
        assertTrue(actualResponse.getConfirmationBody().isPresent());

        assertThat(
            actualResponse.getConfirmationHeader().get())
            .contains("# You have uploaded sensitive documentation");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains("#### What happens next\r\n\r\n");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains(
                "You can see the documentation in the [documents tab](/cases/case-details/1593428851262042#documents). "
                    + "Select Edit documents from the Next step dropdown if you need to remove a document."
            );
    }

    @Test
    void should_throw_exception() {
        assertThatThrownBy(() -> handler.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> handler.handle(callback))
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
                if (Event.UPLOAD_SENSITIVE_DOCUMENTS.equals(e)) {
                    scenarios.add(new CanHandleScenario(e, true));
                } else {
                    scenarios.add(new CanHandleScenario(e, false));
                }
            }
            return scenarios;
        }
    }


}
