package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
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
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.RoleAssignmentService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AllocateTheCaseMidEventHandlerTest {

    @Mock
    private FeatureToggler featureToggle;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @InjectMocks
    private AllocateTheCaseMidEventHandler handler;
    @Mock
    private Callback<AsylumCase> callback;

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void canHandle(Scenario scenario) {

        when(callback.getEvent()).thenReturn(scenario.event);
        when(featureToggle.getValue("allocate-a-case-feature", false))
            .thenReturn(scenario.featureToggleResponse);

        boolean actualResult = handler.canHandle(scenario.preSubmitCallbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.expectedResult);
    }

    private static List<Scenario> scenarioProvider() {
        List<Scenario> scenarios = new ArrayList<>();

        Stream.of(Event.values()).forEach(event -> {

            if (Event.ALLOCATE_THE_CASE.equals(event)) {

                Scenario scenarioWithMidEvent = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(true)
                    .build();

                Scenario scenarioWithMidEventAndFalseToggle = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(false)
                    .event(event)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithAboutToSubmit = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithAboutToStart = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_START)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(false)
                    .build();

                scenarios.add(scenarioWithMidEvent);
                scenarios.add(scenarioWithMidEventAndFalseToggle);
                scenarios.add(scenarioWithAboutToSubmit);
                scenarios.add(scenarioWithAboutToStart);
            } else {

                Scenario scenarioWithMidEvent = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(false)
                    .build();

                scenarios.add(scenarioWithMidEvent);

            }

        });

        return scenarios;
    }

    @Value
    @Builder
    private static class Scenario {
        Event event;
        boolean featureToggleResponse;
        PreSubmitCallbackStage preSubmitCallbackStage;
        boolean expectedResult;

    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handle() {
    }
}