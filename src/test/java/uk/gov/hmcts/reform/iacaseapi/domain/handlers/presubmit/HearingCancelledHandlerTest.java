package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HearingCancelledHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private NextHearingDateService nextHearingDateService;

    private HearingCancelledHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HearingCancelledHandler(nextHearingDateService);
    }

    @ParameterizedTest
    @MethodSource("generateTestScenarios")
    void it_can_handle_callback(HearingCancelledHandlerTest.TestScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.getEvent());

        boolean actualResult = handler.canHandle(scenario.callbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.canBeHandledExpected);
    }

    private static List<HearingCancelledHandlerTest.TestScenario> generateTestScenarios() {
        return HearingCancelledHandlerTest.TestScenario.testScenarioBuilder();
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
    void should_clear_next_hearing_date_and_list_case_hearing_date() {
        given(callback.getEvent()).willReturn(Event.HEARING_CANCELLED);
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


    @Value
    private static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean canBeHandledExpected;

        public static List<HearingCancelledHandlerTest.TestScenario> testScenarioBuilder() {
            List<HearingCancelledHandlerTest.TestScenario> testScenarioList = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.HEARING_CANCELLED)) {
                    testScenarioList.add(new HearingCancelledHandlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarioList.add(new HearingCancelledHandlerTest.TestScenario(e, MID_EVENT, false));
                    testScenarioList.add(new HearingCancelledHandlerTest.TestScenario(e, ABOUT_TO_SUBMIT, true));
                } else {
                    testScenarioList.add(new HearingCancelledHandlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarioList.add(new HearingCancelledHandlerTest.TestScenario(e, MID_EVENT, false));
                    testScenarioList.add(new HearingCancelledHandlerTest.TestScenario(e, ABOUT_TO_SUBMIT, false));
                }
            }
            return testScenarioList;
        }
    }

}