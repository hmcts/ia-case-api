package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.LATEST;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OrganisationOnDecisionLetter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AgeAssessmentDataEditAppealHandlerTest {

    AgeAssessmentDataEditAppealHandler ageAssessmentDataEditAppealHandler;

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        ageAssessmentDataEditAppealHandler = new AgeAssessmentDataEditAppealHandler();
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseFieldDefinition.AGE_ASSESSMENT, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
    }

    @Test
    void set_to_latest() {
        assertThat(ageAssessmentDataEditAppealHandler.getDispatchPriority()).isEqualTo(LATEST);
    }

    @Test
    void should_remove_hsc_trust_details_if_now_local_authority() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
            .thenReturn(Optional.of(OrganisationOnDecisionLetter.LOCAL_AUTHORITY.toString()));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HSC_TRUST);
    }

    @Test
    void should_remove_hsc_trust_details_if_now_national_age_assessment_board() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.NATIONAL_AGE_ASSESSMENT_BOARD.toString()));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LOCAL_AUTHORITY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HSC_TRUST);
    }

    @Test
    void should_remove_hsc_trust_details_if_now_hsc_trust() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.HSC_TRUST.toString()));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LOCAL_AUTHORITY);
    }

    @Test
    void should_remove_litigation_friend_details_if_now_no_litigation_friend() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.LOCAL_AUTHORITY.toString()));
        when(asylumCase.read(AsylumCaseFieldDefinition.LITIGATION_FRIEND, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_GIVEN_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_COMPANY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_PHONE_NUMBER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_EMAIL);
    }

    @Test
    void should_remove_litigation_friend_phone_number_if_now_wants_email() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.LOCAL_AUTHORITY.toString()));
        when(asylumCase.read(AsylumCaseFieldDefinition.LITIGATION_FRIEND, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.LITIGATION_FRIEND_CONTACT_PREFERENCE, ContactPreference.class))
                .thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_PHONE_NUMBER);
    }

    @Test
    void should_remove_litigation_friend_phone_number_if_now_wants_sms() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.LOCAL_AUTHORITY.toString()));
        when(asylumCase.read(AsylumCaseFieldDefinition.LITIGATION_FRIEND, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.LITIGATION_FRIEND_CONTACT_PREFERENCE, ContactPreference.class))
                .thenReturn(Optional.of(ContactPreference.WANTS_SMS));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_EMAIL);
    }

    @Test
    void should_remove_all_age_assessment_details_if_now_not_age_assessment() {
        when(asylumCase.read(AsylumCaseFieldDefinition.AGE_ASSESSMENT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LOCAL_AUTHORITY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HSC_TRUST);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DECISION_LETTER_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.AA_APPELLANT_DATE_OF_BIRTH);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_GIVEN_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_COMPANY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_PHONE_NUMBER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_EMAIL);
    }

    @Test
    void should_remove_all_age_assessment_details_if_now_ada() {
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.AGE_ASSESSMENT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.AGE_ASSESSMENT);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LOCAL_AUTHORITY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HSC_TRUST);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DECISION_LETTER_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.AA_APPELLANT_DATE_OF_BIRTH);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_GIVEN_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_COMPANY);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_PHONE_NUMBER);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.LITIGATION_FRIEND_EMAIL);
    }

    @Test
    void should_remove_hearing_type_hearing_fee() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.HSC_TRUST.toString()));

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION);
    }

    @Test
    void should_remove_hearing_type_rp_dc() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER, String.class))
                .thenReturn(Optional.of(OrganisationOnDecisionLetter.HSC_TRUST.toString()));

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> response = ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.RP_DC_APPEAL_HEARING_OPTION);
    }

    @Test
    void handling_should_throw_if_detention_facility_not_available() {
        assertThatThrownBy(() -> ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Organisation on decision letter missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @ParameterizedTest
    @EnumSource(value = JourneyType.class, names = { "AIP", "REP" })
    void check_canHandle(JourneyType journeyType) {

        Optional<JourneyType> isAip = journeyType.equals(JourneyType.AIP)
            ? Optional.of(JourneyType.AIP)
            : Optional.empty();

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(isAip);

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean canHandle = ageAssessmentDataEditAppealHandler.canHandle(stage, callback);
                if (event == Event.EDIT_APPEAL
                    && stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAip.equals(Optional.empty())) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_argument_null() {
        assertThatThrownBy(() -> ageAssessmentDataEditAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ageAssessmentDataEditAppealHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
