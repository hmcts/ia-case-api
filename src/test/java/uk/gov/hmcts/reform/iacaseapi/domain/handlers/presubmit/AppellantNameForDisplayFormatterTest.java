package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppellantNameForDisplayFormatterTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    AppellantNameForDisplayFormatter appellantNameForDisplayFormatter =
        new AppellantNameForDisplayFormatter();

    @Test
    void should_format_appellant_name_for_display() {

        Map<Pair<String, String>, String> exampleInputOutputs =
            ImmutableMap
                .of(Pair.of("Jane Mary", "Smith"), "Jane Mary Smith",
                    Pair.of("John", "Doe"), "John Doe",
                    Pair.of(" Matt ", " Jones "), "Matt Jones");

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final Pair<String, String> input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.empty());
                when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(input.getKey()));
                when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(input.getValue()));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, times(1)).write(APPELLANT_NAME_FOR_DISPLAY, output);

                reset(asylumCase);
            });
    }

    @Test
    void should_throw_when_appellant_given_names_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantGivenNames is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_appellant_family_name_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNameForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.empty());

                boolean canHandle = appellantNameForDisplayFormatter.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue("Can handle event " + event, canHandle);
                } else {
                    assertFalse("Cannot handle event " + event, canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void cannot_handle_Aip_start_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.of(JourneyType.AIP));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        boolean canHandle = appellantNameForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(canHandle, is(false));
    }

    @Test
    void cannot_handle_Aip_edit_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.of(JourneyType.AIP));
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);

        boolean canHandle = appellantNameForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(canHandle, is(false));
    }

    @Test
    void can_handle_Aip_other_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.of(JourneyType.AIP));
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        boolean canHandle = appellantNameForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(canHandle, is(true));
    }

    @Test
    void can_handle_Rep_start_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.of(JourneyType.REP));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        boolean canHandle = appellantNameForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(canHandle, is(true));
    }

    @Test
    void can_handle_JourneyType_not_set_start_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE)).thenReturn(Optional.empty());
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        boolean canHandle = appellantNameForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(canHandle, is(true));
    }

    @Test
    void should_not_allow_null_arguments() {

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
