package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPELLANT_MINOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MinorTagHandlerTest {

    public static final String APPELLANT_ADULT = LocalDate.of(1979, 2, 1).toString();
    public static final String APPELLANT_MINOR = LocalDate.now().toString();
    private final MinorTagHandler minorTagHandler = new MinorTagHandler();

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("generateCanHandleTestScenario")
    void it_can_handle_callback(CanHandleTestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        boolean canHandleActual = minorTagHandler.canHandle(scenario.callbackStage, callback);

        assertThat(canHandleActual).isEqualTo(scenario.canHandledExpected);
    }

    private static List<CanHandleTestScenario> generateCanHandleTestScenario() {
        return CanHandleTestScenario.builder();
    }

    @ParameterizedTest
    @MethodSource("generateAppellantDobScenarios")
    void given_appellant_dob_should_tag_case_as_minor_or_not_accordingly(AppellantDobScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPELLANT_DATE_OF_BIRTH, scenario.appellantDob);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> actual = minorTagHandler.handle(ABOUT_TO_SUBMIT, callback);

        YesOrNo isAppellantMinor = actual.getData().read(IS_APPELLANT_MINOR, YesOrNo.class)
            .orElse(null);
        assertThat(isAppellantMinor).isEqualTo(scenario.isAppellantMinorExpected);
    }

    private static List<AppellantDobScenario> generateAppellantDobScenarios() {
        return AppellantDobScenario.builder();
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> minorTagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "ABOUT_TO_START, SUBMIT_APPEAL",
        "ABOUT_TO_START, EDIT_APPEAL_AFTER_SUBMIT",
        "ABOUT_TO_SUBMIT, START_APPEAL"
    })
    void given_wrong_event_or_wrong_callback_should_throw_exception(
        PreSubmitCallbackStage callbackStage,
        Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPELLANT_DATE_OF_BIRTH, APPELLANT_ADULT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> minorTagHandler.handle(callbackStage, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Value
    private static class CanHandleTestScenario {
        PreSubmitCallbackStage callbackStage;
        Event event;
        boolean canHandledExpected;

        private static List<CanHandleTestScenario> builder() {
            List<CanHandleTestScenario> scenarios = new ArrayList<>();
            List<Event> validEvents = Arrays.asList(SUBMIT_APPEAL,
                EDIT_APPEAL_AFTER_SUBMIT);
            for (Event event : Event.values()) {
                if (validEvents.contains(event)) {
                    for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                        if (callbackStage.equals(ABOUT_TO_SUBMIT)) {
                            scenarios.add(new CanHandleTestScenario(ABOUT_TO_SUBMIT, event, true));
                        } else {
                            scenarios.add(new CanHandleTestScenario(callbackStage, event, false));
                        }
                    }
                } else {
                    for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                        scenarios.add(new CanHandleTestScenario(callbackStage, event, false));
                    }
                }
            }
            return scenarios;
        }

    }

    @Value
    private static class AppellantDobScenario {
        String appellantDob;
        Event event;
        YesOrNo isAppellantMinorExpected;

        private static List<AppellantDobScenario> builder() {
            List<AppellantDobScenario> scenarios = new ArrayList<>();
            scenarios.add(new AppellantDobScenario(APPELLANT_MINOR, SUBMIT_APPEAL, YesOrNo.YES));
            scenarios.add(new AppellantDobScenario(APPELLANT_ADULT, SUBMIT_APPEAL, YesOrNo.NO));
            scenarios.add(new AppellantDobScenario(APPELLANT_MINOR, EDIT_APPEAL_AFTER_SUBMIT, YesOrNo.YES));
            scenarios.add(new AppellantDobScenario(APPELLANT_ADULT, EDIT_APPEAL_AFTER_SUBMIT, YesOrNo.NO));
            return scenarios;
        }

    }

}
