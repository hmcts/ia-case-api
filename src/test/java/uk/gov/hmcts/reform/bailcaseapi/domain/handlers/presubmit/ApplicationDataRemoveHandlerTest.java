package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.AGREES_TO_BOUND_BY_FINANCIAL_COND;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETENTION_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GENDER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GENDER_OTHER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_HAS_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_HAS_MOBILE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_EVIDENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DISABILITY_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_COND_AMOUNT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_LEGAL_REP;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NO_TRANSFER_BAIL_MANAGEMENT_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_OPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_PREVIOUS_BAIL_APPLICATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_BEEN_REFUSED_BAIL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ApplicationDataRemoveHandlerTest {
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    private ApplicationDataRemoveHandler applicationDataRemoveHandler;

    @BeforeEach
    void setUp() {
        reset(callback);
        applicationDataRemoveHandler = new ApplicationDataRemoveHandler();
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = applicationDataRemoveHandler.canHandle(callbackStage, callback);
                assertThat(canHandle).isEqualTo(
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (event.equals(Event.EDIT_BAIL_APPLICATION)
                            || event.equals(Event.MAKE_NEW_APPLICATION)
                            || event.equals(Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT))
                );
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> applicationDataRemoveHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationDataRemoveHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationDataRemoveHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_remove_supporters_if_no_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFinancialConditionSupporter1Removed();
        assertFinancialConditionSupporter2Removed();
        assertFinancialConditionSupporter3Removed();
        assertFinancialConditionSupporter4Removed();
    }

    @Test
    void should_remove_supporter_other_values_if_supporter1_present() {
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        assertFinancialConditionSupporter2Removed();
        assertFinancialConditionSupporter3Removed();
        assertFinancialConditionSupporter4Removed();
    }

    @Test
    void should_remove_supporter_other_values_if_supporter2_present() {
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        assertFinancialConditionSupporter3Removed();
        assertFinancialConditionSupporter4Removed();
    }

    @Test
    void should_remove_supporter_other_values_if_supporter3_present() {
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_3_GIVEN_NAMES);
        assertFinancialConditionSupporter4Removed();
    }

    @Test
    void should_remove_supporter_other_values_if_supporter4_present() {
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_3_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_4_GIVEN_NAMES);
    }

    @Test
    void should_remove_transfer_management_reason_if_agreed() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(NO_TRANSFER_BAIL_MANAGEMENT_REASONS);
    }

    @Test
    void should_remove_financial_condition_amt_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(FINANCIAL_COND_AMOUNT);
    }

    @Test
    void should_remove_previous_application_details_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_BEEN_REFUSED_BAIL);
        verify(bailCase, times(1)).remove(BAIL_HEARING_DATE);
    }

    @Test
    void should_remove_appeal_reference_number_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPEAL_REFERENCE_NUMBER);
    }

    @Test
    void should_remove_video_hearing_details_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(VIDEO_HEARING_DETAILS);
    }

    @Test
    void should_remove_applicant_address_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_ADDRESS);
    }

    @Test
    void should_remove_disability_details_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_DISABILITY_DETAILS);
    }

    @Test
    void should_remove_mobile_details_if_none_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_MOBILE_NUMBER);
    }

    @Test
    void should_remove_gender_other_details_if_male_female() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_GENDER_OTHER);
    }

    @Test
    void should_remove_prison_details_if_irc() {
        setUpValuesIfValuesAreRemoved();
        //For IRC
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(PRISON_NAME);
        verify(bailCase, times(1)).remove(APPLICANT_PRISON_DETAILS);
        verify(bailCase, never()).remove(IRC_NAME);
    }

    @Test
    void should_remove_prison_details_if_prison() {
        setUpValuesIfValuesAreRemoved();
        //For Prison
        when(bailCase.read(APPLICANT_DETENTION_LOCATION, String.class)).thenReturn(Optional.of("prison"));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(IRC_NAME);
        verify(bailCase, never()).remove(PRISON_NAME);

    }

    @Test
    void should_remove_bail_evidence_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(BAIL_EVIDENCE);
    }

    @Test
    void should_remove_interpreter_language_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(INTERPRETER_LANGUAGES);
    }

    @Test
    void should_remove_LR_details_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(LEGAL_REP_COMPANY);
        verify(bailCase, times(1)).remove(LEGAL_REP_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(LEGAL_REP_NAME);
        verify(bailCase, times(1)).remove(LEGAL_REP_PHONE);
        verify(bailCase, times(1)).remove(LEGAL_REP_REFERENCE);
        verify(bailCase, times(1)).write(IS_LEGALLY_REPRESENTED_FOR_FLAG, NO);
    }


    @Test
    void should_remove_nationalities_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_NATIONALITIES);
    }

    @Test
    void should_not_remove_if_values_present() {
        setUpValuesIfValuesArePresent();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(NO_TRANSFER_BAIL_MANAGEMENT_REASONS);
        verify(bailCase, never()).remove(FINANCIAL_COND_AMOUNT);
        verify(bailCase, never()).remove(APPLICANT_ADDRESS);
        verify(bailCase, never()).remove(APPEAL_REFERENCE_NUMBER);
        verify(bailCase, never()).remove(VIDEO_HEARING_DETAILS);
        verify(bailCase, never()).remove(APPLICANT_DISABILITY_DETAILS);
        verify(bailCase, never()).remove(APPLICANT_MOBILE_NUMBER);
        verify(bailCase, never()).remove(APPLICANT_GENDER_OTHER);
        verify(bailCase, never()).remove(BAIL_EVIDENCE);
        verify(bailCase, never()).remove(APPLICANT_NATIONALITIES);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_3_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_4_GIVEN_NAMES);
        verify(bailCase, never()).remove(APPLICANT_BEEN_REFUSED_BAIL);
        verify(bailCase, never()).remove(BAIL_HEARING_DATE);
    }

    private void setUpValuesIfValuesAreRemoved() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(
            GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION,
            YesOrNo.class
        )).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(DISABILITY_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_NATIONALITY, String.class)).thenReturn(Optional.of("STATELESS"));
        when(bailCase.read(APPLICANT_DETENTION_LOCATION, String.class)).thenReturn(Optional.of(
            "immigrationRemovalCentre"));
        when(bailCase.read(APPLICANT_GENDER, String.class)).thenReturn(Optional.of("male"));
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class)).thenReturn(Optional.of("No"));
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("No"));
    }

    private void setUpValuesIfValuesArePresent() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(
            GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION,
            YesOrNo.class
        )).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(DISABILITY_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(APPLICANT_NATIONALITY, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_DETENTION_LOCATION, String.class)).thenReturn(Optional.of(
            "immigrationRemovalCentre"));
        when(bailCase.read(APPLICANT_GENDER, String.class)).thenReturn(Optional.of("other"));
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class)).thenReturn(Optional.of("Yes"));
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("Yes"));
    }

    private void assertFinancialConditionSupporter1Removed() {
        verify(bailCase, times(1)).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_FAMILY_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_ADDRESS_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_CONTACT_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_TELEPHONE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_MOBILE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(SUPPORTER_DOB);
        verify(bailCase, times(1)).remove(SUPPORTER_RELATION);
        verify(bailCase, times(1)).remove(SUPPORTER_OCCUPATION);
        verify(bailCase, times(1)).remove(SUPPORTER_IMMIGRATION);
        verify(bailCase, times(1)).remove(SUPPORTER_NATIONALITY);
        verify(bailCase, times(1)).remove(SUPPORTER_HAS_PASSPORT);
        verify(bailCase, times(1)).remove(SUPPORTER_PASSPORT);
        verify(bailCase, times(1)).remove(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES);
    }

    private void assertFinancialConditionSupporter2Removed() {
        verify(bailCase, times(1)).remove(SUPPORTER_2_GIVEN_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_2_FAMILY_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_2_ADDRESS_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_2_CONTACT_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_2_TELEPHONE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_2_MOBILE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_2_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(SUPPORTER_2_DOB);
        verify(bailCase, times(1)).remove(SUPPORTER_2_RELATION);
        verify(bailCase, times(1)).remove(SUPPORTER_2_OCCUPATION);
        verify(bailCase, times(1)).remove(SUPPORTER_2_IMMIGRATION);
        verify(bailCase, times(1)).remove(SUPPORTER_2_NATIONALITY);
        verify(bailCase, times(1)).remove(SUPPORTER_2_HAS_PASSPORT);
        verify(bailCase, times(1)).remove(SUPPORTER_2_PASSPORT);
        verify(bailCase, times(1)).remove(FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES);
    }

    private void assertFinancialConditionSupporter3Removed() {
        verify(bailCase, times(1)).remove(SUPPORTER_3_GIVEN_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_3_FAMILY_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_3_ADDRESS_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_3_CONTACT_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_3_TELEPHONE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_3_MOBILE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_3_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(SUPPORTER_3_DOB);
        verify(bailCase, times(1)).remove(SUPPORTER_3_RELATION);
        verify(bailCase, times(1)).remove(SUPPORTER_3_OCCUPATION);
        verify(bailCase, times(1)).remove(SUPPORTER_3_IMMIGRATION);
        verify(bailCase, times(1)).remove(SUPPORTER_3_NATIONALITY);
        verify(bailCase, times(1)).remove(SUPPORTER_3_HAS_PASSPORT);
        verify(bailCase, times(1)).remove(SUPPORTER_3_PASSPORT);
        verify(bailCase, times(1)).remove(FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES);
    }

    private void assertFinancialConditionSupporter4Removed() {
        verify(bailCase, times(1)).remove(SUPPORTER_4_GIVEN_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_4_FAMILY_NAMES);
        verify(bailCase, times(1)).remove(SUPPORTER_4_ADDRESS_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_4_CONTACT_DETAILS);
        verify(bailCase, times(1)).remove(SUPPORTER_4_TELEPHONE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_4_MOBILE_NUMBER);
        verify(bailCase, times(1)).remove(SUPPORTER_4_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(SUPPORTER_4_DOB);
        verify(bailCase, times(1)).remove(SUPPORTER_4_RELATION);
        verify(bailCase, times(1)).remove(SUPPORTER_4_OCCUPATION);
        verify(bailCase, times(1)).remove(SUPPORTER_4_IMMIGRATION);
        verify(bailCase, times(1)).remove(SUPPORTER_4_NATIONALITY);
        verify(bailCase, times(1)).remove(SUPPORTER_4_HAS_PASSPORT);
        verify(bailCase, times(1)).remove(SUPPORTER_4_PASSPORT);
        verify(bailCase, times(1)).remove(FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES);
    }


}
