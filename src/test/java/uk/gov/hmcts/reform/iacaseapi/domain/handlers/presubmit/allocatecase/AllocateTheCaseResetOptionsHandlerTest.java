package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
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
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

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

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
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
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.ALLOCATE_THE_CASE);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        AsylumCase caseData = new AsylumCase();
        caseData.write(AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO, "caseworker");
        when(caseDetails.getCaseData()).thenReturn(caseData);

        PreSubmitCallbackResponse<AsylumCase> actual = handler.handle(ABOUT_TO_START, callback);

        assertThat(actual.getData().read(AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO)).isEmpty();
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}