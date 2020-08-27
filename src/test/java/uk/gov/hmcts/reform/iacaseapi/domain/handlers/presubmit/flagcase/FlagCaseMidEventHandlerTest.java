package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.FLAG_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
class FlagCaseMidEventHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    final FlagCaseMidEventHandler handler = new FlagCaseMidEventHandler();
    @Mock private
    CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("canHandleScenarioTestData")
    void canHandle(PreSubmitCallbackStage callbackStage, Event event, boolean expectedResult) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = handler.canHandle(callbackStage, callback);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> canHandleScenarioTestData() {

        return Stream.of(
            Arguments.of(MID_EVENT, FLAG_CASE, true)
        );
    }

    @ParameterizedTest
    @MethodSource("flagFieldsArgumentsTestData")
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

    private static Stream<Arguments> flagFieldsArgumentsTestData() {

        return Stream.of(
            Arguments.of(CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION, "some anonymity additional info", "ANONYMITY"),
            Arguments.of(CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION, "some complex case additional info", "COMPLEX_CASE"),
            Arguments.of(CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION, "some deport additional info", "DEPORT"),
            Arguments.of(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION, "some detained immigration appeal additional info", "DETAINED_IMMIGRATION_APPEAL"),
            Arguments.of(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION, "some foreign National Offender additional info", "FOREIGN_NATIONAL_OFFENDER"),
            Arguments.of(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION, "some potentiallyViolentPerson info", "POTENTIALLY_VIOLENT_PERSON"),
            Arguments.of(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION, "some unacceptable Customer Behaviour additional info", "UNACCEPTABLE_CUSTOMER_BEHAVIOUR"),
            Arguments.of(CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION, "some unaccompaniedMinor additional info", "UNACCOMPANIED_MINOR")
        );
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
    @MethodSource("illegalStateExceptionTestData")
    void should_throw_illegal_state_exception(Event event, PreSubmitCallbackStage callbackStage) {

        assertThatThrownBy(() -> handler.handle(callbackStage, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private static Stream<Arguments> illegalStateExceptionTestData() {

        return Stream.of(
            Arguments.of(FLAG_CASE, ABOUT_TO_SUBMIT),
            Arguments.of(FLAG_CASE, ABOUT_TO_START),
            Arguments.of(START_APPEAL, MID_EVENT)
        );
    }
}