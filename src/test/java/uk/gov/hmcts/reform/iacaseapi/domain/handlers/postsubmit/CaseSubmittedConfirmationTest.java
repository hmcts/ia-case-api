package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CaseSubmittedConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private CaseSubmittedConfirmation caseSubmittedConfirmation =
        new CaseSubmittedConfirmation();

    @Test
    void should_return_bau_confirmation_when_not_24_week_case() {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", true);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            caseSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(callbackResponse.getConfirmationHeader().get().contains("You have submitted your case"));
        assertTrue(callbackResponse.getConfirmationBody().get().contains("The case officer will now review your appeal"));
        assertTrue(callbackResponse.getConfirmationBody().get().contains("they will send it to the respondent for them to review"));
    }

    @Test
    void should_return_24_week_confirmation_when_24_week_flag_is_yes() {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", true);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            caseSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(callbackResponse.getConfirmationHeader().get().contains("You have submitted your case"));
        assertTrue(callbackResponse.getConfirmationBody().get().contains("The case officer will now review the appeal"));
        assertTrue(callbackResponse.getConfirmationBody().get().contains("you will be asked to provide any hearing requirements"));
    }

    @Test
    void should_return_bau_confirmation_when_24_week_flag_is_absent() {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", true);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.empty());

        PostSubmitCallbackResponse callbackResponse =
            caseSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationBody().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().get().contains("The case officer will now review your appeal"));
        assertTrue(callbackResponse.getConfirmationBody().get().contains("they will send it to the respondent for them to review"));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    void it_can_handle_callback(EventScenarios event) {
        ReflectionTestUtils.setField(caseSubmittedConfirmation, "isSaveAndContinueEnabled", event.isFlag());
        when(callback.getEvent()).thenReturn(event.getEvent());

        boolean canHandle = caseSubmittedConfirmation.canHandle(callback);

        Assertions.assertThat(canHandle).isEqualTo(event.isExpected());
    }

    private static List<EventScenarios> generateDifferentEventScenarios() {
        return EventScenarios.builder();
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseSubmittedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseSubmittedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
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
                    testScenarios.add(new EventScenarios(e, true, false));
                    testScenarios.add(new EventScenarios(e, false, true));
                } else if (e.equals(Event.SUBMIT_CASE)) {
                    testScenarios.add(new EventScenarios(e, true, true));
                    testScenarios.add(new EventScenarios(e, false, false));
                } else {
                    testScenarios.add(new EventScenarios(e, true, false));
                    testScenarios.add(new EventScenarios(e, false, false));
                }
            }
            return testScenarios;
        }
    }
}
