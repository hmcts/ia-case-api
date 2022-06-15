package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
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
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class ApplicationDataRemoveHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.MAKE_NEW_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        final Optional<YesOrNo> optionalFinancialSupporter1 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class);
        final Optional<YesOrNo> optionalFinancialSupporter2 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER_2,YesOrNo.class);
        final Optional<YesOrNo> optionalFinancialSupporter3 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class);
        final Optional<YesOrNo> optionalFinancialSupporter4 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class);
        final Optional<YesOrNo> optionalInterpreterLanguage = bailCase.read(INTERPRETER_YESNO, YesOrNo.class);
        final Optional<String> optionalNationality = bailCase.read(APPLICANT_NATIONALITY, String.class);
        final Optional<YesOrNo> optionalBailEvidenceDocs = bailCase.read(
            GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION, YesOrNo.class);
        final Optional<YesOrNo> optionalLegalRepDetails = bailCase.read(HAS_LEGAL_REP, YesOrNo.class);
        final Optional<String> optionalDetentionLocation = bailCase.read(APPLICANT_DETENTION_LOCATION, String.class);
        final Optional<String> optionalGender = bailCase.read(APPLICANT_GENDER, String.class);
        final Optional<YesOrNo> optionalApplicantMobileNumber = bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class);
        final Optional<YesOrNo> optionalDisability = bailCase.read(DISABILITY_YESNO, YesOrNo.class);
        final Optional<YesOrNo> optionalVideoHearing = bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class);
        final Optional<String> optionalAppealHearing = bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class);
        final Optional<YesOrNo> optionalApplicantAddress = bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class);
        final Optional<YesOrNo> optionalFinancialConditionAmount = bailCase.read(
            AGREES_TO_BOUND_BY_FINANCIAL_COND,YesOrNo.class);
        final Optional<YesOrNo> optionalTransferBailManagement = bailCase.read(
            TRANSFER_BAIL_MANAGEMENT_OPTION,YesOrNo.class);


        final YesOrNo hasFinancialConditionSupporter1 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class).orElse(NO);
        final YesOrNo hasFinancialConditionSupporter2 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class).orElse(NO);
        final YesOrNo hasFinancialConditionSupporter3 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class).orElse(NO);
        final YesOrNo hasFinancialConditionSupporter4 = bailCase.read(
            HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class).orElse(NO);

        if (optionalTransferBailManagement.isPresent()) {
            YesOrNo transferBailManagementValue = optionalTransferBailManagement.get();

            if (transferBailManagementValue.equals(YES)) {
                bailCase.remove(NO_TRANSFER_BAIL_MANAGEMENT_REASONS);
            }
        }

        if (optionalFinancialConditionAmount.isPresent()) {
            YesOrNo agreesToFinancialConditionAmount = optionalFinancialConditionAmount.get();

            if (agreesToFinancialConditionAmount.equals(NO)) {
                bailCase.remove(FINANCIAL_COND_AMOUNT);
            }
        }

        if (optionalApplicantAddress.isPresent()) {
            YesOrNo applicantHasAddress = optionalApplicantAddress.get();

            if (applicantHasAddress.equals(NO)) {
                bailCase.remove(APPLICANT_ADDRESS);
            }
        }

        if (optionalAppealHearing.isPresent()) {
            String hasAppealHearing = optionalAppealHearing.get();

            if (hasAppealHearing.equals("YesWithoutAppealNumber")
                || hasAppealHearing.equals("No")
                || hasAppealHearing.equals("DontKnow")
            ) {
                bailCase.remove(APPEAL_REFERENCE_NUMBER);
            }
        }

        if (optionalVideoHearing.isPresent()) {
            YesOrNo videoHearingValue = optionalVideoHearing.get();

            if (videoHearingValue.equals(YES)) {
                bailCase.remove(VIDEO_HEARING_DETAILS);
            }
        }

        if (optionalDisability.isPresent()) {
            YesOrNo hasDisability = optionalDisability.get();

            if (hasDisability.equals(NO)) {
                bailCase.remove(APPLICANT_DISABILITY_DETAILS);
            }
        }

        if (optionalApplicantMobileNumber.isPresent()) {
            YesOrNo applicantMobilePhoneValue = optionalApplicantMobileNumber.get();

            if (applicantMobilePhoneValue.equals(NO)) {
                bailCase.remove(APPLICANT_MOBILE_NUMBER);
            }
        }

        if (optionalGender.isPresent()) {
            String genderValue = optionalGender.get();

            if (genderValue.equals("female") || genderValue.equals("male")) {
                bailCase.remove(APPLICANT_GENDER_OTHER);
            }
        }

        if (optionalDetentionLocation.isPresent()) {
            String detentionFacilityValue = optionalDetentionLocation.get();

            if (detentionFacilityValue.equals("immigrationRemovalCentre")) {
                bailCase.remove(PRISON_NAME);
                bailCase.remove(APPLICANT_PRISON_DETAILS);
            } else if (detentionFacilityValue.equals("prison")) {
                bailCase.remove(IRC_NAME);
            }
        }

        if (optionalLegalRepDetails.isPresent()) {
            YesOrNo legalRepDetails = optionalLegalRepDetails.get();

            if (legalRepDetails.equals(NO)) {
                clearLegalRepDetails(bailCase);
            }
        }

        if (optionalBailEvidenceDocs.isPresent()) {
            YesOrNo bailEvidenceDocs = optionalBailEvidenceDocs.get();

            if (bailEvidenceDocs.equals(NO)) {
                bailCase.remove(BAIL_EVIDENCE);
            }
        }

        if (optionalInterpreterLanguage.isPresent()) {
            YesOrNo interpreterLanguages = optionalInterpreterLanguage.get();

            if (interpreterLanguages.equals(NO)) {
                bailCase.remove(INTERPRETER_LANGUAGES);
            }
        }

        if (optionalNationality.isPresent()) {
            String nationalityValue = optionalNationality.get();

            if (nationalityValue.equals("STATELESS")) {
                bailCase.remove(APPLICANT_NATIONALITIES);
            }
        }

        if (optionalFinancialSupporter1.isPresent()) {

            //Clear all the supporter 1-4 fields
            if (hasFinancialConditionSupporter1.equals(NO)) {
                log.info("Clearing Financial Supporter details from bail application case data");
                clearFinancialSupporter1Details(bailCase);
                clearFinancialSupporter2Details(bailCase);
                clearFinancialSupporter3Details(bailCase);
                clearFinancialSupporter4Details(bailCase);
            }
        }

        if (hasFinancialConditionSupporter1.equals(YES)
            && optionalFinancialSupporter2.isPresent()) {

            //Clear all the supporter 2-4 fields
            if (hasFinancialConditionSupporter2.equals(NO)) {
                log.info("Clearing Financial Supporter details from bail application case data");
                clearFinancialSupporter2Details(bailCase);
                clearFinancialSupporter3Details(bailCase);
                clearFinancialSupporter4Details(bailCase);
            }
        }

        if (hasFinancialConditionSupporter1.equals(YES)
            && hasFinancialConditionSupporter2.equals(YES)
            && optionalFinancialSupporter3.isPresent()) {

            //Clear all the supporter 3-4 fields
            if (hasFinancialConditionSupporter3.equals(NO)) {
                log.info("Clearing Financial Supporter details from bail application case data");
                clearFinancialSupporter3Details(bailCase);
                clearFinancialSupporter4Details(bailCase);
            }
        }

        if (hasFinancialConditionSupporter1.equals(YES)
            && hasFinancialConditionSupporter2.equals(YES)
            && hasFinancialConditionSupporter3.equals(YES)
            && optionalFinancialSupporter4.isPresent()) {

            //Clear all the supporter 4 fields
            if (hasFinancialConditionSupporter4.equals(NO)) {
                log.info("Clearing Financial Supporter details from bail application case data");
                clearFinancialSupporter4Details(bailCase);
            }
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void clearFinancialSupporter1Details(BailCase bailCase) {
        bailCase.remove(SUPPORTER_GIVEN_NAMES);
        bailCase.remove(SUPPORTER_FAMILY_NAMES);
        bailCase.remove(SUPPORTER_ADDRESS_DETAILS);
        bailCase.remove(SUPPORTER_CONTACT_DETAILS);
        bailCase.remove(SUPPORTER_TELEPHONE_NUMBER);
        bailCase.remove(SUPPORTER_MOBILE_NUMBER);
        bailCase.remove(SUPPORTER_EMAIL_ADDRESS);
        bailCase.remove(SUPPORTER_DOB);
        bailCase.remove(SUPPORTER_RELATION);
        bailCase.remove(SUPPORTER_OCCUPATION);
        bailCase.remove(SUPPORTER_IMMIGRATION);
        bailCase.remove(SUPPORTER_NATIONALITY);
        bailCase.remove(SUPPORTER_HAS_PASSPORT);
        bailCase.remove(SUPPORTER_PASSPORT);
        bailCase.remove(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES);
    }

    private void clearFinancialSupporter2Details(BailCase bailCase) {
        bailCase.remove(SUPPORTER_2_GIVEN_NAMES);
        bailCase.remove(SUPPORTER_2_FAMILY_NAMES);
        bailCase.remove(SUPPORTER_2_ADDRESS_DETAILS);
        bailCase.remove(SUPPORTER_2_CONTACT_DETAILS);
        bailCase.remove(SUPPORTER_2_TELEPHONE_NUMBER);
        bailCase.remove(SUPPORTER_2_MOBILE_NUMBER);
        bailCase.remove(SUPPORTER_2_EMAIL_ADDRESS);
        bailCase.remove(SUPPORTER_2_DOB);
        bailCase.remove(SUPPORTER_2_RELATION);
        bailCase.remove(SUPPORTER_2_OCCUPATION);
        bailCase.remove(SUPPORTER_2_IMMIGRATION);
        bailCase.remove(SUPPORTER_2_NATIONALITY);
        bailCase.remove(SUPPORTER_2_HAS_PASSPORT);
        bailCase.remove(SUPPORTER_2_PASSPORT);
        bailCase.remove(FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES);
    }

    private void clearFinancialSupporter3Details(BailCase bailCase) {
        bailCase.remove(SUPPORTER_3_GIVEN_NAMES);
        bailCase.remove(SUPPORTER_3_FAMILY_NAMES);
        bailCase.remove(SUPPORTER_3_ADDRESS_DETAILS);
        bailCase.remove(SUPPORTER_3_CONTACT_DETAILS);
        bailCase.remove(SUPPORTER_3_TELEPHONE_NUMBER);
        bailCase.remove(SUPPORTER_3_MOBILE_NUMBER);
        bailCase.remove(SUPPORTER_3_EMAIL_ADDRESS);
        bailCase.remove(SUPPORTER_3_DOB);
        bailCase.remove(SUPPORTER_3_RELATION);
        bailCase.remove(SUPPORTER_3_OCCUPATION);
        bailCase.remove(SUPPORTER_3_IMMIGRATION);
        bailCase.remove(SUPPORTER_3_NATIONALITY);
        bailCase.remove(SUPPORTER_3_HAS_PASSPORT);
        bailCase.remove(SUPPORTER_3_PASSPORT);
        bailCase.remove(FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES);
    }

    private void clearFinancialSupporter4Details(BailCase bailCase) {
        bailCase.remove(SUPPORTER_4_GIVEN_NAMES);
        bailCase.remove(SUPPORTER_4_FAMILY_NAMES);
        bailCase.remove(SUPPORTER_4_ADDRESS_DETAILS);
        bailCase.remove(SUPPORTER_4_CONTACT_DETAILS);
        bailCase.remove(SUPPORTER_4_TELEPHONE_NUMBER);
        bailCase.remove(SUPPORTER_4_MOBILE_NUMBER);
        bailCase.remove(SUPPORTER_4_EMAIL_ADDRESS);
        bailCase.remove(SUPPORTER_4_DOB);
        bailCase.remove(SUPPORTER_4_RELATION);
        bailCase.remove(SUPPORTER_4_OCCUPATION);
        bailCase.remove(SUPPORTER_4_IMMIGRATION);
        bailCase.remove(SUPPORTER_4_NATIONALITY);
        bailCase.remove(SUPPORTER_4_HAS_PASSPORT);
        bailCase.remove(SUPPORTER_4_PASSPORT);
        bailCase.remove(FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES);
    }

    private void clearLegalRepDetails(BailCase bailCase) {
        bailCase.remove(LEGAL_REP_COMPANY);
        bailCase.remove(LEGAL_REP_EMAIL_ADDRESS);
        bailCase.remove(LEGAL_REP_NAME);
        bailCase.remove(LEGAL_REP_PHONE);
        bailCase.remove(LEGAL_REP_REFERENCE);
    }

}
