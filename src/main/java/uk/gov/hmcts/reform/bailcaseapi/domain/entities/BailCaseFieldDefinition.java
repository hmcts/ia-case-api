package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

public enum BailCaseFieldDefinition {
    APPLICATION_SENT_BY(
        "sentByChecklist", new TypeReference<String>() {}),
    IS_ADMIN(
        "isAdmin", new TypeReference<YesOrNo>() {}),
    IS_LEGAL_REP(
        "isLegalRep", new TypeReference<YesOrNo>() {}),
    IS_HOME_OFFICE(
        "isHomeOffice", new TypeReference<YesOrNo>() {}),
    APPLICANT_GIVEN_NAMES(
        "applicantGivenNames", new TypeReference<String>() {}),
    APPLICANT_FAMILY_NAME(
        "applicantFamilyName", new TypeReference<String>() {}),
    APPLICANT_DOB(
        "applicantDateOfBirth", new TypeReference<String>() {}),
    APPLICANT_GENDER(
        "applicantGender", new TypeReference<String>() {}),
    APPLICANT_GENDER_OTHER(
        "applicantGenderEnterDetails", new TypeReference<String>() {}),
    APPLICANT_NATIONALITY(
        "applicantNationality", new TypeReference<String>() {}),
    APPLICANT_NATIONALITIES(
        "applicantNationalities", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),
    APPLICANT_DETENTION_LOCATION(
        "applicantDetainedLoc", new TypeReference<String>(){}),
    APPLICANT_PRISON_DETAILS(
        "applicantPrisonDetails", new TypeReference<String>(){}),
    IRC_NAME(
        "ircName", new TypeReference<String>(){}),
    PRISON_NAME(
        "prisonName", new TypeReference<String>(){}),
    APPLICANT_ARRIVAL_IN_UK(
        "applicantArrivalInUKDate", new TypeReference<String>(){}),
    APPLICANT_HAS_MOBILE(
        "applicantHasMobile", new TypeReference<YesOrNo>(){}),
    APPLICANT_MOBILE_NUMBER(
        "applicantMobileNumber", new TypeReference<String>(){}),
    HAS_APPEAL_HEARING_PENDING(
        "hasAppealHearingPending", new TypeReference<String>(){}),
    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),
    HAS_PREV_BAIL_APPLICATION(
        "hasPreviousBailApplication", new TypeReference<String>(){}),
    PREV_BAIL_APPLICATION_NUMBER(
        "previousBailApplicationNumber", new TypeReference<String>(){}),
    APPLICANT_BEEN_REFUSED_BAIL(
        "applicantBeenRefusedBail", new TypeReference<YesOrNo>(){}),
    BAIL_HEARING_DATE(
        "bailHearingDate", new TypeReference<String>(){}),
    APPLICANT_HAS_ADDRESS(
        "applicantHasAddress", new TypeReference<YesOrNo>(){}),
    APPLICANT_ADDRESS(
        "applicantAddress", new TypeReference<AddressUK>(){}),
    AGREES_TO_BOUND_BY_FINANCIAL_COND(
        "agreesToBoundByFinancialCond", new TypeReference<YesOrNo>(){}),
    FINANCIAL_COND_AMOUNT(
        "financialCondAmount", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER(
        "hasFinancialCondSupporter", new TypeReference<String>(){}),
    SUPPORTER_GIVEN_NAMES(
        "supporterGivenNames", new TypeReference<String>(){}),
    SUPPORTER_FAMILY_NAMES(
        "supporterFamilyNames", new TypeReference<String>(){}),
    SUPPORTER_ADDRESS_DETAILS(
        "supporterAddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_CONTACT_DETAILS(
        "supporterContactDetails", new TypeReference<String>(){}),
    SUPPORTER_TELEPHONE_NUMBER(
        "supporterTelephoneNumber", new TypeReference<String>(){}),
    SUPPORTER_MOBILE_NUMBER(
        "supporterMobileNumber", new TypeReference<String>(){}),
    SUPPORTER_EMAIL_ADDRESS(
        "supporterEmailAddress", new TypeReference<String>(){}),
    SUPPORTER_DOB(
        "supporterDOB", new TypeReference<String>(){}),
    SUPPORTER_RELATION(
        "supporterRelation", new TypeReference<String>(){}),
    SUPPORTER_OCCUPATION(
        "supporterOccupation", new TypeReference<String>(){}),
    SUPPORTER_IMMIGRATION(
        "supporterImmigration", new TypeReference<String>(){}),
    SUPPORTER_NATIONALITY(
        "supporterNationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_HAS_PASSPORT(
        "supporterHasPassport", new TypeReference<String>(){}),
    SUPPORTER_PASSPORT(
        "supporterPassport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES(
        "financialAmountSupporterUndertakes", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_2(
        "hasFinancialCondSupporter2", new TypeReference<String>(){}),
    SUPPORTER_2_GIVEN_NAMES(
        "supporter2GivenNames", new TypeReference<String>(){}),
    SUPPORTER_2_FAMILY_NAMES(
        "supporter2FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_2_ADDRESS_DETAILS(
        "supporter2AddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_2_CONTACT_DETAILS(
        "supporter2ContactDetails", new TypeReference<String>(){}),
    SUPPORTER_2_TELEPHONE_NUMBER(
        "supporter2TelephoneNumber", new TypeReference<String>(){}),
    SUPPORTER_2_MOBILE_NUMBER(
        "supporter2MobileNumber", new TypeReference<String>(){}),
    SUPPORTER_2_EMAIL_ADDRESS(
        "supporter2EmailAddress", new TypeReference<String>(){}),
    SUPPORTER_2_DOB(
        "supporter2DOB", new TypeReference<String>(){}),
    SUPPORTER_2_RELATION(
        "supporter2Relation", new TypeReference<String>(){}),
    SUPPORTER_2_OCCUPATION(
        "supporter2Occupation", new TypeReference<String>(){}),
    SUPPORTER_2_IMMIGRATION(
        "supporter2Immigration", new TypeReference<String>(){}),
    SUPPORTER_2_NATIONALITY(
        "supporter2Nationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_2_HAS_PASSPORT(
        "supporter2HasPassport", new TypeReference<String>(){}),
    SUPPORTER_2_PASSPORT(
        "supporter2Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES(
        "financialAmountSupporter2Undertakes", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_3(
        "hasFinancialCondSupporter3", new TypeReference<String>(){}),
    SUPPORTER_3_GIVEN_NAMES(
        "supporter3GivenNames", new TypeReference<String>(){}),
    SUPPORTER_3_FAMILY_NAMES(
        "supporter3FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_3_ADDRESS_DETAILS(
        "supporter3AddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_3_CONTACT_DETAILS(
        "supporter3ContactDetails", new TypeReference<String>(){}),
    SUPPORTER_3_TELEPHONE_NUMBER(
        "supporter2TelephoneNumber", new TypeReference<String>(){}),
    SUPPORTER_3_MOBILE_NUMBER(
        "supporter3MobileNumber", new TypeReference<String>(){}),
    SUPPORTER_3_EMAIL_ADDRESS(
        "supporter3EmailAddress", new TypeReference<String>(){}),
    SUPPORTER_3_DOB(
        "supporter3DOB", new TypeReference<String>(){}),
    SUPPORTER_3_RELATION(
        "supporter3Relation", new TypeReference<String>(){}),
    SUPPORTER_3_OCCUPATION(
        "supporter3Occupation", new TypeReference<String>(){}),
    SUPPORTER_3_IMMIGRATION(
        "supporter3Immigration", new TypeReference<String>(){}),
    SUPPORTER_3_NATIONALITY(
        "supporter3Nationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_3_HAS_PASSPORT(
        "supporter3HasPassport", new TypeReference<String>(){}),
    SUPPORTER_3_PASSPORT(
        "supporter3Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES(
        "financialAmountSupporter3Undertakes", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_4(
        "hasFinancialCondSupporter4", new TypeReference<String>(){}),
    SUPPORTER_4_GIVEN_NAMES(
        "supporter4GivenNames", new TypeReference<String>(){}),
    SUPPORTER_4_FAMILY_NAMES(
        "supporter4FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_4_ADDRESS_DETAILS(
        "supporter4AddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_4_CONTACT_DETAILS(
        "supporter4ContactDetails", new TypeReference<String>(){}),
    SUPPORTER_4_TELEPHONE_NUMBER(
        "supporter4TelephoneNumber", new TypeReference<String>(){}),
    SUPPORTER_4_MOBILE_NUMBER(
        "supporter4MobileNumber", new TypeReference<String>(){}),
    SUPPORTER_4_EMAIL_ADDRESS(
        "supporter4EmailAddress", new TypeReference<String>(){}),
    SUPPORTER_4_DOB(
        "supporter4DOB", new TypeReference<String>(){}),
    SUPPORTER_4_RELATION(
        "supporter4Relation", new TypeReference<String>(){}),
    SUPPORTER_4_OCCUPATION(
        "supporter4Occupation", new TypeReference<String>(){}),
    SUPPORTER_4_IMMIGRATION(
        "supporter4Immigration", new TypeReference<String>(){}),
    SUPPORTER_4_NATIONALITY(
        "supporter4Nationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_4_HAS_PASSPORT(
        "supporter4HasPassport", new TypeReference<String>(){}),
    SUPPORTER_4_PASSPORT(
        "supporter4Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES(
        "financialAmountSupporter4Undertakes", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),
    LEGAL_REP_EMAIL_ADDRESS(
        "legalRepEmail", new TypeReference<String>(){}),
    GROUNDS_FOR_BAIL_REASONS(
        "groundsForBailReasons", new TypeReference<String>(){}),
    GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION(
        "groundsForBailProvideEvidenceOption", new TypeReference<YesOrNo>(){}),
    UPLOAD_BAIL_EVIDENCE_DOCUMENTS(
        "uploadTheBailEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    TRANSFER_BAIL_MANAGEMENT_OPTION(
        "transferBailManagementYesOrNo", new TypeReference<YesOrNo>(){}),
    NO_TRANSFER_BAIL_MANAGEMENT_REASONS(
        "noTransferBailManagementReasons", new TypeReference<String>(){}),
    APPLICATION_SUBMITTED_BY(
        "applicationSubmittedBy", new TypeReference<String>(){}),
    BAIL_REFERENCE_NUMBER(
        "bailReferenceNumber", new TypeReference<String>(){}),
    APPLICANT_FULL_NAME(
        "applicantFullName", new TypeReference<String>(){}),
    IS_LEGALLY_REPRESENTED_FOR_FLAG(
        "isLegallyRepresentedForFlag", new TypeReference<YesOrNo>() {}),
    HAS_LEGAL_REP(
        "hasLegalRep", new TypeReference<YesOrNo>(){}),
    ;

    private final String value;
    private final TypeReference typeReference;

    BailCaseFieldDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
