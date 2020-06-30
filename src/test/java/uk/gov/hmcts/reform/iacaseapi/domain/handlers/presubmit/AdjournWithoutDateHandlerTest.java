package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(JUnitParamsRunner.class)
public class AdjournWithoutDateHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AdjournWithoutDateHandler handler = new AdjournWithoutDateHandler();

    @Test
    @Parameters(method = "generateTestScenarios")
    public void it_can_handle_callback(TestScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.getEvent());

        boolean actualResult = handler.canHandle(scenario.callbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.canBeHandledExpected);
    }

    private List<TestScenario> generateTestScenarios() {
        return TestScenario.testScenarioBuilder();
    }

    @Value
    private static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean canBeHandledExpected;

        public static List<TestScenario> testScenarioBuilder() {
            List<TestScenario> testScenarioList = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.ADJOURN_HEARING_WITHOUT_DATE)) {
                    testScenarioList.add(new TestScenario(e, ABOUT_TO_START, false));
                    testScenarioList.add(new TestScenario(e, MID_EVENT, false));
                    testScenarioList.add(new TestScenario(e, ABOUT_TO_SUBMIT, true));
                } else {
                    testScenarioList.add(new TestScenario(e, ABOUT_TO_START, false));
                    testScenarioList.add(new TestScenario(e, MID_EVENT, false));
                    testScenarioList.add(new TestScenario(e, ABOUT_TO_SUBMIT, false));
                }
            }
            return testScenarioList;
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void sets_hearing_date_to_adjourned() {
        given(callback.getEvent()).willReturn(Event.ADJOURN_HEARING_WITHOUT_DATE);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.class)).willReturn(Optional.of(State.PREPARE_FOR_HEARING));
        given(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).willReturn(Optional.of("05/05/2020"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE_ADJOURNED), eq("Adjourned"));
        then(asylumCase).should(times(1))

            .write(eq(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE), eq("prepareForHearing"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.DATE_BEFORE_ADJOURN_WITHOUT_DATE), eq("05/05/2020"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.DOES_THE_CASE_NEED_TO_BE_RELISTED), eq(YesOrNo.NO));
        then(asylumCase).should(times(1))
            .clear(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE));

        then(asylumCase).should(times(1))
            .clear(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE));
    }
}