package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FLAG_CASE_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FLAG_CASE_TYPE_OF_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.FLAG_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FlagCaseMidEventHandlerTest {

    private final FlagCaseMidEventHandler handler = new FlagCaseMidEventHandler();
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    void canHandle(CanHandleScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.event);

        boolean actualResult = handler.canHandle(scenario.callbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.expectedResult);
    }

    private static List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @ParameterizedTest
    @CsvSource({
        "CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION, some anonymity additional info, ANONYMITY",
        "CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION, some complex case additional info, COMPLEX_CASE",
        "CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION, some deport additional info, DEPORT",
        "CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION, some detained immigration appeal additional info, DETAINED_IMMIGRATION_APPEAL",
        "CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION, some foreign National Offender additional info, FOREIGN_NATIONAL_OFFENDER",
        "CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION, some potentiallyViolentPerson info, POTENTIALLY_VIOLENT_PERSON",
        "CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION, some unacceptable Customer Behaviour additional info, UNACCEPTABLE_CUSTOMER_BEHAVIOUR",
        "CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION, some unaccompaniedMinor additional info, UNACCOMPANIED_MINOR",
        "CASE_FLAG_SET_ASIDE_REHEARD_ADDITIONAL_INFORMATION, some setAsideReheard additional info, SET_ASIDE_REHEARD",
        "CASE_FLAG_S94B_OUT_OF_COUNTRY_ADDITIONAL_INFORMATION, some Section 94B additional info, S94B_OUT_OF_COUNTRY",
        "CASE_FLAG_APPEAL_ON_HOLD_ADDITIONAL_INFORMATION, this appeal on hold reason, APPEAL_ON_HOLD"
    })
    void given_existing_flag_should_populate_additional_information_field(
        AsylumCaseFieldDefinition caseFlagFieldDefinition, String existingAdditionalInfo, CaseFlagType caseFlagType) {

        given(callback.getEvent()).willReturn(FLAG_CASE);
        given(callback.getCaseDetails()).willReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(caseFlagFieldDefinition, existingAdditionalInfo);
        asylumCase.write(FLAG_CASE_TYPE_OF_FLAG, caseFlagType);
        given(caseDetails.getCaseData()).willReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> actualAsylumCase = handler.handle(MID_EVENT, callback);

        String actualAdditionalInfo = actualAsylumCase.getData()
            .read(FLAG_CASE_ADDITIONAL_INFORMATION, String.class).orElse(StringUtils.EMPTY);

        assertThat(actualAdditionalInfo).isEqualTo(existingAdditionalInfo);
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

    @ParameterizedTest
    @CsvSource({
        "FLAG_CASE, ABOUT_TO_SUBMIT",
        "FLAG_CASE, ABOUT_TO_START",
        "START_APPEAL, MID_EVENT"
    })
    void should_throw_illegal_state_exception(Event event, PreSubmitCallbackStage callbackStage) {
        given(callback.getEvent()).willReturn(event);

        assertThatThrownBy(() -> handler.handle(callbackStage, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Value
    private static class CanHandleScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean expectedResult;

        private static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (FLAG_CASE.equals(e)) {
                    addScenario(scenarios, e, true);
                } else {
                    addScenario(scenarios, e, false);
                }
            }
            return scenarios;
        }

        private static void addScenario(List<CanHandleScenario> scenarios, Event event, boolean expected) {
            scenarios.add(new CanHandleScenario(event, ABOUT_TO_START, false));
            scenarios.add(new CanHandleScenario(event, ABOUT_TO_SUBMIT, false));
            scenarios.add(new CanHandleScenario(event, MID_EVENT, expected));
        }
    }
}
