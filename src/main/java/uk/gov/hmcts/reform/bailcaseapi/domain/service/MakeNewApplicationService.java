package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Service
public class MakeNewApplicationService {
    private final Appender<PriorApplication> appender;
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;
    private final ObjectMapper mapper;

    public MakeNewApplicationService(
        Appender<PriorApplication> appender,
        UserDetails userDetails,
        UserDetailsHelper userDetailsHelper,
        ObjectMapper mapper
    ) {
        this.appender = appender;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.mapper = mapper;
    }

    public void appendPriorApplication(BailCase bailCase, BailCase bailCaseBefore) {
        Optional<List<IdValue<PriorApplication>>> maybeExistingPriorApplication =
            bailCaseBefore.read(BailCaseFieldDefinition.PRIOR_APPLICATIONS);

        String nextAppId = String.valueOf(maybeExistingPriorApplication.orElse(Collections.emptyList()).size() + 1);

        List<IdValue<PriorApplication>> allPriorApplications = appender.append(
            buildNewPriorApplication(nextAppId, bailCaseBefore),
            maybeExistingPriorApplication.orElse(Collections.emptyList()));

        bailCase.write(BailCaseFieldDefinition.PRIOR_APPLICATIONS, allPriorApplications);
    }

    private void clearUnrelatedFields(BailCase bailCase, List<String> listOfValidDefinitions) {
        List<String> fieldDefinitionsToBeRemoved = bailCase.keySet()
            .stream()
            .filter(o -> !listOfValidDefinitions.contains(o))
            .collect(Collectors.toList());

        fieldDefinitionsToBeRemoved.forEach(bailCase::removeByString);

        clearRoleDependentFields(bailCase);
    }

    public void clearFieldsAboutToStart(BailCase bailCase) {
        clearUnrelatedFields(bailCase, VALID_ABOUT_TO_START_MAKE_NEW_APPLICATION_FIELDS);
    }

    public void clearFieldsAboutToSubmit(BailCase bailCase) {
        clearUnrelatedFields(bailCase, VALID_ABOUT_TO_SUBMIT_MAKE_NEW_APPLICATION_FIELDS);
    }

    private void clearRoleDependentFields(BailCase bailCase) {
        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true);

