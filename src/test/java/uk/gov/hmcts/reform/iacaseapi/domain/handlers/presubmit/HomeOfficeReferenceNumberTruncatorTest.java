package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_APPEAL_ADMIN_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances.LEAVE_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSAL_OF_PROTECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REMOVAL_OF_CLIENT;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeReferenceNumberTruncatorTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private HomeOfficeReferenceNumberTruncator homeOfficeReferenceNumberTruncator =
        new HomeOfficeReferenceNumberTruncator();

    @ParameterizedTest
    @MethodSource("truncatorScenarios")
    void should_truncate_home_office_reference_numbers_for_submit_appeal(
        YesOrNo isAdmin,
        YesOrNo isAgeAssessment,
        OutOfCountryDecisionType outOfCountryDecisionType,
        OutOfCountryCircumstances outOfCountryCircumstances
    ) {

        Map<String, String> exampleInputOutputs =
            ImmutableMap.<String, String>builder()
                .put("1234567", "1234567")
                .put("123456", "123456")
                .put("2266-7654-9987-1234", "2266-7654-9987-1234")
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of(input));

                when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
                when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(isAgeAssessment));
                when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(outOfCountryDecisionType));
                when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(outOfCountryCircumstances));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, times(1)).write(HOME_OFFICE_REFERENCE_NUMBER, output);

                reset(asylumCase);
            });
    }

    @Test
    void should_not_touch_home_office_reference_numbers_when_ooc_and_refusal_of_human_rights_is_decided() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());

        reset(asylumCase);
    }

    @Test
    void should_not_do_anything_when_age_assessment_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.AG));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());

        reset(asylumCase);
    }

    @Test
    void should_not_touch_home_office_reference_numbers_that_are_not_too_long_for_submit_appeal() {

        Map<String, String> exampleInputOutputs =
            ImmutableMap
                .of("", "");

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String input = inputOutput.getKey();
                final String output = inputOutput.getValue();

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of(input));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());
                verify(asylumCase, never()).write(HOME_OFFICE_REFERENCE_NUMBER, output);

                reset(asylumCase);
            });
    }

    @ParameterizedTest
    @MethodSource("truncatorScenarios")
    void should_throw_when_home_office_reference_is_not_present_for_submit_appeal(
        YesOrNo isAdmin,
        YesOrNo isAgeAssessment,
        OutOfCountryDecisionType outOfCountryDecisionType,
        OutOfCountryCircumstances outOfCountryCircumstances
    ) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.empty());

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(isAgeAssessment));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(outOfCountryDecisionType));
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(outOfCountryCircumstances));

        assertThatThrownBy(
            () -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("homeOfficeReferenceNumber is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(
            () -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);

                boolean canHandle = homeOfficeReferenceNumberTruncator.canHandle(callbackStage, callback);

                if (Arrays.asList(
                        Event.SUBMIT_APPEAL)
                    .contains(callback.getEvent())
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> homeOfficeReferenceNumberTruncator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeReferenceNumberTruncator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> homeOfficeReferenceNumberTruncator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> truncatorScenarios() {
        return Stream.of(
            Arguments.of(YesOrNo.NO, YesOrNo.NO, REFUSAL_OF_PROTECTION, LEAVE_UK),
            Arguments.of(YesOrNo.NO, YesOrNo.NO, REMOVAL_OF_CLIENT, LEAVE_UK),
            Arguments.of(YesOrNo.YES, YesOrNo.NO, REMOVAL_OF_CLIENT, LEAVE_UK),
            Arguments.of(YesOrNo.YES, YesOrNo.NO, REMOVAL_OF_CLIENT, OutOfCountryCircumstances.NONE)
        );
    }
}
