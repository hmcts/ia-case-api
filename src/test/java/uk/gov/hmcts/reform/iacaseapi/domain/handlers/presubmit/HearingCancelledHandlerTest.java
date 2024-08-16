package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.HEARING_CANCELLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;

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
        when(callback.getEvent()).thenReturn(scenario.getEvent());

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

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_clear_next_hearing_date_and_list_case_hearing_date() {
        when(callback.getEvent()).thenReturn(HEARING_CANCELLED);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));
        when(caseDetails.getState()).thenReturn(State.PREPARE_FOR_HEARING);
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("05/05/2020"));
        when(nextHearingDateService.enabled()).thenReturn(true);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(nextHearingDateService, times(1)).clearHearingDateInformation(asylumCase);
    }

    @Test
    void should_not_clear_next_hearing_date_and_list_case_hearing_date() {
        when(callback.getEvent()).thenReturn(HEARING_CANCELLED);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(nextHearingDateService.enabled()).thenReturn(false);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(nextHearingDateService, never()).clearHearingDateInformation(asylumCase);
    }

    @Value
    private static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean canBeHandledExpected;

        public static List<HearingCancelledHandlerTest.TestScenario> testScenarioBuilder() {
            List<HearingCancelledHandlerTest.TestScenario> testScenarioList = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(HEARING_CANCELLED)) {
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
