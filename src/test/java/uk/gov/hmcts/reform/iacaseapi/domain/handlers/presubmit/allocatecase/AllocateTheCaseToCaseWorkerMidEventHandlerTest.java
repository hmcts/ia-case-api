package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseWorkerService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@Slf4j
class AllocateTheCaseToCaseWorkerMidEventHandlerTest {

    private final String someActorId = "some actor id";
    @Mock
    private FeatureToggler featureToggle;
    @Mock
    private CaseWorkerService caseWorkerService;
    @Mock
    private AllocateTheCaseService allocateTheCaseService;
    @InjectMocks
    private AllocateTheCaseToCaseWorkerMidEventHandler handler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void canHandle(Scenario scenario) {
        log.info("*** scenario *** \n {}", scenario);

        when(callback.getEvent()).thenReturn(scenario.event);

        when(featureToggle.getValue("allocate-a-case-feature", false))
            .thenReturn(scenario.featureToggleResponse);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class)))
            .thenReturn(scenario.allocateToCaseWorkerOption);

        boolean actualResult = handler.canHandle(scenario.preSubmitCallbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.expectedResult);
    }

    private static List<Scenario> scenarioProvider() {
        List<Scenario> scenarios = new ArrayList<>();

        Stream.of(Event.values()).forEach(event -> {

            if (Event.ALLOCATE_THE_CASE.equals(event)) {

                Scenario scenarioWithMidEventAndAllocateToMe = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .allocateToCaseWorkerOption(false)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithMidEventAllocateToCaseWorker = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .allocateToCaseWorkerOption(true)
                    .expectedResult(true)
                    .build();

                Scenario scenarioWithMidEventAndFalseToggle = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(false)
                    .event(event)
                    .allocateToCaseWorkerOption(true)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithAboutToSubmit = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    .featureToggleResponse(true)
                    .event(event)
                    .allocateToCaseWorkerOption(true)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithAboutToStart = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_START)
                    .featureToggleResponse(true)
                    .event(event)
                    .allocateToCaseWorkerOption(true)
                    .expectedResult(false)
                    .build();

                scenarios.add(scenarioWithMidEventAndAllocateToMe);
                scenarios.add(scenarioWithMidEventAllocateToCaseWorker);
                scenarios.add(scenarioWithMidEventAndFalseToggle);
                scenarios.add(scenarioWithAboutToSubmit);
                scenarios.add(scenarioWithAboutToStart);
            } else {

                Scenario scenarioWithMidEvent = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .allocateToCaseWorkerOption(true)
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
        boolean allocateToCaseWorkerOption;
        boolean expectedResult;

    }

    @Test
    void should_not_allow_null_arguments() {
        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class)))
            .thenReturn(true);

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("handleScenarioProvider")
    void handle(HandleScenario scenario) {
        when(callback.getEvent()).thenReturn(Event.ALLOCATE_THE_CASE);
        when(featureToggle.getValue("allocate-a-case-feature", false))
            .thenReturn(true);
        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class)))
            .thenReturn(true);

        mockCaseDetails();
        mockCaseWorkerService(scenario.getCaseWorkerName(), scenario.getAssignments());

        PreSubmitCallbackResponse<AsylumCase> actualResult =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Optional<DynamicList> caseWorkerNameList = actualResult.getData().read(
            CASE_WORKER_NAME_LIST,
            DynamicList.class
        );

        if (scenario.isCaseWorkerListPresent()) {
            assertThat(caseWorkerNameList).isPresent();
            String someCaseworkerName = "some caseworker name";
            DynamicList expectedCaseWorkerNameList =
                new DynamicList(
                    new uk.gov.hmcts.reform.iacaseapi.domain.entities.Value("", ""),
                    List.of(new uk.gov.hmcts.reform.iacaseapi.domain.entities.Value(someActorId, someCaseworkerName))
                );
            assertThat(caseWorkerNameList.get()).isEqualTo(expectedCaseWorkerNameList);

        } else {
            assertThat(caseWorkerNameList).isEmpty();
            actualResult.getErrors().forEach((error) ->
                assertThat("There are no users for the location you have selected. Choose another location to continue.")
                    .isEqualTo(error)
            );
        }

    }

    private static Stream<HandleScenario> handleScenarioProvider() {
        HandleScenario assignmentAndCaseWorkerNameExistScenario = new HandleScenario(
            new CaseWorkerName("some actor id", "some caseworker name"),
            List.of(Assignment.builder().actorId("some actor id").build()),
            true
        );

        HandleScenario assignmentDoesNotExistScenario = new HandleScenario(
            new CaseWorkerName("some actor id", "some caseworker name"),
            Collections.emptyList(),
            false
        );

        HandleScenario assignmentExistsAndCaseWorkerNameDoesNotExistScenario = new HandleScenario(
            new CaseWorkerName("some actor id", StringUtils.EMPTY),
            List.of(Assignment.builder().actorId("some actor id").build()),
            false
        );

        return Stream.of(assignmentAndCaseWorkerNameExistScenario,
            assignmentDoesNotExistScenario,
            assignmentExistsAndCaseWorkerNameDoesNotExistScenario);
    }

    @Value
    private static class HandleScenario {
        CaseWorkerName caseWorkerName;
        List<Assignment> assignments;
        boolean caseWorkerListPresent;
    }

    private void mockCaseWorkerService(CaseWorkerName caseWorkerName, List<Assignment> assignments) {

        when(caseWorkerService.getRoleAssignmentsPerLocationAndClassification(
            "some location id",
            "PUBLIC")
        ).thenReturn(assignments);

        when(caseWorkerService.getCaseWorkerNameForActorIds(newArrayList(someActorId)))
            .thenReturn(newArrayList(caseWorkerName));
    }

    private void mockCaseDetails() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(CASE_WORKER_LOCATION_LIST, "some location id");
        asylumCase.write(ALLOCATE_THE_CASE_TO, "caseworker");
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getSecurityClassification()).thenReturn("PUBLIC");
    }

    @ParameterizedTest
    @MethodSource("exceptionHandleScenarioProvider")
    void handling_should_throw_exception_if_cannot_handle(ExceptionHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class)))
            .thenReturn(true);

        assertThatThrownBy(() -> handler.handle(scenario.getPreSubmitCallbackStage(), callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private static Stream<ExceptionHandleScenario> exceptionHandleScenarioProvider() {
        ExceptionHandleScenario scenarioWithWrongEvent = ExceptionHandleScenario.builder()
            .event(Event.ADD_CASE_NOTE)
            .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
            .build();

        ExceptionHandleScenario scenarioWithWrongCallbackStage = ExceptionHandleScenario.builder()
            .event(Event.ALLOCATE_THE_CASE)
            .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
            .build();

        return Stream.of(scenarioWithWrongEvent, scenarioWithWrongCallbackStage);
    }

    @Value
    @Builder
    private static class ExceptionHandleScenario {
        PreSubmitCallbackStage preSubmitCallbackStage;
        Event event;
    }

}
