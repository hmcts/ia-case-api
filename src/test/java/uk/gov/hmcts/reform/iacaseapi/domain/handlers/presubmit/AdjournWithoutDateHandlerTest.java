package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AdjournWithoutDateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private NextHearingDateService nextHearingDateService;

    private AdjournWithoutDateHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AdjournWithoutDateHandler(nextHearingDateService);
    }

    @ParameterizedTest
    @MethodSource("generateTestScenarios")
    void it_can_handle_callback(TestScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.getEvent());

        boolean actualResult = handler.canHandle(scenario.callbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.canBeHandledExpected);
    }

    private static List<TestScenario> generateTestScenarios() {
        return TestScenario.testScenarioBuilder();
    }

    @Test
    void should_not_allow_null_arguments() {

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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void sets_hearing_date_to_adjourned() {
        given(callback.getEvent()).willReturn(Event.ADJOURN_HEARING_WITHOUT_DATE);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(callback.getCaseDetailsBefore()).willReturn(Optional.of(caseDetails));
        given(caseDetails.getState()).willReturn(State.PREPARE_FOR_HEARING);
        given(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).willReturn(Optional.of("05/05/2020"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE_ADJOURNED), eq("Adjourned"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE), eq("prepareForHearing"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.DATE_BEFORE_ADJOURN_WITHOUT_DATE), eq("05/05/2020"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.DOES_THE_CASE_NEED_TO_BE_RELISTED), eq(NO));
    }

    @Test
    void should_clear_next_hearing_date_and_list_case_hearing_date() {
        given(callback.getEvent()).willReturn(Event.ADJOURN_HEARING_WITHOUT_DATE);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(callback.getCaseDetailsBefore()).willReturn(Optional.of(caseDetails));
        given(caseDetails.getState()).willReturn(State.PREPARE_FOR_HEARING);
        given(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).willReturn(Optional.of("05/05/2020"));
        given(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).willReturn(Optional.of(NO));
        given(nextHearingDateService.enabled()).willReturn(true);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        then(asylumCase).should(times(1))
            .clear(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS),
                eq(NextHearingDetails.builder().hearingId(null).hearingDateTime(null).build()));
    }

    @Test
    void should_not_clear_next_hearing_date_and_list_case_hearing_date_for_integrated_cases() {
        given(callback.getEvent()).willReturn(Event.ADJOURN_HEARING_WITHOUT_DATE);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(callback.getCaseDetailsBefore()).willReturn(Optional.of(caseDetails));
        given(caseDetails.getState()).willReturn(State.PREPARE_FOR_HEARING);
        given(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).willReturn(Optional.of("05/05/2020"));
        given(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).willReturn(Optional.of(YES));
        given(nextHearingDateService.enabled()).willReturn(true);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        then(asylumCase).should(never())
            .clear(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE));
        then(asylumCase).should(never())
            .write(eq(AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS), any());
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
}
