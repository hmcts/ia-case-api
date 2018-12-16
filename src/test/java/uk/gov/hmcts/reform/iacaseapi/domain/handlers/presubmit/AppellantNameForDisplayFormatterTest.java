package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javafx.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppellantNameForDisplayFormatterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private AppellantNameForDisplayFormatter appellantNameForDisplayFormatter =
        new AppellantNameForDisplayFormatter();

    @Test
    public void should_format_appellant_name_for_display() {

        Map<Pair<String, String>, String> exampleInputOutputs =
            ImmutableMap
                .of(new Pair<>("Jane Mary", "Smith"), "Jane Mary Smith",
                    new Pair<>("John", "Doe"), "John Doe",
                    new Pair<>(" Matt ", " Jones "), "Matt Jones");

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final Pair<String, String> input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(input.getKey()));
                when(asylumCase.getAppellantLastName()).thenReturn(Optional.of(input.getValue()));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, times(1)).setAppellantNameForDisplay(output);

                reset(asylumCase);
            });
    }

    @Test
    public void should_throw_when_appellant_given_names_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantGivenNames is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_appellant_last_name_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of("John"));
        when(asylumCase.getAppellantLastName()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantLastName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appellantNameForDisplayFormatter.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}