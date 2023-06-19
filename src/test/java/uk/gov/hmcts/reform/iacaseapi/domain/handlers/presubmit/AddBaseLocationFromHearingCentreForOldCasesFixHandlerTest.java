package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddBaseLocationFromHearingCentreForOldCasesFixHandlerTest {

    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @InjectMocks
    private AddBaseLocationFromHearingCentreForOldCasesFixHandler handler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("canHandleScenarioProvider")
    void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());

        boolean actual = handler.canHandle(scenario.getPreSubmitCallbackStage(), callback);

        assertThat(actual).isEqualTo(scenario.isExpected());
    }

    private static List<CanHandleScenario> canHandleScenarioProvider() {
        List<Event> blackListEvents = Arrays.asList(
            Event.SUBMIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.CHANGE_HEARING_CENTRE,
            Event.START_APPEAL);

        List<CanHandleScenario> canHandleIsTrueScenarios = new ArrayList<>();
        List<CanHandleScenario> canHandleIsFalseScenarios = new ArrayList<>();
        List<CanHandleScenario> allCanHandleScenarios = new ArrayList<>();

        Arrays.stream(Event.values()).forEach(e -> {
            if (!blackListEvents.contains(e)) {
                canHandleIsTrueScenarios.add(buildScenario(e, PreSubmitCallbackStage.ABOUT_TO_SUBMIT, true));
                canHandleIsFalseScenarios.add(buildScenario(e, PreSubmitCallbackStage.ABOUT_TO_START, true));
                canHandleIsFalseScenarios.add(buildScenario(e, PreSubmitCallbackStage.MID_EVENT, true));
            } else {
                canHandleIsFalseScenarios.add(buildScenario(e, PreSubmitCallbackStage.ABOUT_TO_SUBMIT, false));
            }
        });

        allCanHandleScenarios.addAll(canHandleIsTrueScenarios);
        allCanHandleScenarios.addAll(canHandleIsFalseScenarios);

        return allCanHandleScenarios;
    }

    private static CanHandleScenario buildScenario(Event e, PreSubmitCallbackStage aboutToStart, boolean b) {
        return CanHandleScenario.builder()
            .event(e)
            .preSubmitCallbackStage(aboutToStart)
            .expected(b)
            .build();
    }

    @Value
    @Builder
    private static class CanHandleScenario {
        Event event;
        PreSubmitCallbackStage preSubmitCallbackStage;
        boolean expected;
    }

    @Test
    void canHandleRequireNonNull() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("callback must not be null");
    }

    @Test
    void getDispatchPriority() {
        assertThat(handler.getDispatchPriority()).isEqualTo(DispatchPriority.EARLIEST);
    }

    @ParameterizedTest
    @MethodSource("handleScenarioProvider")
    void handle(HandleScenario scenario) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(scenario.getAsylumCase());

        when(caseManagementLocationService.getCaseManagementLocation(scenario.getExpectedStaffLocation()))
            .thenReturn(new CaseManagementLocation(Region.NATIONAL, scenario.getExpectedBaseLocation()));

        PreSubmitCallbackResponse<AsylumCase> actual = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        AsylumCase actualAsylum = actual.getData();
        Optional<CaseManagementLocation> actualCaseManagementLocation = actualAsylum.read(CASE_MANAGEMENT_LOCATION,
            CaseManagementLocation.class);
        assertThat(actualCaseManagementLocation.isPresent()).isTrue();
        assertThat(actualCaseManagementLocation.get().getBaseLocation()).isEqualTo(scenario.getExpectedBaseLocation());

        Optional<String> actualStaffLocation = actualAsylum.read(STAFF_LOCATION, String.class);
        assertThat(actualStaffLocation.isPresent()).isTrue();
        assertThat(actualStaffLocation.get()).isEqualTo(scenario.getExpectedStaffLocation());
    }

    private static Stream<HandleScenario> handleScenarioProvider() {
        final HandleScenario givenCcdCaseDoesNotHaveHearingCentreOrCaseBaseLocationThenDefaultToNewport =
            HandleScenario.builder()
                .asylumCase(new AsylumCase())
                .expectedBaseLocation(BaseLocation.NEWPORT)
                .expectedStaffLocation("Newport")
                .build();

        AsylumCase asylumCaseWithHearingCentreOnly = new AsylumCase();
        asylumCaseWithHearingCentreOnly.write(HEARING_CENTRE, HearingCentre.BIRMINGHAM);
        final HandleScenario givenCcdCaseHasHearingCentreAndDoesNotHaveCaseBaseLocationThenUseHearingCentre =
            HandleScenario.builder()
                .asylumCase(asylumCaseWithHearingCentreOnly)
                .expectedBaseLocation(BaseLocation.BIRMINGHAM)
                .expectedStaffLocation("Birmingham")
                .build();

        AsylumCase asylumCaseWithHearingCentreAndNotCompleteCaseBaseLocation = new AsylumCase();
        asylumCaseWithHearingCentreAndNotCompleteCaseBaseLocation.write(HEARING_CENTRE, HearingCentre.BRADFORD);
        asylumCaseWithHearingCentreAndNotCompleteCaseBaseLocation.write(CASE_MANAGEMENT_LOCATION,
            new CaseManagementLocation(Region.NATIONAL, null));
        final HandleScenario givenCcdCaseHasHearingCentreAndDoesNotHaveCompleteCaseBaseLocationThenUseHearingCentre =
            HandleScenario.builder()
                .asylumCase(asylumCaseWithHearingCentreAndNotCompleteCaseBaseLocation)
                .expectedBaseLocation(BaseLocation.BRADFORD)
                .expectedStaffLocation("Bradford")
                .build();

        AsylumCase asylumCaseWithHearingCentreAndGlasgowBaseLocation = new AsylumCase();
        asylumCaseWithHearingCentreAndGlasgowBaseLocation.write(HEARING_CENTRE, HearingCentre.GLASGOW);
        asylumCaseWithHearingCentreAndGlasgowBaseLocation.write(CASE_MANAGEMENT_LOCATION,
            new CaseManagementLocation(Region.NATIONAL, BaseLocation.GLASGOW_DEPRECATED));

        final HandleScenario givenCcdCaseHasHearingCentreGlasgowAndDoesHaveCompleteCaseBaseLocationButItIsDeprecated =
            HandleScenario.builder()
                .asylumCase(asylumCaseWithHearingCentreAndGlasgowBaseLocation)
                .expectedBaseLocation(BaseLocation.GLASGOW)
                .expectedStaffLocation("Glasgow")
                .build();

        return Stream.of(
            givenCcdCaseDoesNotHaveHearingCentreOrCaseBaseLocationThenDefaultToNewport,
            givenCcdCaseHasHearingCentreAndDoesNotHaveCaseBaseLocationThenUseHearingCentre,
            givenCcdCaseHasHearingCentreAndDoesNotHaveCompleteCaseBaseLocationThenUseHearingCentre,
            givenCcdCaseHasHearingCentreGlasgowAndDoesHaveCompleteCaseBaseLocationButItIsDeprecated
        );
    }

    @Value
    @Builder
    private static class HandleScenario {
        AsylumCase asylumCase;
        String expectedStaffLocation;
        BaseLocation expectedBaseLocation;
    }

}
