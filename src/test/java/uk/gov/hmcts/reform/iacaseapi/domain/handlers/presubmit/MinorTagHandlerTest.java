package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPELLANT_MINOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(JUnitParamsRunner.class)
public class MinorTagHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private final MinorTagHandler minorTagHandler = new MinorTagHandler();
    public static final String APPELLANT_ADULT = LocalDate.of(1979, 2, 1).toString();
    public static final String APPELLANT_MINOR = LocalDate.now().toString();

    @Test
    @Parameters(method = "generateCanHandleTestScenario")
    public void it_can_handle_callback(CanHandleTestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean canHandleActual = minorTagHandler.canHandle(scenario.callbackStage, callback);

        assertThat(canHandleActual).isEqualTo(scenario.canHandledExpected);
    }

    private List<CanHandleTestScenario> generateCanHandleTestScenario() {
        return CanHandleTestScenario.builder();
    }

    @Value
    private static class CanHandleTestScenario {
        PreSubmitCallbackStage callbackStage;
        Event event;
        boolean canHandledExpected;

        private static List<CanHandleTestScenario> builder() {
            List<CanHandleTestScenario> scenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.SUBMIT_APPEAL)) {
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_SUBMIT, e, true));
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_START, e, false));
                    scenarios.add(new CanHandleTestScenario(MID_EVENT, e, false));
                } else {
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_SUBMIT, e, false));
                    scenarios.add(new CanHandleTestScenario(ABOUT_TO_START, e, false));
                    scenarios.add(new CanHandleTestScenario(MID_EVENT, e, false));
                }
            }
            return scenarios;
        }

    }

    @Test
    @Parameters(method = "generateAppellantDobScenarios")
    public void given_appellant_is_minor_should_tag_case_as_minor(AppellantDobScenario scenario) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPELLANT_DATE_OF_BIRTH, scenario.appellantDob);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> actual = minorTagHandler.handle(ABOUT_TO_SUBMIT, callback);

        YesOrNo isAppellantMinor = actual.getData().read(IS_APPELLANT_MINOR, YesOrNo.class)
            .orElse(YesOrNo.NO);

        assertThat(isAppellantMinor).isEqualTo(scenario.isAppellantMinorExpected);
    }

    private List<AppellantDobScenario> generateAppellantDobScenarios() {
        return AppellantDobScenario.builder();
    }

    @Value
    private static class AppellantDobScenario {
        String appellantDob;
        YesOrNo isAppellantMinorExpected;

        private static List<AppellantDobScenario> builder() {
            List<AppellantDobScenario> scenarios = new ArrayList<>();

            LocalDate appellantAdult = LocalDate.of(1979, 2, 1);
            scenarios.add(new AppellantDobScenario(appellantAdult.toString(), YesOrNo.NO));

            LocalDate appellantMinor = LocalDate.now();
            scenarios.add(new AppellantDobScenario(appellantMinor.toString(), YesOrNo.YES));

            scenarios.add(new AppellantDobScenario(null, YesOrNo.NO));
            scenarios.add(new AppellantDobScenario("", YesOrNo.NO));

            return scenarios;
        }

    }


    @Test
    public void should_not_allow_null_arguments() {
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

    @Test
    public void given_canHandled_is_false_should_throw_exception() {
        assertThatThrownBy(() -> minorTagHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}