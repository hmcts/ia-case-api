package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AllocateTheCaseResetOptionsHandlerTest {

    @Mock
    FeatureToggler featureToggler;
    @InjectMocks
    AllocateTheCaseResetOptionsHandler handler;

    @Mock
    private Callback<AsylumCase> callback;

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void canHandle(Scenario scenario) {
        when(featureToggler.getValue("allocate-a-case-feature", false))
            .thenReturn(scenario.isFeatureFlagValue());

        when(callback.getEvent()).thenReturn(scenario.getEvent());

        boolean actual = handler.canHandle(scenario.getCallbackStage(), callback);

        assertThat(actual).isEqualTo(scenario.expected);
    }

    private static List<Scenario> scenarioProvider() {
        List<Scenario> scenarios = new ArrayList<>();

        Arrays.stream(Event.values()).forEach(event -> {
            if (Event.ALLOCATE_THE_CASE.equals(event)) {
                Scenario happyPathScenario = Scenario.builder()
                    .callbackStage(PreSubmitCallbackStage.ABOUT_TO_START)
                    .featureFlagValue(true)
                    .event(event)
                    .expected(true)
                    .build();

                Scenario featureFlagOffScenario = Scenario.builder()
                    .callbackStage(PreSubmitCallbackStage.ABOUT_TO_START)
                    .featureFlagValue(false)
                    .event(event)
                    .expected(false)
                    .build();

                Scenario wrongCallbackStageScenario = Scenario.builder()
                    .callbackStage(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    .featureFlagValue(false)
                    .event(event)
                    .expected(false)
                    .build();

                scenarios.add(happyPathScenario);
                scenarios.add(featureFlagOffScenario);
                scenarios.add(wrongCallbackStageScenario);

            } else {
                scenarios.add(Scenario.builder()
                    .callbackStage(PreSubmitCallbackStage.ABOUT_TO_START)
                    .featureFlagValue(true)
                    .event(event)
                    .expected(false)
                    .build());
            }
        });

        return scenarios;
    }

    @Value
    @Builder
    private static class Scenario {
        boolean featureFlagValue;
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean expected;
    }

    @Test
    void handle() {
    }

}