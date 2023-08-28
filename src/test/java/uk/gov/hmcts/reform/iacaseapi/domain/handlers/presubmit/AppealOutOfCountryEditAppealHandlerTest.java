package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AppealOutOfCountryEditAppealHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private AppealOutOfCountryEditAppealHandler appealOutOfCountryEditAppealHandler;

    @BeforeEach
    public void setUp() {
        appealOutOfCountryEditAppealHandler = new AppealOutOfCountryEditAppealHandler(featureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_change_to_in_country_clear_out_of_country_details(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(1)).clear(OUT_OF_COUNTRY_DECISION_TYPE);
        verify(asylumCase, times(1)).clear(DECISION_LETTER_RECEIVED_DATE);
        verify(asylumCase, times(1)).clear(HAS_SPONSOR);
        verify(asylumCase, times(1)).clear(OUT_OF_COUNTRY_MOBILE_NUMBER);
        clearHumanRightsDecision(asylumCase);
        clearRefusalOfProtection(asylumCase);
        clearSponsor(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_HO_decision_date_when_switching_to_ada_case(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(1)).clear(OUT_OF_COUNTRY_DECISION_TYPE);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, times(1)).clear(HAS_SPONSOR);
        verify(asylumCase, times(1)).clear(OUT_OF_COUNTRY_MOBILE_NUMBER);
        clearHumanRightsDecision(asylumCase);
        clearRefusalOfProtection(asylumCase);
        clearSponsor(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_change_to_out_of_country_refusal_of_hr_clear_in_country_details(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(
            Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).read(HAS_SPONSOR, YesOrNo.class);
        clearSponsor(asylumCase);
        verify(asylumCase, times(1)).read(
            OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        clearRefusalOfProtection(asylumCase);
        verify(asylumCase, times(1)).clear(DECISION_LETTER_RECEIVED_DATE);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(DEPORTATION_ORDER_OPTIONS);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_DECISION_DATE);

        verify(asylumCase, Mockito.times(1)).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).clear(IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_FACILITY);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_STATUS);
        verify(asylumCase, Mockito.times(1)).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, Mockito.times(1)).clear(IRC_NAME);
        verify(asylumCase, Mockito.times(1)).clear(PRISON_NAME);
        clearAdaSuitabilityFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_change_to_out_of_country_refusal_of_protection_clear_in_country_details(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(
            Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).read(HAS_SPONSOR, YesOrNo.class);
        clearSponsor(asylumCase);
        verify(asylumCase, times(1)).read(
            OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        clearHumanRightsDecision(asylumCase);
        clearAdaSuitabilityFields(asylumCase);

        verify(asylumCase, Mockito.times(1)).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).clear(IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_FACILITY);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_STATUS);
        verify(asylumCase, Mockito.times(1)).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, Mockito.times(1)).clear(IRC_NAME);
        verify(asylumCase, Mockito.times(1)).clear(PRISON_NAME);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_change_to_out_of_country_removal_of_client_clear_in_country_details(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(
            Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).read(HAS_SPONSOR, YesOrNo.class);
        clearSponsor(asylumCase);
        verify(asylumCase, times(1)).read(
            OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        clearHumanRightsDecision(asylumCase);
        clearRefusalOfProtection(asylumCase);
        clearAdaSuitabilityFields(asylumCase);

        verify(asylumCase, Mockito.times(1)).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).clear(IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_FACILITY);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_STATUS);
        verify(asylumCase, Mockito.times(1)).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, Mockito.times(1)).clear(IRC_NAME);
        verify(asylumCase, Mockito.times(1)).clear(PRISON_NAME);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_out_of_country_sponsor_mobile_for_change_to_email(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(
            Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).read(HAS_SPONSOR, YesOrNo.class);
        verify(asylumCase, times(1)).read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class);
        verify(asylumCase, times(1)).clear(SPONSOR_MOBILE_NUMBER);
        verify(asylumCase, times(1)).read(
            OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        clearHumanRightsDecision(asylumCase);
        clearRefusalOfProtection(asylumCase);
        clearAdaSuitabilityFields(asylumCase);

        verify(asylumCase, Mockito.times(1)).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).clear(IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_FACILITY);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_STATUS);
        verify(asylumCase, Mockito.times(1)).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, Mockito.times(1)).clear(IRC_NAME);
        verify(asylumCase, Mockito.times(1)).clear(PRISON_NAME);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_out_of_country_sponsor_email_for_change_to_phone(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(ContactPreference.WANTS_SMS));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(
            Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).read(HAS_SPONSOR, YesOrNo.class);
        verify(asylumCase, times(1)).read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class);
        verify(asylumCase, times(1)).clear(SPONSOR_EMAIL);
        verify(asylumCase, times(1)).read(
            OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        clearHumanRightsDecision(asylumCase);
        clearRefusalOfProtection(asylumCase);
        clearAdaSuitabilityFields(asylumCase);

        verify(asylumCase, Mockito.times(1)).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).clear(IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_FACILITY);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_STATUS);
        verify(asylumCase, Mockito.times(1)).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, Mockito.times(1)).clear(IRC_NAME);
        verify(asylumCase, Mockito.times(1)).clear(PRISON_NAME);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

        assertThatThrownBy(
            () -> appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
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
                when(featureToggler.getValue("out-of-country-feature", false)).thenReturn(true);

                boolean canHandle = appealOutOfCountryEditAppealHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (event.equals(Event.START_APPEAL) || event.equals(Event.EDIT_APPEAL) || event.equals(Event.EDIT_APPEAL_AFTER_SUBMIT))) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }

            //reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealOutOfCountryEditAppealHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> appealOutOfCountryEditAppealHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealOutOfCountryEditAppealHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> appealOutOfCountryEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private void clearHumanRightsDecision(AsylumCase asylumCase) {
        verify(asylumCase, times(1)).clear(GWF_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(DATE_ENTRY_CLEARANCE_DECISION);
    }

    private void clearRefusalOfProtection(AsylumCase asylumCase) {
        verify(asylumCase, times(1)).clear(DATE_CLIENT_LEAVE_UK);
    }

    private void clearSponsor(AsylumCase asylumCase) {
        verify(asylumCase, times(1)).clear(SPONSOR_GIVEN_NAMES);
        verify(asylumCase, times(1)).clear(SPONSOR_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(SPONSOR_ADDRESS);
        verify(asylumCase, times(1)).clear(SPONSOR_CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(SPONSOR_EMAIL);
        verify(asylumCase, times(1)).clear(SPONSOR_MOBILE_NUMBER);
        verify(asylumCase, times(1)).clear(SPONSOR_AUTHORISATION);
        verify(asylumCase, times(1)).clear(SPONSOR_NAME_FOR_DISPLAY);
        verify(asylumCase, times(1)).clear(SPONSOR_ADDRESS_FOR_DISPLAY);
    }

    private void clearAdaSuitabilityFields(AsylumCase asylumCase) {
        verify(asylumCase, times(1)).clear(SUITABILITY_HEARING_TYPE_YES_OR_NO);
        verify(asylumCase, times(1)).clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
        verify(asylumCase, times(1)).clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
        verify(asylumCase, times(1)).clear(SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
        verify(asylumCase, times(1)).clear(SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
    }

}
