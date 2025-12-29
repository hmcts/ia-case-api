package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.PreviousDecisionDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.PreviousListingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtVenue;

import java.util.Arrays;
import java.util.List;

public enum BailCaseFieldDefinition {
    HAS_PREVIOUS_BAIL_APPLICATION(
        "hasPreviousBailApplication", new TypeReference<String>() {}),
    PREVIOUS_BAIL_APPLICATION_NUMBER(
        "previousBailApplicationNumber", new TypeReference<String>() {}),
    PREVIOUS_APPLICATION_DONE_VIA_ARIA(
        "previousApplicationDoneViaAria", new TypeReference<YesOrNo>() {}),
    PREVIOUS_APPLICATION_DONE_VIA_CCD(
        "previousApplicationDoneViaCcd", new TypeReference<YesOrNo>() {}),
    REDIRECT_TO_PREVIOUS_APPLICATION_OR_NOC(
        "redirectToPreviousApplicationOrNoc", new TypeReference<String>() {}),
    APPLICATION_SENT_BY(
        "sentByChecklist", new TypeReference<String>() {}),
    IS_ADMIN(
        "isAdmin", new TypeReference<YesOrNo>() {}),
    IS_LEGAL_REP(
        "isLegalRep", new TypeReference<YesOrNo>() {}),
    IS_HOME_OFFICE(
        "isHomeOffice", new TypeReference<YesOrNo>() {}),
    CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE(
        "currentCaseStateVisibleToLegalRepresentative", new TypeReference<String>(){}),
    CURRENT_CASE_STATE_VISIBLE_TO_JUDGE(
        "currentCaseStateVisibleToJudge", new TypeReference<String>(){}),
    CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER(
        "currentCaseStateVisibleToAdminOfficer", new TypeReference<String>(){}),
    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE(
        "currentCaseStateVisibleToHomeOffice", new TypeReference<String>(){}),
    CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS(
        "currentCaseStateVisibleToAllUsers", new TypeReference<String>(){}),
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
        "applicantMobileNumber1", new TypeReference<String>(){}),
    HAS_APPEAL_HEARING_PENDING(
        "hasAppealHearingPending", new TypeReference<String>(){}),
    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),
    HAS_APPEAL_HEARING_PENDING_UT(
        "hasAppealHearingPendingUt", new TypeReference<String>(){}),
    UT_APPEAL_REFERENCE_NUMBER(
        "utAppealReferenceNumber", new TypeReference<String>(){}),
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
        "financialCondAmount1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER(
        "hasFinancialCondSupporter", new TypeReference<YesOrNo>(){}),
    SUPPORTER_GIVEN_NAMES(
        "supporterGivenNames", new TypeReference<String>(){}),
    SUPPORTER_FAMILY_NAMES(
        "supporterFamilyNames", new TypeReference<String>(){}),
    SUPPORTER_ADDRESS_DETAILS(
        "supporterAddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_CONTACT_DETAILS(
        "supporterContactDetails", new TypeReference<List<ContactPreference>>(){}),
    SUPPORTER_TELEPHONE_NUMBER(
        "supporterTelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_MOBILE_NUMBER(
        "supporterMobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_EMAIL_ADDRESS(
        "supporterEmailAddress1", new TypeReference<String>(){}),
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
        "supporterHasPassport", new TypeReference<YesOrNo>(){}),
    SUPPORTER_PASSPORT(
        "supporterPassport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES(
        "financialAmountSupporterUndertakes1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_2(
        "hasFinancialCondSupporter2", new TypeReference<YesOrNo>(){}),
    SUPPORTER_2_GIVEN_NAMES(
        "supporter2GivenNames", new TypeReference<String>(){}),
    SUPPORTER_2_FAMILY_NAMES(
        "supporter2FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_2_ADDRESS_DETAILS(
        "supporter2AddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_2_CONTACT_DETAILS(
        "supporter2ContactDetails", new TypeReference<List<ContactPreference>>(){}),
    SUPPORTER_2_TELEPHONE_NUMBER(
        "supporter2TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_2_MOBILE_NUMBER(
        "supporter2MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_2_EMAIL_ADDRESS(
        "supporter2EmailAddress1", new TypeReference<String>(){}),
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
        "supporter2HasPassport", new TypeReference<YesOrNo>(){}),
    SUPPORTER_2_PASSPORT(
        "supporter2Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES(
        "financialAmountSupporter2Undertakes1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_3(
        "hasFinancialCondSupporter3", new TypeReference<YesOrNo>(){}),
    SUPPORTER_3_GIVEN_NAMES(
        "supporter3GivenNames", new TypeReference<String>(){}),
    SUPPORTER_3_FAMILY_NAMES(
        "supporter3FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_3_ADDRESS_DETAILS(
        "supporter3AddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_3_CONTACT_DETAILS(
        "supporter3ContactDetails", new TypeReference<List<ContactPreference>>(){}),
    SUPPORTER_3_TELEPHONE_NUMBER(
        "supporter3TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_3_MOBILE_NUMBER(
        "supporter3MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_3_EMAIL_ADDRESS(
        "supporter3EmailAddress1", new TypeReference<String>(){}),
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
        "supporter3HasPassport", new TypeReference<YesOrNo>(){}),
    SUPPORTER_3_PASSPORT(
        "supporter3Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES(
        "financialAmountSupporter3Undertakes1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_4(
        "hasFinancialCondSupporter4", new TypeReference<YesOrNo>(){}),
    SUPPORTER_4_GIVEN_NAMES(
        "supporter4GivenNames", new TypeReference<String>(){}),
    SUPPORTER_4_FAMILY_NAMES(
        "supporter4FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_4_ADDRESS_DETAILS(
        "supporter4AddressDetails", new TypeReference<AddressUK>(){}),
    SUPPORTER_4_CONTACT_DETAILS(
        "supporter4ContactDetails", new TypeReference<List<ContactPreference>>(){}),
    SUPPORTER_4_TELEPHONE_NUMBER(
        "supporter4TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_4_MOBILE_NUMBER(
        "supporter4MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_4_EMAIL_ADDRESS(
        "supporter4EmailAddress1", new TypeReference<String>(){}),
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
        "supporter4HasPassport", new TypeReference<YesOrNo>(){}),
    SUPPORTER_4_PASSPORT(
        "supporter4Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES(
        "financialAmountSupporter4Undertakes1", new TypeReference<String>(){}),
    INTERPRETER_YESNO(
        "interpreterYesNo", new TypeReference<YesOrNo>(){}),
    INTERPRETER_LANGUAGES(
        "interpreterLanguages", new TypeReference<List<IdValue<InterpreterLanguage>>>(){}),
    DISABILITY_YESNO(
        "applicantDisability1", new TypeReference<YesOrNo>(){}),
    APPLICANT_DISABILITY_DETAILS(
        "applicantDisabilityDetails", new TypeReference<String>(){}),
    VIDEO_HEARING_YESNO(
        "videoHearing1", new TypeReference<YesOrNo>(){}),
    VIDEO_HEARING_DETAILS(
        "videoHearingDetails", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),
    LEGAL_REP_EMAIL_ADDRESS(
        "legalRepEmail", new TypeReference<String>(){}),
    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),
    LEGAL_REP_FAMILY_NAME(
        "legalRepFamilyName", new TypeReference<String>(){}),
    LEGAL_REP_PHONE(
        "legalRepPhone", new TypeReference<String>(){}),
    LEGAL_REP_REFERENCE(
        "legalRepReference", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY_ADDRESS(
        "legalRepCompanyAddress", new TypeReference<AddressUK>(){}),
    GROUNDS_FOR_BAIL_REASONS(
        "groundsForBailReasons", new TypeReference<String>(){}),
    GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION(
        "groundsForBailProvideEvidenceOption", new TypeReference<YesOrNo>(){}),
    BAIL_EVIDENCE(
        "uploadTheBailEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),
    APPLICANT_DOCUMENTS_WITH_METADATA(
        "applicantDocumentsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    TRANSFER_BAIL_MANAGEMENT_OPTION(
        "transferBailManagementYesOrNo", new TypeReference<YesOrNo>(){}),
    NO_TRANSFER_BAIL_MANAGEMENT_REASONS(
        "noTransferBailManagementReasons", new TypeReference<String>(){}),
    TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION(
        "transferBailManagementObjectionYesOrNo", new TypeReference<YesOrNo>(){}),
    OBJECTED_TRANSFER_BAIL_MANAGEMENT_REASONS(
        "objectedTransferBailManagementReasons", new TypeReference<String>(){}),
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
    HEARING_CENTRE(
        "hearingCentre", new TypeReference<HearingCentre>(){}),
    DESIGNATED_TRIBUNAL_CENTRE(
        "designatedTribunalCentre", new TypeReference<HearingCentre>(){}),
    HEARING_CENTRE_REF_DATA(
        "hearingCentreRefData", new TypeReference<DynamicList>(){}),
    SELECTED_HEARING_CENTRE_REF_DATA(
        "selectedHearingCentreRefData", new TypeReference<String>(){}),
    DETENTION_FACILITY(
        "detentionFacility", new TypeReference<String>(){}),
    UPLOAD_BAIL_SUMMARY_DOCS(
        "uploadBailSummaryDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),
    UPLOAD_B1_FORM_DOCS(
        "uploadB1FormDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),
    HOME_OFFICE_DOCUMENTS_WITH_METADATA(
        "homeOfficeDocumentsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    CONDITION_FOR_BAIL(
        "conditionsForBail", new TypeReference<List<String>>(){}),
    CONDITION_APPEARANCE(
        "conditionsForBailAppearance", new TypeReference<String>(){}),
    CONDITION_ACTIVITIES(
        "conditionsForBailActivities", new TypeReference<String>(){}),
    CONDITION_RESIDENCE(
        "conditionsForBailResidence", new TypeReference<String>(){}),
    CONDITION_REPORTING(
        "conditionsForBailReporting", new TypeReference<String>(){}),
    CONDITION_ELECTRONIC_MONITORING(
        "conditionsForBailElectronicMonitoring",  new TypeReference<String>(){}),
    BAIL_TRANSFER_DIRECTIONS(
        "bailTransferDirections", new TypeReference<DynamicList>(){}),
    LAST_MODIFIED_DIRECTION(
        "lastModifiedDirection", new TypeReference<Direction>(){}),
    SECRETARY_OF_STATE_REFUSAL_REASONS(
        "secretaryOfStateRefusalReasons", new TypeReference<String>(){}),
    UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT(
        "uploadSignedDecisionNoticeDocument", new TypeReference<Document>(){}),
    DECISION_GRANTED_OR_REFUSED(
        "decisionGrantedOrRefused", new TypeReference<String>(){}),
    //Once we switch the IMA feature on, we will be using this case field instead of the one above
    DECISION_GRANTED_OR_REFUSED_IMA(
        "decisionGrantedOrRefusedIma", new TypeReference<String>(){}),
    RECORD_THE_DECISION_LIST(
        "recordTheDecisionList", new TypeReference<String>(){}),
    //Once we switch the IMA feature on, we will be using this case field instead of the one above
    RECORD_THE_DECISION_LIST_IMA(
        "recordTheDecisionListIma", new TypeReference<String>(){}),
    RELEASE_STATUS_YES_OR_NO(
        "releaseStatusYesOrNo", new TypeReference<YesOrNo>(){}),
    SS_CONSENT_DECISION(
        "ssConsentDecision", new TypeReference<YesOrNo>(){}),
    RECORD_UNSIGNED_DECISION_TYPE(//For UI
        "recordUnsignedDecisionType", new TypeReference<String>(){}),
    RECORD_DECISION_TYPE(
        "recordDecisionType", new TypeReference<String>(){}),
    SECRETARY_OF_STATE_YES_OR_NO(
        "secretaryOfStateConsentYesOrNo", new TypeReference<YesOrNo>(){}),
    DECISION_UNSIGNED_DETAILS_DATE(// For UI
        "decisionUnsignedDetailsDate", new TypeReference<String>(){}),
    DECISION_DETAILS_DATE(
        "decisionDetailsDate", new TypeReference<String>(){}),
    ADD_CASE_NOTE_SUBJECT(
        "addCaseNoteSubject", new TypeReference<String>(){}),
    ADD_CASE_NOTE_DESCRIPTION(
        "addCaseNoteDescription", new TypeReference<String>(){}),
    ADD_CASE_NOTE_DOCUMENT(
        "addCaseNoteDocument", new TypeReference<Document>(){}),
    CASE_NOTES(
        "caseNotes", new TypeReference<List<IdValue<CaseNote>>>(){}),
    SEND_DIRECTION_DESCRIPTION(
        "sendDirectionDescription", new TypeReference<String>(){}),
    SEND_DIRECTION_LIST(
        "sendDirectionList", new TypeReference<String>(){}),
    DATE_OF_COMPLIANCE(
        "dateOfCompliance", new TypeReference<String>(){}),
    DIRECTIONS(
        "directions", new TypeReference<List<IdValue<Direction>>>(){}),
    BAIL_DIRECTION_LIST(
        "bailDirectionList", new TypeReference<DynamicList>(){}),
    BAIL_DIRECTION_EDIT_EXPLANATION(
        "bailDirectionEditExplanation", new TypeReference<String>(){}),
    BAIL_DIRECTION_EDIT_PARTIES(
        "bailDirectionEditParties", new TypeReference<String>(){}),
    BAIL_DIRECTION_EDIT_DATE_SENT(
        "bailDirectionEditDateSent", new TypeReference<String>(){}),
    BAIL_DIRECTION_EDIT_DATE_DUE(
        "bailDirectionEditDateDue", new TypeReference<String>(){}),
    EDITABLE_DIRECTIONS(
        "editableDirections", new TypeReference<List<IdValue<EditableDirection>>>(){}),
    REASON_FOR_REFUSAL_DETAILS(
        "reasonForRefusalDetails", new TypeReference<String>(){}),
    TRIBUNAL_REFUSAL_REASON(
        "tribunalRefusalReason", new TypeReference<String>(){}),
    REASONS_JUDGE_IS_MINDED_DETAILS(
        "reasonsJudgeIsMindedDetails", new TypeReference<String>(){}),
    JUDGE_DETAILS_NAME(
        "judgeDetailsName", new TypeReference<String>(){}),
    CONDITION_OTHER(
        "conditionsForBailOther", new TypeReference<String>(){}),
    BAIL_TRANSFER_YES_OR_NO(
        "bailTransferYesOrNo", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER1(
        "judgeHasAgreedToSupporter1", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER2(
        "judgeHasAgreedToSupporter2", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER3(
        "judgeHasAgreedToSupporter3", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER4(
        "judgeHasAgreedToSupporter4", new TypeReference<YesOrNo>(){}),
    RECORD_FINANCIAL_CONDITION_YES_OR_NO(
        "recordFinancialConditionYesOrNo", new TypeReference<YesOrNo>(){}),
    DECISION_UNSIGNED_DOCUMENT(
        "decisionUnsignedDocument", new TypeReference<Document>(){}),
    TRIBUNAL_DOCUMENTS_WITH_METADATA(
        "tribunalDocumentsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    HEARING_DOCUMENTS(
        "hearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    END_APPLICATION_DATE(
        "endApplicationDate", new TypeReference<String>(){}),
    END_APPLICATION_OUTCOME(
        "endApplicationOutcome", new TypeReference<String>(){}),
    END_APPLICATION_REASONS(
        "endApplicationReasons", new TypeReference<String>(){}),
    UPLOAD_DOCUMENTS(
        "uploadDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),
    UPLOAD_DOCUMENTS_SUPPLIED_BY(
        "uploadDocumentsSuppliedBy", new TypeReference<String>(){}),
    CURRENT_USER(
        "currentUser", new TypeReference<String>(){}),
    EDIT_DOCUMENTS_REASON(
        "editDocumentsReason", new TypeReference<String>(){}),
    OUTCOME_STATE(
        "outcomeState", new TypeReference<String>(){}),
    OUTCOME_DATE(
        "outcomeDate", new TypeReference<String>(){}),
    PRIOR_APPLICATIONS(
        "priorApplications1", new TypeReference<List<IdValue<PriorApplication>>>(){}),
    NOTIFICATIONS_SENT(
        "notificationsSent", new TypeReference<List<IdValue<String>>>() {}),
    DECLARATION_ON_SUBMIT(
        "declarationOnSubmit", new TypeReference<List<String>>(){}),
    SUBMIT_NOTIFICATION_STATUS(
        "submitNotificationStatus", new TypeReference<String>() {}),
    PREVIOUS_APPLICATION_LIST(
        "previousApplicationList", new TypeReference<DynamicList>() {}),
    PREV_APP_ID(
        "prevAppId", new TypeReference<String>() {}),
    PREV_APP_APPLICANT_DOCS_DETAILS(
        "prevAppApplicantDocsDetails", new TypeReference<String>() {}),
    PREV_APP_DOCS_WITH_METADATA(
        "prevAppDocsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    PREV_APP_DECISION_DETAILS_LABEL(
        "prevAppDecisionDetailsLabel", new TypeReference<String>() {}),
    PREV_APP_DIRECTION_DETAILS(
        "prevAppDirectionDetails", new TypeReference<String>() {}),
    PREV_APP_HEARING_DETAILS(
        "prevAppHearingDetails", new TypeReference<String>() {}),
    PREV_APP_HEARING_REQ_DETAILS(
        "prevAppHearingReqDetails", new TypeReference<String>() {}),
    PREV_APP_PERSONAL_INFO_DETAILS(
        "prevAppPersonalInfoDetails", new TypeReference<String>() {}),
    PREV_APP_SUBMISSION_DETAILS(
        "prevAppSubmissionDetails", new TypeReference<String>() {}),
    PREV_APP_DECISION_CONDITIONS(
        "prevAppDecisionConditions", new TypeReference<String>() {}),
    LOCAL_AUTHORITY_POLICY(
        "localAuthorityPolicy", new TypeReference<OrganisationPolicy>() {}),
    CHANGE_ORGANISATION_REQUEST_FIELD(
        "changeOrganisationRequestField", new TypeReference<ChangeOrganisationRequest>() {}),
    PREV_APP_APPLICANT_INFO(
        "prevAppApplicantInfo", new TypeReference<String>() {}),
    PREV_APP_FINANCIAL_COND_COMMITMENT(
        "prevAppFinancialCondCommitment", new TypeReference<String>() {}),
    PREV_APP_FINANCIAL_COND_SUPPORTER1(
        "prevAppFinancialCondSupporter1", new TypeReference<String>() {}),
    PREV_APP_FINANCIAL_COND_SUPPORTER2(
        "prevAppFinancialCondSupporter2", new TypeReference<String>() {}),
    PREV_APP_FINANCIAL_COND_SUPPORTER3(
        "prevAppFinancialCondSupporter3", new TypeReference<String>() {}),
    PREV_APP_FINANCIAL_COND_SUPPORTER4(
        "prevAppFinancialCondSupporter4", new TypeReference<String>() {}),
    PREV_APP_GROUNDS_FOR_BAIL(
        "prevAppGroundsForBail", new TypeReference<String>() {}),
    PREV_APP_LEGAL_REP_DETAILS(
        "prevAppLegalRepDetails", new TypeReference<String>() {}),
    PREV_APP_PROBATION_OFFENDER_MANAGER(
        "prevAppProbationOffenderManager", new TypeReference<String>() {}),
    HAS_PROBATION_OFFENDER_MANAGER(
        "hasProbationOffenderManager", new TypeReference<YesOrNo>() {}),
    PROBATION_OFFENDER_MANAGER_GIVEN_NAME(
        "probationOffenderManagerGivenName", new TypeReference<String>() {}),
    PROBATION_OFFENDER_MANAGER_FAMILY_NAME(
        "probationOffenderManagerFamilyName", new TypeReference<String>() {}),
    PROBATION_OFFENDER_MANAGER_CONTACT_DETAILS(
        "probationOffenderManagerContactDetails", new TypeReference<String>() {}),
    PROBATION_OFFENDER_MANAGER_TELEPHONE_NUMBER(
        "probationOffenderManagerTelephoneNumber", new TypeReference<String>() {}),
    PROBATION_OFFENDER_MANAGER_MOBILE_NUMBER(
        "probationOffenderManagerMobileNumber", new TypeReference<String>() {}),
    PROBATION_OFFENDER_MANAGER_EMAIL_ADDRESS(
        "probationOffenderManagerEmailAddress", new TypeReference<String>() {}),
    UPDATE_LEGAL_REP_COMPANY(
        "updateLegalRepCompany", new TypeReference<String>(){}),
    UPDATE_LEGAL_REP_EMAIL_ADDRESS(
        "updateLegalRepEmail1", new TypeReference<String>(){}),
    UPDATE_LEGAL_REP_NAME(
        "updateLegalRepName", new TypeReference<String>(){}),
    UPDATE_LEGAL_REP_FAMILY_NAME(
        "updateLegalRepFamilyName", new TypeReference<String>(){}),
    UPDATE_LEGAL_REP_REFERENCE(
        "updateLegalRepReference", new TypeReference<String>(){}),
    UPDATE_LEGAL_REP_PHONE(
        "updateLegalRepPhone", new TypeReference<String>(){}),
    UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA(
        "unsgnDecisionDocumentWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    SIGNED_DECISION_DOCUMENTS_WITH_METADATA(
        "signDecisionDocumentWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    CASE_NAME_HMCTS_INTERNAL(
        "caseNameHmctsInternal", new TypeReference<String>() {}),
    APPELLANT_LEVEL_FLAGS(
        "appellantLevelFlags", new TypeReference<StrategicCaseFlag>() {}),
    CASE_FLAGS(
        "caseFlags", new TypeReference<StrategicCaseFlag>(){}),

    APPLICANT_INTERPRETER_SPOKEN_LANGUAGE(
        "applicantInterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    APPLICANT_INTERPRETER_SIGN_LANGUAGE(
        "applicantInterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    APPLICANT_INTERPRETER_LANGUAGE_CATEGORY(
        "applicantInterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    FCS_INTERPRETER_YESNO(
            "fcsInterpreterYesNo", new TypeReference<YesOrNo>(){}),

    FCS1_INTERPRETER_LANGUAGE_CATEGORY(
            "fcs1InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    FCS1_INTERPRETER_SPOKEN_LANGUAGE(
            "fcs1InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS1_INTERPRETER_SIGN_LANGUAGE(
            "fcs1InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS2_INTERPRETER_LANGUAGE_CATEGORY(
            "fcs2InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    FCS2_INTERPRETER_SPOKEN_LANGUAGE(
            "fcs2InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS2_INTERPRETER_SIGN_LANGUAGE(
            "fcs2InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS3_INTERPRETER_LANGUAGE_CATEGORY(
            "fcs3InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    FCS3_INTERPRETER_SPOKEN_LANGUAGE(
            "fcs3InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS3_INTERPRETER_SIGN_LANGUAGE(
            "fcs3InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS4_INTERPRETER_LANGUAGE_CATEGORY(
            "fcs4InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    FCS4_INTERPRETER_SPOKEN_LANGUAGE(
            "fcs4InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS4_INTERPRETER_SIGN_LANGUAGE(
            "fcs4InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    FCS_LEVEL_FLAGS(
        "fcsLevelFlags", new TypeReference<List<PartyFlagIdValue>>() {}),

    APPLICANT_PARTY_ID(
        "applicantPartyId", new TypeReference<String>(){}),

    LEGAL_REP_INDIVIDUAL_PARTY_ID(
        "legalRepIndividualPartyId", new TypeReference<String>(){}),

    LEGAL_REP_ORGANISATION_PARTY_ID(
        "legalRepOrganisationPartyId", new TypeReference<String>(){}),

    SUPPORTER_1_PARTY_ID(
        "supporter1PartyId", new TypeReference<String>(){}),

    SUPPORTER_2_PARTY_ID(
        "supporter2PartyId", new TypeReference<String>(){}),

    SUPPORTER_3_PARTY_ID(
        "supporter3PartyId", new TypeReference<String>(){}),

    SUPPORTER_4_PARTY_ID(
        "supporter4PartyId", new TypeReference<String>(){}),
    INTERPRETER_DETAILS(
        "interpreterDetails", new TypeReference<List<IdValue<InterpreterDetails>>>() {}),

    APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING(
        "applicantInterpreterSpokenLanguageBooking", new TypeReference<String>(){}),

    APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS(
        "applicantInterpreterSpokenLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>(){}),

    APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING(
        "applicantInterpreterSignLanguageBooking", new TypeReference<String>(){}),

    APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS(
        "applicantInterpreterSignLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1(
        "fcsInterpreterSpokenLanguageBooking1", new TypeReference<String>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1(
        "fcsInterpreterSpokenLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2(
        "fcsInterpreterSpokenLanguageBooking2", new TypeReference<String>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2(
        "fcsInterpreterSpokenLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3(
        "fcsInterpreterSpokenLanguageBooking3", new TypeReference<String>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3(
        "fcsInterpreterSpokenLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4(
        "fcsInterpreterSpokenLanguageBooking4", new TypeReference<String>(){}),

    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4(
        "fcsInterpreterSpokenLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1(
        "fcsInterpreterSignLanguageBooking1", new TypeReference<String>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1(
        "fcsInterpreterSignLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2(
        "fcsInterpreterSignLanguageBooking2", new TypeReference<String>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2(
        "fcsInterpreterSignLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3(
        "fcsInterpreterSignLanguageBooking3", new TypeReference<String>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3(
        "fcsInterpreterSignLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4(
        "fcsInterpreterSignLanguageBooking4", new TypeReference<String>(){}),

    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4(
        "fcsInterpreterSignLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>(){}),

    REASON_TO_FORCE_CASE_TO_HEARING(
        "reasonToForceCaseToHearing", new TypeReference<String>(){}),

    IS_DETENTION_LOCATION_CORRECT(
        "isDetentionLocationCorrect", new TypeReference<YesOrNo>(){}),

    LISTING_EVENT(
        "listingEvent", new TypeReference<ListingEvent>(){}),

    UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE(
        "uploadBailSummaryActionAvailable", new TypeReference<YesOrNo>(){}),

    LIST_CASE_HEARING_DATE(
        "listingHearingDate", new TypeReference<String>(){}),

    LISTING_HEARING_DURATION(
        "listingHearingDuration", new TypeReference<String>(){}),

    LISTING_LOCATION(
        "listingLocation", new TypeReference<ListingHearingCentre>(){}),

    PREVIOUS_LISTING_DETAILS(
        "previousListingDetails", new TypeReference<List<IdValue<PreviousListingDetails>>>() {}),
    HAS_BEEN_RELISTED(
        "hasBeenRelisted", new TypeReference<YesOrNo>() {}),
    PREVIOUS_DECISION_DETAILS(
        "previousDecisionDetails", new TypeReference<List<IdValue<PreviousDecisionDetails>>>() {}),
    HO_HAS_IMA_STATUS(
        "hoHasImaStatus", new TypeReference<YesOrNo>(){}),
    ADMIN_HAS_IMA_STATUS(
        "adminHasImaStatus", new TypeReference<YesOrNo>(){}),

    /*
        ADMIN_SELECT_IMA_STATUS and HO_SELECT_IMA_STATUS used in the journey pages.
        HO_HAS_IMA_STATUS and ADMIN_HAS_IMA_STATUS used for representation in summary pages with different titles.
    */
    ADMIN_SELECT_IMA_STATUS(
        "adminSelectImaStatus", new TypeReference<YesOrNo>() {}),
    HO_SELECT_IMA_STATUS(
        "hoSelectImaStatus", new TypeReference<YesOrNo>() {}),
    IS_IMA_ENABLED(
        "isImaEnabled", new TypeReference<YesOrNo>() {}),
    IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED(
        "isBailsLocationReferenceDataEnabled", new TypeReference<YesOrNo>() {}),
    REF_DATA_LISTING_LOCATION(
        "refDataListingLocation", new TypeReference<DynamicList>() {}),
    REF_DATA_LISTING_LOCATION_DETAIL(
        "refDataListingLocationDetail", new TypeReference<CourtVenue>() {}),
    IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT(
        "isBailsLocationReferenceDataEnabledFt", new TypeReference<YesOrNo>() {}),
    HAS_CASE_BEEN_FORCED_TO_HEARING(
        "hasCaseBeenForcedToHearing", new TypeReference<YesOrNo>() {}),

    CURRENT_HEARING_ID(
        "currentHearingId", new TypeReference<String>() {}),

    HEARING_ID_LIST(
        "hearingIdList", new TypeReference<List<IdValue<String>>>(){}),

    HEARING_DECISION_LIST(
            "hearingDecisionList", new TypeReference<List<IdValue<HearingDecision>>>(){}),

    HEARING_RECORDING_DOCUMENTS(
        "hearingRecordingDocuments", new TypeReference<List<IdValue<HearingRecordingDocument>>>(){});

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

    public static BailCaseFieldDefinition getEnumFromString(String stringToGet) {

        return Arrays.stream(BailCaseFieldDefinition.values())
            .filter(ccd -> ccd.value.equals(stringToGet))
            .findFirst().orElseThrow(
                () -> new IllegalArgumentException("No BailCaseFieldDefinition found with the value: " + stringToGet)
            );
    }
}
