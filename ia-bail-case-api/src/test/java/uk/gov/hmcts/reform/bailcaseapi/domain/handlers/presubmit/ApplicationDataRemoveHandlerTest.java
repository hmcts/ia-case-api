package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ApplicationDataRemoveHandlerTest {
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    private ApplicationDataRemoveHandler applicationDataRemoveHandler;

    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;
    @Captor
    private ArgumentCaptor<InterpreterLanguageRefData> bailValueCaptor;

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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_BAIL_APPLICATION", "MAKE_NEW_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"})
    void should_remove_supporters_if_no_present(Event event) {
        when(callback.getEvent()).thenReturn(event);
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFinancialConditionSupporter1Removed();
        assertFinancialConditionSupporter2Removed();
        assertFinancialConditionSupporter3Removed();
        assertFinancialConditionSupporter4Removed();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_BAIL_APPLICATION", "MAKE_NEW_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"})
    void should_remove_supporter_other_values_if_supporter1_present(Event event) {
        setUpValuesIfValuesAreRemoved();
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        assertFinancialConditionSupporter2Removed();
        assertFinancialConditionSupporter3Removed();
        assertFinancialConditionSupporter4Removed();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_BAIL_APPLICATION", "MAKE_NEW_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"})
    void should_remove_supporter_other_values_if_supporter2_present(Event event) {
        when(callback.getEvent()).thenReturn(event);
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        assertFinancialConditionSupporter3Removed();
        assertFinancialConditionSupporter4Removed();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_BAIL_APPLICATION", "MAKE_NEW_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"})
    void should_remove_supporter_other_values_if_supporter3_present(Event event) {
        when(callback.getEvent()).thenReturn(event);
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_3_GIVEN_NAMES);
        assertFinancialConditionSupporter4Removed();
    }

    @Test
    void should_remove_supporter_other_values_if_supporter4_present() {
        setUpValuesIfValuesAreRemoved();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(SUPPORTER_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_2_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_3_GIVEN_NAMES);
        verify(bailCase, never()).remove(SUPPORTER_4_GIVEN_NAMES);
    }

    @Test
    void should_remove_old_transfer_management_reason_if_agreed() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(NO_TRANSFER_BAIL_MANAGEMENT_REASONS);
    }

    @Test
    void should_remove_new_transfer_management_reason_if_agreed() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(OBJECTED_TRANSFER_BAIL_MANAGEMENT_REASONS);
        verify(bailCase, times(0)).remove(TRANSFER_BAIL_MANAGEMENT_OPTION);
        verify(bailCase, times(0)).remove(TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION);
    }


    @Test
    void should_remove_old_transfer_management_values_if_new_present() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(bailCase.read(NO_TRANSFER_BAIL_MANAGEMENT_REASONS, String.class)).thenReturn(Optional.of("reason"));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(0)).remove(OBJECTED_TRANSFER_BAIL_MANAGEMENT_REASONS);
        verify(bailCase, times(1)).remove(TRANSFER_BAIL_MANAGEMENT_OPTION);
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
        verify(bailCase, times(1)).remove(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE);
    }

    @Test
    void should_remove_LR_details_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(LEGAL_REP_COMPANY);
        verify(bailCase, times(1)).remove(LEGAL_REP_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(LEGAL_REP_NAME);
        verify(bailCase, times(1)).remove(LEGAL_REP_FAMILY_NAME);
        verify(bailCase, times(1)).remove(LEGAL_REP_PHONE);
        verify(bailCase, times(1)).remove(LEGAL_REP_REFERENCE);
        verify(bailCase, times(1)).write(IS_LEGALLY_REPRESENTED_FOR_FLAG, NO);
    }

    @Test
    void should_remove_POM_details_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(PROBATION_OFFENDER_MANAGER_GIVEN_NAME);
        verify(bailCase, times(1)).remove(PROBATION_OFFENDER_MANAGER_FAMILY_NAME);
        verify(bailCase, times(1)).remove(PROBATION_OFFENDER_MANAGER_CONTACT_DETAILS);
        verify(bailCase, times(1)).remove(PROBATION_OFFENDER_MANAGER_EMAIL_ADDRESS);
        verify(bailCase, times(1)).remove(PROBATION_OFFENDER_MANAGER_MOBILE_NUMBER);
        verify(bailCase, times(1)).remove(PROBATION_OFFENDER_MANAGER_TELEPHONE_NUMBER);
    }


    @Test
    void should_remove_nationalities_if_not_present() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_NATIONALITIES);
    }

    @Test
    void should_set_fcsInterpreterYesNo_to_no_when_fcs_removed() {
        setUpValuesIfValuesAreRemoved();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).write(FCS_INTERPRETER_YESNO, NO);
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

    @Test
    void should_remove_spoken_language_if_category_was_updated_to_sign() {
        List<String> category = List.of("signLanguageInterpreter");
        final DynamicList spokenLanguage = new DynamicList(new Value("1", "English"), List.of(new Value("1", "English")));
        final DynamicList signLanguage = new DynamicList(new Value("1", "Makaton"), List.of(new Value("1", "Makaton")));
        setUpValuesIfValuesArePresent();
        when(bailCase.read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguage));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(signLanguage));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING);
        verify(bailCase, times(1)).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
    }

    @Test
    void should_not_remove_language_fields_if_category_was_updated_to_both() {
        List<String> category = List.of("signLanguageInterpreter", "spokenLanguageInterpreter");
        final DynamicList spokenLanguage = new DynamicList(new Value("1", "English"), List.of(new Value("1", "English")));
        final DynamicList signLanguage = new DynamicList(new Value("1", "Makaton"), List.of(new Value("1", "Makaton")));
        setUpValuesIfValuesArePresent();
        when(bailCase.read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguage));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(signLanguage));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING);
        verify(bailCase, never()).remove(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
    }

    @Test
    void should_remove_all_fcs_interpreter_details_if_fcs_interpreterNeeded_updated_to_no() {
        when(bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(NO));
        setUpValuesIfValuesArePresent();
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);

        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);

        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);

        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
    }

    @Test
    void should_sanitize_all_fcs_sign_interpreter_details_if_fcs_interpreterNeeded_updated_to_yes() {
        //when only sign interpreter details are present, make sure spoken language details are removed
        when(bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        setUpValuesIfValuesArePresent();
        List<String> category = List.of("signLanguageInterpreter");
        final DynamicList signLanguage = new DynamicList(new Value("1", "Makaton"), List.of(new Value("1", "Makaton")));
        when(bailCase.read(FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS1_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS1_INTERPRETER_SIGN_LANGUAGE)).thenReturn((Optional.of(signLanguage)));
        when(bailCase.read(FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS2_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS2_INTERPRETER_SIGN_LANGUAGE)).thenReturn((Optional.of(signLanguage)));
        when(bailCase.read(FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS3_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS3_INTERPRETER_SIGN_LANGUAGE)).thenReturn((Optional.of(signLanguage)));
        when(bailCase.read(FCS4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS4_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS4_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(signLanguage));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
    }

    @Test
    void should_sanitize_all_fcs_spoken_interpreter_details_if_fcs_interpreterNeeded_updated_to_yes() {
        //when only spoken interpreter details are present, make sure sign language details are removed
        when(bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        setUpValuesIfValuesArePresent();
        List<String> category = List.of("spokenLanguageInterpreter");
        final DynamicList spokenLanguage = new DynamicList(new Value("1", "English"), List.of(new Value("1", "English")));
        when(bailCase.read(FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS1_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguage));
        when(bailCase.read(FCS1_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS2_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguage));
        when(bailCase.read(FCS2_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS3_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguage));
        when(bailCase.read(FCS3_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(FCS4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(FCS4_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguage));
        when(bailCase.read(FCS4_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.empty());
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
    }

    @Test
    void should_clear_isDetentionLocationCorrect_if_present_for_edit_application_post_submit() {
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
        when(bailCase.read(IS_DETENTION_LOCATION_CORRECT, YesOrNo.class)).thenReturn(Optional.of(NO));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).clear(IS_DETENTION_LOCATION_CORRECT);

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
        when(bailCase.read(HAS_PROBATION_OFFENDER_MANAGER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(DISABILITY_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(APPLICANT_NATIONALITY, String.class)).thenReturn(Optional.of("STATELESS"));
        when(bailCase.read(APPLICANT_DETENTION_LOCATION, String.class)).thenReturn(Optional.of(
            "immigrationRemovalCentre"));
        when(bailCase.read(APPLICANT_GENDER, String.class)).thenReturn(Optional.of("male"));
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class)).thenReturn(Optional.of("No"));
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("No"));
    }

    private void setUpValuesIfValuesArePresent() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(
            GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION,
            YesOrNo.class
        )).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(HAS_PROBATION_OFFENDER_MANAGER, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(DISABILITY_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class)).thenReturn(Optional.of(YES));
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
        verify(bailCase, times(1)).write(HAS_FINANCIAL_COND_SUPPORTER, NO);
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
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS1_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
    }

    private void assertFinancialConditionSupporter2Removed() {
        verify(bailCase, times(1)).removeByString(HAS_FINANCIAL_COND_SUPPORTER_2.value());
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
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS2_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
    }

    private void assertFinancialConditionSupporter3Removed() {
        verify(bailCase, times(1)).removeByString(HAS_FINANCIAL_COND_SUPPORTER_3.value());
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
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS3_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
    }

    private void assertFinancialConditionSupporter4Removed() {
        verify(bailCase, times(1)).removeByString(HAS_FINANCIAL_COND_SUPPORTER_4.value());
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
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_SIGN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_SPOKEN_LANGUAGE);
        verify(bailCase, times(1)).remove(FCS4_INTERPRETER_LANGUAGE_CATEGORY);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4);
        verify(bailCase, times(1)).remove(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
    }

    @Test
    void should_sanitize_and_create_applicant_sign_interpreter_details_if_languageManualEntry_updated_to_yes() {
        //when sign interpreter details are present and languageManualEntry is Yes, then make sure languageRefData are removed
        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        setUpValuesIfValuesArePresent();
        List<String> category = List.of("signLanguageInterpreter");
        final DynamicList signLanguage = new DynamicList(new Value("1", "Makaton"), List.of(new Value("1", "Makaton")));
        final InterpreterLanguageRefData existingLanguage = new InterpreterLanguageRefData(
            signLanguage,
            "Yes",
            "manual sign language1");
        when(bailCase.read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn((Optional.of(existingLanguage)));
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(bailCase, times(1)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        List<BailCaseFieldDefinition> extractors = bailExtractorCaptor.getAllValues();
        List<InterpreterLanguageRefData> bailCaseValues = bailValueCaptor.getAllValues();

        assertThat(bailCaseValues.get(extractors.indexOf(APPLICANT_INTERPRETER_SIGN_LANGUAGE))
                       .getLanguageRefData()).isEqualTo(null);
        assertThat(bailCaseValues.get(extractors.indexOf(APPLICANT_INTERPRETER_SIGN_LANGUAGE))
                       .getLanguageManualEntryDescription()).isEqualTo("manual sign language1");
    }

    @Test
    void should_sanitize_and_create_applicant_spoken_interpreter_details_if_languageManualEntry_updated_to_no() {
        //when spoken interpreter details are present and languageManualEntry is No, then make sure languageManualEntryDescription are removed
        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YES));
        setUpValuesIfValuesArePresent();
        List<String> category = List.of("spokenLanguageInterpreter");
        final DynamicList spokenLanguage = new DynamicList(new Value("1", "English"), List.of(new Value("1", "English")));
        final InterpreterLanguageRefData existingLanguage = new InterpreterLanguageRefData(
            spokenLanguage,
            "No",
            "manual spoken language1");
        when(bailCase.read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(category));
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn((Optional.of(existingLanguage)));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.empty());
        applicationDataRemoveHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(bailCase, times(1)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        List<BailCaseFieldDefinition> extractors = bailExtractorCaptor.getAllValues();
        List<InterpreterLanguageRefData> bailCaseValues = bailValueCaptor.getAllValues();

        assertThat(bailCaseValues.get(extractors.indexOf(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE))
                       .getLanguageManualEntryDescription()).isEqualTo(null);
        assertThat(bailCaseValues.get(extractors.indexOf(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE))
                       .getLanguageRefData()).isEqualTo(spokenLanguage);
    }


}
