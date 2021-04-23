package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddBaseLocationFromHearingCentreForOldCasesFixHandlerTest {

    @MockBean
    private CaseManagementLocationService caseManagementLocationService;
    @InjectMocks
    private AddBaseLocationFromHearingCentreForOldCasesFixHandler handler;

    @Mock
    private Callback<AsylumCase> callback;

    @ParameterizedTest
    @MethodSource("canHandleScenarioProvider")
    void canHandle(Scenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());

        boolean actual = handler.canHandle(scenario.getPreSubmitCallbackStage(), callback);

        assertThat(actual).isEqualTo(scenario.isExpected());
    }

    private static List<Scenario> canHandleScenarioProvider() {
        List<Event> blackListEvents = Arrays.asList(
            Event.SUBMIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.PAY_AND_SUBMIT_APPEAL,
            Event.CHANGE_HEARING_CENTRE);

        List<Scenario> canHandleIsTrueScenarios = new ArrayList<>();
        List<Scenario> canHandleIsFalseScenarios = new ArrayList<>();
        List<Scenario> allCanHandleScenarios = new ArrayList<>();

        Arrays.stream(Event.values()).forEach(e -> {
            if (!blackListEvents.contains(e)) {
                canHandleIsTrueScenarios.add(buildScenario(e, PreSubmitCallbackStage.ABOUT_TO_START, true));
            } else {
                canHandleIsFalseScenarios.add(buildScenario(e, PreSubmitCallbackStage.ABOUT_TO_START, false));
            }
            canHandleIsFalseScenarios.add(buildScenario(e, PreSubmitCallbackStage.ABOUT_TO_SUBMIT, false));
            canHandleIsFalseScenarios.add(buildScenario(e, PreSubmitCallbackStage.MID_EVENT, false));

        });

        allCanHandleScenarios.addAll(canHandleIsTrueScenarios);
        allCanHandleScenarios.addAll(canHandleIsFalseScenarios);

        return allCanHandleScenarios;
    }

    private static Scenario buildScenario(Event e, PreSubmitCallbackStage aboutToStart, boolean b) {
        return Scenario.builder()
            .event(e)
            .preSubmitCallbackStage(aboutToStart)
            .expected(b)
            .build();
    }

    @Value
    @Builder
    private static class Scenario {
        Event event;
        PreSubmitCallbackStage preSubmitCallbackStage;
        boolean expected;
    }

    @Test
    void canHandleRequireNonNull() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("callback must not be null");
    }

    @Test
    void getDispatchPriority() {
    }

    @Test
    void handle() {
    }

}