        if (userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE)) {
            bailCase.remove(BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS);
        }
        if (userRoleLabel.equals(UserRoleLabel.HOME_OFFICE_BAIL)) {
            bailCase.remove(BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS);
        }
    }

    private PriorApplication buildNewPriorApplication(String nextAppId, BailCase bailCaseBefore) {
        // Clear any application that was saved as Prior Application for this Application.
        // We only want to store immediate previous casedetails, not the ones prior to it.
        bailCaseBefore.clear(BailCaseFieldDefinition.PRIOR_APPLICATIONS);

        // Show only hearing centre information from reference data if available
        if (bailCaseBefore.read(BailCaseFieldDefinition.HEARING_CENTRE_REF_DATA, DynamicList.class).isPresent()) {
            bailCaseBefore.clear(BailCaseFieldDefinition.HEARING_CENTRE);
        }
        if (bailCaseBefore.read(BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION, DynamicList.class).isPresent()) {
            bailCaseBefore.clear(BailCaseFieldDefinition.LISTING_LOCATION);
        }

        String previousCaseDataJson;
        try {
            previousCaseDataJson = mapper.writeValueAsString(bailCaseBefore);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize data", e);
        }

        return new PriorApplication(
            nextAppId,
            previousCaseDataJson
        );
    }

    public BailCase getBailCaseFromString(String caseDataJson) {
        if (caseDataJson == null || caseDataJson.isEmpty()) {
            throw new IllegalArgumentException("CaseData (json) is missing");
        }

        BailCase bailCase;
        try {
            bailCase = mapper.readValue(caseDataJson, BailCase.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert data", e);
        }

        return bailCase;
    }

    private static final List<String> VALID_ABOUT_TO_START_MAKE_NEW_APPLICATION_FIELDS = List.of(
        BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.PRIOR_APPLICATIONS.value(),
        BailCaseFieldDefinition.IS_ADMIN.value(),
        BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.APPLICANT_FAMILY_NAME.value(),
        BailCaseFieldDefinition.APPLICANT_DOB.value(),
        BailCaseFieldDefinition.APPLICANT_GENDER.value(),
        BailCaseFieldDefinition.APPLICANT_GENDER_OTHER.value(),
        BailCaseFieldDefinition.APPLICANT_NATIONALITY.value(),
        BailCaseFieldDefinition.APPLICANT_NATIONALITIES.value(),
        BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS.value(),
        BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK.value(),
        BailCaseFieldDefinition.CASE_NOTES.value(),
        BailCaseFieldDefinition.IS_IMA_ENABLED.value()
    );

    private static final List<String> VALID_ABOUT_TO_SUBMIT_MAKE_NEW_APPLICATION_FIELDS = List.of(
        BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL.value(),
        BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.PRIOR_APPLICATIONS.value(),
        BailCaseFieldDefinition.IS_ADMIN.value(),
        BailCaseFieldDefinition.APPLICATION_SENT_BY.value(),
        BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.APPLICANT_FAMILY_NAME.value(),
        BailCaseFieldDefinition.APPLICANT_DOB.value(),
        BailCaseFieldDefinition.APPLICANT_GENDER.value(),
        BailCaseFieldDefinition.APPLICANT_GENDER_OTHER.value(),
        BailCaseFieldDefinition.APPLICANT_NATIONALITY.value(),
        BailCaseFieldDefinition.APPLICANT_NATIONALITIES.value(),
        BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.APPLICANT_DETENTION_LOCATION.value(),
        BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS.value(),
        BailCaseFieldDefinition.IRC_NAME.value(),
        BailCaseFieldDefinition.PRISON_NAME.value(),
        BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK.value(),
        BailCaseFieldDefinition.APPLICANT_HAS_MOBILE.value(),
        BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER.value(),
        BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING.value(),
        BailCaseFieldDefinition.APPEAL_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING_UT.value(),
        BailCaseFieldDefinition.UT_APPEAL_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.HAS_PREVIOUS_BAIL_APPLICATION.value(),
        BailCaseFieldDefinition.PREV_BAIL_APPLICATION_NUMBER.value(),
        BailCaseFieldDefinition.APPLICANT_BEEN_REFUSED_BAIL.value(),
        BailCaseFieldDefinition.BAIL_HEARING_DATE.value(),
        BailCaseFieldDefinition.APPLICANT_HAS_ADDRESS.value(),
        BailCaseFieldDefinition.APPLICANT_ADDRESS.value(),
        BailCaseFieldDefinition.AGREES_TO_BOUND_BY_FINANCIAL_COND.value(),
        BailCaseFieldDefinition.FINANCIAL_COND_AMOUNT.value(),
        BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER.value(),
        BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_ADDRESS_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_CONTACT_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS.value(),
        BailCaseFieldDefinition.SUPPORTER_DOB.value(),
        BailCaseFieldDefinition.SUPPORTER_RELATION.value(),
        BailCaseFieldDefinition.SUPPORTER_OCCUPATION.value(),
        BailCaseFieldDefinition.SUPPORTER_IMMIGRATION.value(),
        BailCaseFieldDefinition.SUPPORTER_NATIONALITY.value(),
        BailCaseFieldDefinition.SUPPORTER_HAS_PASSPORT.value(),
        BailCaseFieldDefinition.SUPPORTER_PASSPORT.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_YESNO.value(),
        BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY.value(),
        BailCaseFieldDefinition.FCS1_INTERPRETER_SIGN_LANGUAGE.value(),
        BailCaseFieldDefinition.FCS1_INTERPRETER_SPOKEN_LANGUAGE.value(),
        BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES.value(),
        BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2.value(),
        BailCaseFieldDefinition.SUPPORTER_2_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_2_FAMILY_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_2_ADDRESS_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_2_CONTACT_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_2_TELEPHONE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_2_MOBILE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_2_EMAIL_ADDRESS.value(),
        BailCaseFieldDefinition.SUPPORTER_2_DOB.value(),
        BailCaseFieldDefinition.SUPPORTER_2_RELATION.value(),
        BailCaseFieldDefinition.SUPPORTER_2_OCCUPATION.value(),
        BailCaseFieldDefinition.SUPPORTER_2_IMMIGRATION.value(),
        BailCaseFieldDefinition.SUPPORTER_2_NATIONALITY.value(),
        BailCaseFieldDefinition.SUPPORTER_2_HAS_PASSPORT.value(),
        BailCaseFieldDefinition.SUPPORTER_2_PASSPORT.value(),
        BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY.value(),
        BailCaseFieldDefinition.FCS2_INTERPRETER_SIGN_LANGUAGE.value(),
        BailCaseFieldDefinition.FCS2_INTERPRETER_SPOKEN_LANGUAGE.value(),
        BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES.value(),
        BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3.value(),
        BailCaseFieldDefinition.SUPPORTER_3_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_3_FAMILY_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_3_ADDRESS_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_3_CONTACT_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_3_TELEPHONE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_3_MOBILE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_3_EMAIL_ADDRESS.value(),
        BailCaseFieldDefinition.SUPPORTER_3_DOB.value(),
        BailCaseFieldDefinition.SUPPORTER_3_RELATION.value(),
        BailCaseFieldDefinition.SUPPORTER_3_OCCUPATION.value(),
        BailCaseFieldDefinition.SUPPORTER_3_IMMIGRATION.value(),
        BailCaseFieldDefinition.SUPPORTER_3_NATIONALITY.value(),
        BailCaseFieldDefinition.SUPPORTER_3_HAS_PASSPORT.value(),
        BailCaseFieldDefinition.SUPPORTER_3_PASSPORT.value(),
        BailCaseFieldDefinition.FCS3_INTERPRETER_LANGUAGE_CATEGORY.value(),
        BailCaseFieldDefinition.FCS3_INTERPRETER_SIGN_LANGUAGE.value(),
        BailCaseFieldDefinition.FCS3_INTERPRETER_SPOKEN_LANGUAGE.value(),
        BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES.value(),
        BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4.value(),
        BailCaseFieldDefinition.SUPPORTER_4_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_4_FAMILY_NAMES.value(),
        BailCaseFieldDefinition.SUPPORTER_4_ADDRESS_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_4_CONTACT_DETAILS.value(),
        BailCaseFieldDefinition.SUPPORTER_4_TELEPHONE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_4_MOBILE_NUMBER.value(),
        BailCaseFieldDefinition.SUPPORTER_4_EMAIL_ADDRESS.value(),
        BailCaseFieldDefinition.SUPPORTER_4_DOB.value(),
        BailCaseFieldDefinition.SUPPORTER_4_RELATION.value(),
        BailCaseFieldDefinition.SUPPORTER_4_OCCUPATION.value(),
        BailCaseFieldDefinition.SUPPORTER_4_IMMIGRATION.value(),
        BailCaseFieldDefinition.SUPPORTER_4_NATIONALITY.value(),
        BailCaseFieldDefinition.SUPPORTER_4_HAS_PASSPORT.value(),
        BailCaseFieldDefinition.SUPPORTER_4_PASSPORT.value(),
        BailCaseFieldDefinition.FCS4_INTERPRETER_LANGUAGE_CATEGORY.value(),
        BailCaseFieldDefinition.FCS4_INTERPRETER_SIGN_LANGUAGE.value(),
        BailCaseFieldDefinition.FCS4_INTERPRETER_SPOKEN_LANGUAGE.value(),
        BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES.value(),
        BailCaseFieldDefinition.GROUNDS_FOR_BAIL_REASONS.value(),
        BailCaseFieldDefinition.GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION.value(),
        BailCaseFieldDefinition.BAIL_EVIDENCE.value(),
        BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_OPTION.value(),
        BailCaseFieldDefinition.NO_TRANSFER_BAIL_MANAGEMENT_REASONS.value(),
        BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION.value(),
        BailCaseFieldDefinition.OBJECTED_TRANSFER_BAIL_MANAGEMENT_REASONS.value(),
        BailCaseFieldDefinition.INTERPRETER_YESNO.value(),
        BailCaseFieldDefinition.INTERPRETER_LANGUAGES.value(),
        BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE.value(),
        BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE.value(),
        BailCaseFieldDefinition.APPLICANT_INTERPRETER_LANGUAGE_CATEGORY.value(),
        BailCaseFieldDefinition.DISABILITY_YESNO.value(),
        BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS.value(),
        BailCaseFieldDefinition.VIDEO_HEARING_YESNO.value(),
        BailCaseFieldDefinition.VIDEO_HEARING_DETAILS.value(),
        BailCaseFieldDefinition.IS_HOME_OFFICE.value(),
        BailCaseFieldDefinition.HAS_LEGAL_REP.value(),
        BailCaseFieldDefinition.IS_LEGAL_REP.value(),
        BailCaseFieldDefinition.LEGAL_REP_COMPANY.value(),
        BailCaseFieldDefinition.LEGAL_REP_NAME.value(),
        BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME.value(),
        BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS.value(),
        BailCaseFieldDefinition.LEGAL_REP_PHONE.value(),
        BailCaseFieldDefinition.LEGAL_REP_REFERENCE.value(),
        BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS.value(),
        BailCaseFieldDefinition.CASE_NOTES.value(),
        BailCaseFieldDefinition.APPLICANT_PARTY_ID.value(),
        BailCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID.value(),
        BailCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID.value(),
        BailCaseFieldDefinition.SUPPORTER_1_PARTY_ID.value(),
        BailCaseFieldDefinition.SUPPORTER_2_PARTY_ID.value(),
        BailCaseFieldDefinition.SUPPORTER_3_PARTY_ID.value(),
        BailCaseFieldDefinition.SUPPORTER_4_PARTY_ID.value(),
        BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG.value(),
        BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS.value(),
        BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3.value(),
        BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4.value(),
        BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER.value(),
        BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY.value(),
        BailCaseFieldDefinition.LISTING_LOCATION.value(),
        BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION.value(),
        BailCaseFieldDefinition.LIST_CASE_HEARING_DATE.value(),
        BailCaseFieldDefinition.HAS_PROBATION_OFFENDER_MANAGER.value(),
        BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_GIVEN_NAME.value(),
        BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_FAMILY_NAME.value(),
        BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_CONTACT_DETAILS.value(),
        BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_TELEPHONE_NUMBER.value(),
        BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_MOBILE_NUMBER.value(),
        BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_EMAIL_ADDRESS.value()
    );
}
