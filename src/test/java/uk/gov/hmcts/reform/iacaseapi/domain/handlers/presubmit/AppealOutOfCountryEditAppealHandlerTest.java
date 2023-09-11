package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CLIENT_LEAVE_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_AUTHORISATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
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
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_change_to_in_country_clear_out_of_country_details(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
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
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
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

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
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

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
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

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
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
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
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
                    && (event.equals(Event.EDIT_APPEAL) || event.equals(Event.EDIT_APPEAL_AFTER_SUBMIT))) {
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
        verify(asylumCase, times(1)).clear(SPONSOR_PARTY_ID);
    }

}
