package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HearingType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;

public enum AsylumCaseFieldDefinition {


    BUNDLE_CONFIGURATION(
        "bundleConfiguration", new TypeReference<String>(){}),

    CASE_BUNDLES(
        "caseBundles", new TypeReference<List<IdValue<Bundle>>>(){}),

    STITCHING_STATUS(
        "stitchingStatus", new TypeReference<String>(){}),

    BUNDLE_FILE_NAME_PREFIX(
        "bundleFileNamePrefix", new TypeReference<String>(){}),

    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_DECISION_DATE(
        "homeOfficeDecisionDate", new TypeReference<String>(){}),

    APPELLANT_GIVEN_NAMES(
        "appellantGivenNames", new TypeReference<String>(){}),

    APPELLANT_FAMILY_NAME(
        "appellantFamilyName", new TypeReference<String>(){}),

    APPELLANT_NATIONALITIES(
            "appellantNationalities", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),

    APPELLANT_NATIONALITIES_DESCRIPTION(
        "appellantNationalitiesDescription", new TypeReference<String>(){}),

    APPELLANT_STATELESS(
            "appellantStateless", new TypeReference<String>(){}),

    APPELLANT_HAS_FIXED_ADDRESS(
        "appellantHasFixedAddress", new TypeReference<YesOrNo>(){}),

    APPELLANT_ADDRESS(
        "appellantAddress", new TypeReference<AddressUk>(){}),

    SEARCH_POSTCODE(
        "searchPostcode", new TypeReference<String>(){}),

    CONTACT_PREFERENCE(
        "contactPreference", new TypeReference<ContactPreference>(){}),

    CONTACT_PREFERENCE_DESCRIPTION(
        "contactPreferenceDescription", new TypeReference<String>(){}),

    EMAIL(
        "email", new TypeReference<String>(){}),

    MOBILE_NUMBER(
        "mobileNumber", new TypeReference<String>(){}),

    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){}),

    APPEAL_TYPE_DESCRIPTION(
        "appealTypeDescription", new TypeReference<String>(){}),

    APPEAL_GROUNDS_PROTECTION(
        "appealGroundsProtection", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_HUMAN_RIGHTS(
        "appealGroundsHumanRights", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_REVOCATION(
        "appealGroundsRevocation", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_HUMAN_RIGHTS_REFUSAL(
        "appealGroundsHumanRightsRefusal", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_DEPRIVATION_HUMAN_RIGHTS(
        "appealGroundsDeprivationHumanRights", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_DEPRIVATION(
        "appealGroundsDeprivation", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_EU_REFUSAL(
        "appealGroundsEuRefusal", new TypeReference<CheckValues<String>>(){}),


    HAS_OTHER_APPEALS(
        "hasOtherAppeals", new TypeReference<String>(){}),

    NEW_MATTERS(
        "newMatters", new TypeReference<String>(){}),

    HAS_NEW_MATTERS(
        "hasNewMatters", new TypeReference<YesOrNo>(){}),

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    APPELLANT_NAME_FOR_DISPLAY(
        "appellantNameForDisplay", new TypeReference<String>(){}),

    APPEAL_GROUNDS_FOR_DISPLAY(
        "appealGroundsForDisplay", new TypeReference<List<String>>(){}),

    HEARING_CENTRE(
        "hearingCentre", new TypeReference<HearingCentre>(){}),

    SEND_DIRECTION_EXPLANATION(
        "sendDirectionExplanation", new TypeReference<String>(){}),

    SEND_DIRECTION_PARTIES(
        "sendDirectionParties", new TypeReference<Parties>(){}),

    SEND_DIRECTION_DATE_DUE(
        "sendDirectionDateDue", new TypeReference<String>(){}),

    SEND_DIRECTION_QUESTIONS(
            "sendDirectionQuestions", new TypeReference<List<IdValue<ClarifyingQuestion>>>(){}),

    EDITABLE_DIRECTIONS(
        "editableDirections", new TypeReference<List<IdValue<EditableDirection>>>(){}),

    ADDITIONAL_EVIDENCE_DOCUMENTS(
        "additionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    HEARING_RECORDING_DOCUMENTS(
        "hearingRecordingDocuments", new TypeReference<List<IdValue<HearingRecordingDocument>>>(){}),

    TRIBUNAL_DOCUMENTS(
        "tribunalDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>() {}),

    HEARING_DOCUMENTS(
        "hearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    LEGAL_REPRESENTATIVE_DOCUMENTS(
        "legalRepresentativeDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RESPONDENT_DOCUMENTS(
        "respondentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RESPONDENT_EVIDENCE(
        "respondentEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPELLANT_GROUNDS_DOCUMENTS(
        "ftpaAppellantGroundsDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPELLANT_EVIDENCE_DOCUMENTS(
        "ftpaAppellantEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS(
        "ftpaAppellantOutOfTimeDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION(
        "ftpaAppellantOutOfTimeExplanation", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaAppellantDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaAppellantGroundsDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaAppellantEvidenceDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED(
        "isFtpaAppellantOotExplanationVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaAppellantOotDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaAppellantDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaAppellantGroundsDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaAppellantEvidenceDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_DECIDED(
        "isFtpaAppellantOotExplanationVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaAppellantOotDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaRespondentDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaRespondentGroundsDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaRespondentEvidenceDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED(
        "isFtpaRespondentOotExplanationVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_SUBMITTED(
        "isFtpaRespondentOotDocsVisibleInSubmitted", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaRespondentDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaRespondentGroundsDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaRespondentEvidenceDocsVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_DECIDED(
        "isFtpaRespondentOotExplanationVisibleInDecided", new TypeReference<String>(){}),

    IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_DECIDED(
        "isFtpaRespondentOotDocsVisibleInDecided", new TypeReference<String>(){}),

    FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION(
        "ftpaRespondentOutOfTimeExplanation", new TypeReference<String>(){}),

    FTPA_APPELLANT_DOCUMENTS(
        "ftpaAppellantDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    FTPA_APPLICANT_TYPE(
        "ftpaApplicantType", new TypeReference<String>(){}),

    FTPA_APPELLANT_SUBMITTED(
        "ftpaAppellantSubmitted", new TypeReference<String>(){}),

    FTPA_RESPONDENT_SUBMITTED(
        "ftpaRespondentSubmitted", new TypeReference<String>(){}),

    FTPA_APPELLANT_APPLICATION_DATE(
        "ftpaAppellantApplicationDate", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_DOCUMENT(
        "ftpaAppellantDecisionDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPELLANT_NOTICE_DOCUMENT(
        "ftpaAppellantNoticeDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_DECISION_DOCUMENT(
        "ftpaRespondentDecisionDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_NOTICE_DOCUMENT(
        "ftpaRespondentNoticeDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    ALL_FTPA_APPELLANT_DECISION_DOCS(
        "allFtpaAppellantDecisionDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ALL_FTPA_RESPONDENT_DECISION_DOCS(
        "allFtpaRespondentDecisionDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    IS_FTPA_APPELLANT_DECIDED(
        "isFtpaAppellantDecided", new TypeReference<YesOrNo>(){}),

    IS_FTPA_RESPONDENT_DECIDED(
        "isFtpaRespondentDecided", new TypeReference<YesOrNo>(){}),

    IS_FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE(
        "isFtpaAppellantNoticeOfDecisionSetAside", new TypeReference<YesOrNo>(){}),

    IS_FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE(
        "isFtpaRespondentNoticeOfDecisionSetAside", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE(
        "ftpaAppellantNoticeOfDecisionSetAsideVisible", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE(
        "ftpaRespondentNoticeOfDecisionSetAsideVisible", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_DECISION_OBJECTIONS_VISIBLE(
        "ftpaAppellantDecisionObjectionsVisible", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_DECISION_OBJECTIONS_VISIBLE(
        "ftpaRespondentDecisionObjectionsVisible", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE(
        "ftpaAppellantDecisionReasonsNotesVisible", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE(
        "ftpaRespondentDecisionReasonsNotesVisible", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_DECISION_LISTING_VISIBLE(
        "ftpaAppellantDecisionListingVisible", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_DECISION_LISTING_VISIBLE(
        "ftpaRespondentDecisionListingVisible", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_DECISION_REASONS_VISIBLE(
        "ftpaAppellantDecisionReasonsVisible", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_DECISION_REASONS_VISIBLE(
        "ftpaRespondentDecisionReasonsVisible", new TypeReference<YesOrNo>(){}),

    IS_APPELLANT_FTPA_DECISION_VISIBLE_TO_ALL(
        "isAppellantFtpaDecisionVisibleToAll", new TypeReference<String>(){}),

    IS_RESPONDENT_FTPA_DECISION_VISIBLE_TO_ALL(
        "isRespondentFtpaDecisionVisibleToAll", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_DATE(
        "ftpaAppellantDecisionDate", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_DATE(
        "ftpaRespondentDecisionDate", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_OUTCOME_TYPE(
        "ftpaAppellantDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE(
        "ftpaAppellantRjDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE(
        "ftpaRespondentDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE(
        "ftpaRespondentRjDecisionOutcomeType", new TypeReference<String>(){}),

    HOME_OFFICE_BUNDLE(
        "homeOfficeBundle", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_GROUNDS_DOCUMENTS(
        "ftpaRespondentGroundsDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_EVIDENCE_DOCUMENTS(
        "ftpaRespondentEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS(
        "ftpaRespondentOutOfTimeDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_DOCUMENTS(
        "ftpaRespondentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    FTPA_RESPONDENT_APPLICATION_DATE(
        "ftpaRespondentApplicationDate", new TypeReference<String>(){}),

    FTPA_APPELLANT_RJ_NEW_DECISION_OF_APPEAL(
        "ftpaAppellantRjNewDecisionOfAppeal", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_REMADE_RULE_32(
        "ftpaAppellantDecisionRemadeRule32", new TypeReference<String>(){}),

    FTPA_RESPONDENT_RJ_NEW_DECISION_OF_APPEAL(
        "ftpaRespondentRjNewDecisionOfAppeal", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_REMADE_RULE_32(
        "ftpaRespondentDecisionRemadeRule32", new TypeReference<String>(){}),

    FTPA_FIRST_DECISION(
        "ftpaFirstDecision", new TypeReference<String>() {}),

    FTPA_SECOND_DECISION(
        "ftpaSecondDecision", new TypeReference<String>() {}),

    FTPA_FINAL_DECISION_REMADE_RULE_32(
        "ftpaFinalDecisionRemadeRule32", new TypeReference<String>() {}),

    FTPA_FINAL_DECISION_FOR_DISPLAY(
        "ftpaFinalDecisionForDisplay", new TypeReference<String>(){}),

    UPLOADED_HOME_OFFICE_BUNDLE_DOCS(
        "uploadedHomeOfficeBundleDocs", new TypeReference<String>(){}),

    CASE_ARGUMENT_DOCUMENT(
        "caseArgumentDocument", new TypeReference<Document>(){}),

    CASE_ARGUMENT_DESCRIPTION(
        "caseArgumentDescription", new TypeReference<String>(){}),

    UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT(
        "uploadTheNoticeOfDecisionDocument", new TypeReference<Document>(){}),

    UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION(
        "uploadTheNoticeOfDecisionExplanation", new TypeReference<String>(){}),

    CASE_ARGUMENT_EVIDENCE(
        "caseArgumentEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    APPEAL_RESPONSE_DOCUMENT(
        "appealResponseDocument", new TypeReference<Document>(){}),

    APPEAL_RESPONSE_DESCRIPTION(
        "appealResponseDescription", new TypeReference<String>(){}),

    APPEAL_RESPONSE_EVIDENCE(
        "appealResponseEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    HOME_OFFICE_APPEAL_RESPONSE_DOCUMENT(
        "homeOfficeAppealResponseDocument", new TypeReference<Document>(){}),

    HOME_OFFICE_APPEAL_RESPONSE_DESCRIPTION(
        "homeOfficeAppealResponseDescription", new TypeReference<String>(){}),

    HOME_OFFICE_APPEAL_RESPONSE_EVIDENCE(
        "homeOfficeAppealResponseEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    UPLOADED_HOME_OFFICE_APPEAL_RESPONSE_DOCS(
        "uploadedHomeOfficeAppealResponseDocs", new TypeReference<String>(){}),

    ADDITIONAL_EVIDENCE(
        "additionalEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    LIST_CASE_HEARING_CENTRE(
        "listCaseHearingCentre", new TypeReference<HearingCentre>(){}),

    LIST_CASE_HEARING_DATE(
        "listCaseHearingDate", new TypeReference<String>(){}),

    LIST_CASE_HEARING_DATE_ADJOURNED(
        "listCaseHearingDateAdjourned", new TypeReference<String>(){}),

    CASE_SUMMARY_DOCUMENT(
        "caseSummaryDocument", new TypeReference<Document>(){}),

    CASE_SUMMARY_DESCRIPTION(
        "caseSummaryDescription", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_NAME(
        "legalRepresentativeName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),

    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
        "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE(
        "changeDirectionDueDateActionAvailable", new TypeReference<YesOrNo>(){}),

    SEND_DIRECTION_ACTION_AVAILABLE(
        "sendDirectionActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_ADDITIONAL_EVIDENCE_ACTION_AVAILABLE(
        "uploadAdditionalEvidenceActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE(
        "uploadHomeOfficeBundleActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE(
        "uploadHomeOfficeBundleAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE(
        "uploadHomeOfficeAppealResponseActionAvailable", new TypeReference<YesOrNo>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER(
        "currentCaseStateVisibleToCaseOfficer", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_JUDGE(
        "currentCaseStateVisibleToJudge", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE(
        "currentCaseStateVisibleToLegalRepresentative", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER(
        "currentCaseStateVisibleToAdminOfficer", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_APC(
        "currentCaseStateVisibleToHomeOfficeApc", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_LART(
        "currentCaseStateVisibleToHomeOfficeLart", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_POU(
        "currentCaseStateVisibleToHomeOfficePou", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_GENERIC(
        "currentCaseStateVisibleToHomeOfficeGeneric", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL(
        "currentCaseStateVisibleToHomeOfficeAll", new TypeReference<State>(){}),

    CASE_ARGUMENT_AVAILABLE(
        "caseArgumentAvailable", new TypeReference<YesOrNo>(){}),

    APPEAL_RESPONSE_AVAILABLE(
        "appealResponseAvailable", new TypeReference<YesOrNo>(){}),

    CASE_BUILDING_READY_FOR_SUBMISSION(
        "caseBuildingReadyForSubmission", new TypeReference<YesOrNo>(){}),

    RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED(
        "respondentReviewAppealResponseAdded", new TypeReference<YesOrNo>(){}),

    SUBMISSION_OUT_OF_TIME(
        "submissionOutOfTime", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME(
        "ftpaAppellantSubmissionOutOfTime", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME(
        "ftpaRespondentSubmissionOutOfTime", new TypeReference<YesOrNo>(){}),

    DIRECTIONS(
        "directions", new TypeReference<List<IdValue<Direction>>>(){}),

    DECISION_AND_REASONS_AVAILABLE(
        "decisionAndReasonsAvailable", new TypeReference<YesOrNo>(){}),

    ADD_CASE_NOTE_SUBJECT(
        "addCaseNoteSubject", new TypeReference<String>(){}),

    ADD_CASE_NOTE_DESCRIPTION(
        "addCaseNoteDescription", new TypeReference<String>(){}),

    CASE_NOTES(
        "caseNotes", new TypeReference<List<IdValue<CaseNote>>>(){}),

    ADD_CASE_NOTE_DOCUMENT(
        "addCaseNoteDocument", new TypeReference<Document>(){}),

    FINAL_DECISION_AND_REASONS_PDF(
        "finalDecisionAndReasonsPdf", new TypeReference<Document>(){}),


    // VALUES IN TRANSITION, ONLY USED IN DATA FIXING SERVICE
    APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION(
        "appellantsAgreedScheduleOfIssuesDescription", new TypeReference<String>(){}),
    APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION(
        "appellantsDisputedScheduleOfIssuesDescription", new TypeReference<String>(){}),
    RESPONDENTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION(
        "respondentsAgreedScheduleOfIssuesDescription", new TypeReference<String>(){}),
    RESPONDENTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION(
        "respondentsDisputedScheduleOfIssuesDescription", new TypeReference<String>(){}),
    DRAFT_DECISION_AND_REASONS_DOCUMENTS(
        "draftDecisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    DECISION_AND_REASONS_DOCUMENTS(
        "decisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    FINAL_DECISION_AND_REASONS_DOCUMENTS(
        "finalDecisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    UPLOAD_ADDENDUM_EVIDENCE_ACTION_AVAILABLE(
        "uploadAddendumEvidenceActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP_ACTION_AVAILABLE(
        "uploadAddendumEvidenceLegalRepActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE_ACTION_AVAILABLE(
        "uploadAddendumEvidenceHomeOfficeActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER_ACTION_AVAILABLE(
        "uploadAddendumEvidenceAdminOfficerActionAvailable", new TypeReference<YesOrNo>(){}),

    ADDENDUM_EVIDENCE(
        "addendumEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    ADDENDUM_EVIDENCE_DOCUMENTS(
        "addendumEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    IS_APPELLANT_RESPONDENT(
        "isAppellantRespondent", new TypeReference<String>(){}),

    APPLICATION_DOCUMENTS(
        "applicationDocuments", new TypeReference<List<IdValue<Document>>>(){}),

    APPLICATION_REASON(
        "applicationReason", new TypeReference<String>(){}),

    APPLICATION_DATE(
        "applicationDate", new TypeReference<String>(){}),

    APPLICATION_DECISION(
        "applicationDecision", new TypeReference<String>(){}),

    APPLICATION_DECISION_REASON(
        "applicationDecisionReason", new TypeReference<String>(){}),

    APPLICATION_TYPE(
        "applicationType", new TypeReference<String>(){}),

    MAKE_AN_APPLICATION_TYPES(
        "makeAnApplicationTypes", new TypeReference<DynamicList>(){}),

    MAKE_AN_APPLICATION_DETAILS_LABEL(
        "makeAnApplicationDetailsLabel", new TypeReference<String>(){}),

    APPLICATION_SUPPLIER(
        "applicationSupplier", new TypeReference<String>(){}),

    APPLICATIONS(
        "applications", new TypeReference<List<IdValue<Application>>>(){}),

    MAKE_AN_APPLICATIONS_LIST(
        "makeAnApplicationsList", new TypeReference<DynamicList>(){}),

    APPLICATION_DETAILS(
        "applicationDetails", new TypeReference<String>(){}),

    APPLICATION_WITHDRAW_EXISTS(
        "applicationWithdrawExists", new TypeReference<String>(){}),

    APPLICATION_TIME_EXTENSION_EXISTS(
        "applicationTimeExtensionExists", new TypeReference<String>(){}),

    APPLICATION_EDIT_LISTING_EXISTS(
        "applicationEditListingExists", new TypeReference<String>(){}),

    APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS(
        "applicationUpdateHearingRequirementsExists", new TypeReference<String>() {}),

    APPLICATION_CHANGE_HEARING_CENTRE_EXISTS(
        "applicationChangeHearingCentreExists", new TypeReference<String>() {}),

    UPDATE_HEARING_REQUIREMENTS_EXISTS(
        "updateHearingRequirementsExists", new TypeReference<YesOrNo>(){}),

    DISABLE_OVERVIEW_PAGE(
        "disableOverviewPage", new TypeReference<String>(){}),

    JUDGE_ALLOCATION_EXISTS(
        "judgeAllocationExists", new TypeReference<YesOrNo>(){}),

    ALLOCATED_JUDGE(
        "allocatedJudge", new TypeReference<String>(){}),

    ALLOCATED_JUDGE_EDIT(
        "allocatedJudgeEdit", new TypeReference<String>(){}),

    PREVIOUS_JUDGE_ALLOCATIONS(
        "previousJudgeAllocations", new TypeReference<List<IdValue<String>>>(){}
    ),

    ARIA_LISTING_REFERENCE(
        "ariaListingReference", new TypeReference<String>(){}),

    END_APPEAL_DATE(
        "endAppealDate", new TypeReference<String>(){}),

    HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED(
        "haveHearingAttendeesAndDurationBeenRecorded", new TypeReference<YesOrNo>(){}),

    IS_DECISION_ALLOWED(
        "isDecisionAllowed", new TypeReference<AppealDecision>(){}),

    APPEAL_DECISION(
        "appealDecision", new TypeReference<String>(){}),

    APPEAL_DATE(
        "appealDate", new TypeReference<String>(){}),

    ADD_CASE_NOTE_ACTION_DISABLED(
        "addCaseNoteActionDisabled", new TypeReference<YesOrNo>(){}),

    APPEAL_DECISION_AVAILABLE(
        "appealDecisionAvailable", new TypeReference<YesOrNo>(){}),

    RECORD_APPLICATION_ACTION_DISABLED(
        "recordApplicationActionDisabled", new TypeReference<YesOrNo>(){}),

    UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_ACTION_AVAILABLE(
        "uploadAdditionalEvidenceHomeOfficeActionAvailable", new TypeReference<YesOrNo>() {}),

    ADDITIONAL_EVIDENCE_HOME_OFFICE(
        "additionalEvidenceHomeOffice", new TypeReference<List<IdValue<DocumentWithDescription>>>() {}),

    REVIEW_RESPONSE_ACTION_AVAILABLE(
        "reviewResponseActionAvailable", new TypeReference<YesOrNo>(){}),

    AMEND_RESPONSE_ACTION_AVAILABLE(
        "amendResponseActionAvailable", new TypeReference<YesOrNo>() {}),

    APPEAL_SUBMISSION_DATE(
        "appealSubmissionDate", new TypeReference<String>() {}),

    JOURNEY_TYPE(
        "journeyType", new TypeReference<JourneyType>(){}),

    HEARING_TYPE(
        "hearingType", new TypeReference<HearingType>(){}),

    HEARING_DATE_RANGE_DESCRIPTION(
        "hearingDateRangeDescription", new TypeReference<String>() {}),

    ORG_LIST_OF_USERS(
        "orgListOfUsers", new TypeReference<DynamicList>(){}),

    DIRECTION_LIST(
        "directionList", new TypeReference<DynamicList>(){}),

    DIRECTION_EDIT_EXPLANATION(
        "directionEditExplanation", new TypeReference<String>(){}),

    EDIT_DOCUMENTS_REASON(
        "editDocumentsReason", new TypeReference<String>(){}),

    DIRECTION_EDIT_PARTIES(
        "directionEditParties", new TypeReference<Parties>(){}),

    DIRECTION_EDIT_DATE_DUE(
        "directionEditDateDue", new TypeReference<String>(){}),

    DIRECTION_EDIT_DATE_SENT(
        "directionEditDateSent", new TypeReference<String>(){}),

    REVIEWED_HEARING_REQUIREMENTS(
        "reviewedHearingRequirements", new TypeReference<YesOrNo>() {}),

    REVIEWED_UPDATED_HEARING_REQUIREMENTS(
        "reviewedUpdatedHearingRequirements", new TypeReference<YesOrNo>() {}),

    CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS(
        "caseListedWithoutHearingRequirements", new TypeReference<YesOrNo>() {}),

    REVIEW_HOME_OFFICE_RESPONSE_BY_LEGAL_REP(
        "reviewHomeOfficeResponseByLegalRep", new TypeReference<YesOrNo>(){}),

    REMOVE_APPEAL_FROM_ONLINE_DATE(
        "removeAppealFromOnlineDate", new TypeReference<String>() {}),

    WITNESS_COUNT(
        "witnessCount", new TypeReference<String>() {}),

    WITNESS_DETAILS(
        "witnessDetails", new TypeReference<List<IdValue<WitnessDetails>>>() {}),

    WITNESS_DETAILS_READONLY(
        "witnessDetailsReadonly", new TypeReference<List<IdValue<WitnessDetails>>>() {}),

    INTERPRETER_LANGUAGE(
        "interpreterLanguage", new TypeReference<List<IdValue<InterpreterLanguage>>>() {}),

    INTERPRETER_LANGUAGE_READONLY(
        "interpreterLanguageReadonly", new TypeReference<List<IdValue<InterpreterLanguage>>>() {}),

    VULNERABILITIES_TRIBUNAL_RESPONSE(
        "vulnerabilitiesTribunalResponse", new TypeReference<String>() {}),

    MULTIMEDIA_TRIBUNAL_RESPONSE(
        "multimediaTribunalResponse", new TypeReference<String>() {}),

    SINGLE_SEX_COURT_TRIBUNAL_RESPONSE(
        "singleSexCourtTribunalResponse", new TypeReference<String>() {}),

    IN_CAMERA_COURT_TRIBUNAL_RESPONSE(
        "inCameraCourtTribunalResponse", new TypeReference<String>() {}),

    ADDITIONAL_TRIBUNAL_RESPONSE(
        "additionalTribunalResponse", new TypeReference<String>() {}),

    REASON_TO_FORCE_REQUEST_CASE_BUILDING(
        "reasonToForceRequestCaseBuilding", new TypeReference<String>(){}),

    SUBMIT_HEARING_REQUIREMENTS_AVAILABLE(
        "submitHearingRequirementsAvailable", new TypeReference<YesOrNo>() {}),

    AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS(
        "automaticDirectionRequestingHearingRequirements", new TypeReference<String>(){}),

    APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE(
        "applicationChangeDesignatedHearingCentre", new TypeReference<HearingCentre>(){}),

    SUBMIT_TIME_EXTENSION_REASON(
        "submitTimeExtensionReason", new TypeReference<String>(){}),

    SUBMIT_TIME_EXTENSION_EVIDENCE(
        "submitTimeExtensionEvidence", new TypeReference<List<IdValue<Document>>>(){}),

    REVIEW_TIME_EXTENSION_DATE(
        "reviewTimeExtensionDate", new TypeReference<String>(){}),

    REVIEW_TIME_EXTENSION_PARTY(
        "reviewTimeExtensionParty", new TypeReference<Parties>(){}),

    REVIEW_TIME_EXTENSION_REASON(
        "reviewTimeExtensionReason", new TypeReference<String>(){}),

    REVIEW_TIME_EXTENSION_DECISION(
        "reviewTimeExtensionDecision", new TypeReference<TimeExtensionDecision>(){}),

    REVIEW_TIME_EXTENSION_DECISION_REASON(
        "reviewTimeExtensionDecisionReason", new TypeReference<String>(){}),

    TIME_EXTENSIONS(
        "timeExtensions", new TypeReference<List<IdValue<TimeExtension>>>(){}),

    REVIEW_TIME_EXTENSION_REQUIRED(
        "reviewTimeExtensionRequired", new TypeReference<YesOrNo>() {}),

    REVIEW_TIME_EXTENSION_DUE_DATE(
        "reviewTimeExtensionDueDate", new TypeReference<String>(){}),

    FLAG_CASE_TYPE_OF_FLAG(
        "flagCaseTypeOfFlag", new TypeReference<CaseFlagType>(){}),

    FLAG_CASE_ADDITIONAL_INFORMATION(
        "flagCaseAdditionalInformation", new TypeReference<String>(){}),

    CASE_FLAGS(
        "caseFlags", new TypeReference<List<IdValue<CaseFlag>>>(){}),

    CASE_FLAG_ANONYMITY_EXISTS(
        "caseFlagAnonymityExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION(
        "caseFlagAnonymityAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_COMPLEX_CASE_EXISTS(
        "caseFlagComplexCaseExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION(
        "caseFlagComplexCaseAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_DEPORT_EXISTS(
        "caseFlagDeportExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION(
        "caseFlagDeportAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS(
        "caseFlagDetainedImmigrationAppealExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION(
        "caseFlagDetainedImmigrationAppealAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS(
        "caseFlagForeignNationalOffenderExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION(
        "caseFlagForeignNationalOffenderAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS(
        "caseFlagPotentiallyViolentPersonExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION(
        "caseFlagPotentiallyViolentPersonAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS(
        "caseFlagUnacceptableCustomerBehaviourExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION(
        "caseFlagUnacceptableCustomerBehaviourAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS(
        "caseFlagUnaccompaniedMinorExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION(
        "caseFlagUnaccompaniedMinorAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_SET_ASIDE_REHEARD_EXISTS(
        "caseFlagSetAsideReheardExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_SET_ASIDE_REHEARD_ADDITIONAL_INFORMATION(
        "caseFlagSetAsideReheardAdditionalInformation", new TypeReference<String>() {}),

    IS_APPELLANT_MINOR(
        "isAppellantMinor", new TypeReference<YesOrNo>() {}),
    APPELLANT_DATE_OF_BIRTH(
        "appellantDateOfBirth", new TypeReference<String>() {}),
    REMOVE_FLAG_TYPE_OF_FLAG(
        "removeFlagTypeOfFlag", new TypeReference<DynamicList>() {}),
    REQUEST_CMA_REQUIREMENTS_REASONS(
            "requestCmaRequirementsReasons", new TypeReference<String>() {}),
    APPELLANT_DOCUMENTS(
            "appellantDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    REASONS_FOR_APPEAL_DOCUMENTS(
            "reasonsForAppealDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    CLARIFYING_QUESTIONS_ANSWERS("clarifyingQuestionsAnswers",
            new TypeReference<List<IdValue<ClarifyingQuestionAnswer>>>() {}),
    REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW(
        "reasonToForceCaseToCaseUnderReview", new TypeReference<String>(){}),
    LEGAL_REP_REFERENCE_NUMBER(
        "legalRepReferenceNumber", new TypeReference<String>() {}),

    UPDATE_LEGAL_REP_COMPANY(
        "updateLegalRepCompany", new TypeReference<String>() {}),

    UPDATE_LEGAL_REP_NAME(
        "updateLegalRepName", new TypeReference<String>() {}),

    UPDATE_LEGAL_REP_EMAIL_ADDRESS(
        "updateLegalRepEmailAddress", new TypeReference<String>() {}),

    UPDATE_LEGAL_REP_REFERENCE_NUMBER(
        "updateLegalRepReferenceNumber", new TypeReference<String>() {}),

    REASON_TO_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS(
        "reasonToForceCaseToSubmitHearingRequirements", new TypeReference<String>() {}),

    STATE_BEFORE_ADJOURN_WITHOUT_DATE(
        "stateBeforeAdjournWithoutDate", new TypeReference<String>(){}),

    DATE_BEFORE_ADJOURN_WITHOUT_DATE(
        "dateBeforeAdjournWithoutDate", new TypeReference<String>(){}),

    DOES_THE_CASE_NEED_TO_BE_RELISTED(
        "doesTheCaseNeedToBeRelisted", new TypeReference<YesOrNo>(){}),

    ADJOURN_HEARING_WITHOUT_DATE_REASONS(
            "adjournHearingWithoutDateReasons", new TypeReference<String>(){}),

    APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS(
        "applicationEditAppealAfterSubmitExists", new TypeReference<String>() {}),

    APPLICATION_OUT_OF_TIME_EXPLANATION(
        "applicationOutOfTimeExplanation", new TypeReference<String>() {}),

    APPLICATION_OUT_OF_TIME_DOCUMENT(
        "applicationOutOfTimeDocument", new TypeReference<Document>() {}),

    REASON_FOR_LINK_APPEAL(
        "reasonForLinkAppeal", new TypeReference<ReasonForLinkAppealOptions>() {}),

    HEARING_DECISION_SELECTED(
        "hearingDecisionSelected", new TypeReference<String>(){}),
    IS_FEE_PAYMENT_ENABLED(
            "isFeePaymentEnabled", new TypeReference<YesOrNo>() {}),

    PA_APPEAL_TYPE_PAYMENT_OPTION(
        "paAppealTypePaymentOption", new TypeReference<String>() {}),
    EA_HU_APPEAL_TYPE_PAYMENT_OPTION(
        "eaHuAppealTypePaymentOption", new TypeReference<String>() {}),
    APPEAL_FEE_HEARING_DESC(
            "appealFeeHearingDesc", new TypeReference<String>(){}),
    APPEAL_FEE_WITHOUT_HEARING_DESC(
            "appealFeeWithoutHearingDesc", new TypeReference<String>(){}),
    FEE_HEARING_AMOUNT_FOR_DISPLAY(
            "feeHearingAmountForDisplay", new TypeReference<String>(){}),
    FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY(
            "feeWithoutHearingAmountForDisplay", new TypeReference<String>(){}),
    PAYMENT_STATUS(
            "paymentStatus", new TypeReference<PaymentStatus>(){}),
    FEE_PAYMENT_APPEAL_TYPE(
            "feePaymentAppealType", new TypeReference<String>(){}),
    PAYMENT_REFERENCE(
            "paymentReference", new TypeReference<String>() {}),
    PAID_DATE(
        "paidDate", new TypeReference<String>(){}),
    PAYMENT_DATE(
        "paymentDate", new TypeReference<String>(){}),
    FEE_CODE(
            "feeCode", new TypeReference<String>(){}),
    FEE_DESCRIPTION(
            "feeDescription", new TypeReference<String>(){}),
    FEE_VERSION(
            "feeVersion", new TypeReference<String>(){}),
    FEE_AMOUNT_FOR_DISPLAY(
        "feeAmountForDisplay", new TypeReference<String>(){}),
    PBA_NUMBER(
            "pbaNumber", new TypeReference<String>(){}),
    PAYMENT_DESCRIPTION(
            "paymentDescription", new TypeReference<String>(){}),
    PAYMENT_ERROR_CODE(
            "paymentErrorCode", new TypeReference<String>(){}),
    PAYMENT_ERROR_MESSAGE(
            "paymentErrorMessage", new TypeReference<String>(){}),
    PAYMENT_FAILED_FOR_DISPLAY(
        "paymentFailedForDisplay", new TypeReference<String>(){}),
    PAYMENT_OFFLINE_FOR_DISPLAY(
        "paymentOfflineForDisplay", new TypeReference<String>(){}),
    DECISION_WITH_HEARING(
        "decisionWithHearing", new TypeReference<String>(){}),
    DECISION_WITHOUT_HEARING(
        "decisionWithoutHearing", new TypeReference<String>(){}),
    DECISION_HEARING_FEE_OPTION(
        "decisionHearingFeeOption", new TypeReference<String>(){}),

    DATES_TO_AVOID(
        "datesToAvoid", new TypeReference<List<IdValue<DatesToAvoid>>>(){}),

    DATES_TO_AVOID_READONLY(
        "datesToAvoidReadonly", new TypeReference<List<IdValue<DatesToAvoid>>>() {}),
    HMCTS(
            "hmcts", new TypeReference<String>(){}),

    CUSTOM_HEARING_DOCUMENTS(
            "customHearingDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    IS_HOME_OFFICE_INTEGRATION_ENABLED(
        "isHomeOfficeIntegrationEnabled", new TypeReference<YesOrNo>() {}),

    HOME_OFFICE_CASE_STATUS_DATA(
        "homeOfficeCaseStatusData", new TypeReference<HomeOfficeCaseStatus>() {}),

    HOME_OFFICE_SEARCH_STATUS(
        "homeOfficeSearchStatus", new TypeReference<String>() {}),

    HOME_OFFICE_SEARCH_STATUS_MESSAGE(
        "homeOfficeSearchStatusMessage", new TypeReference<String>() {}),

    HOME_OFFICE_INSTRUCT_STATUS(
        "homeOfficeInstructStatus", new TypeReference<String>() {}),

    CUSTOM_LEGAL_REP_DOCUMENTS(
            "customLegalRepDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS(
            "customAdditionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_RESPONDENT_DOCUMENTS(
            "customRespondentDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    STAFF_LOCATION(
        "staffLocation", new TypeReference<String>(){}),

    STAFF_LOCATION_ID(
        "staffLocationId", new TypeReference<String>(){}),

    UPLOAD_SENSITIVE_DOCS_FILE_UPLOADS(
        "uploadSensitiveDocsFileUploads", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    UPLOAD_SENSITIVE_DOCS(
        "uploadSensitiveDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    UPLOAD_SENSITIVE_DOCS_IS_APPELLANT_RESPONDENT(
        "uploadSensitiveDocsIsAppellantRespondent", new TypeReference<String>(){}),

    CASE_MANAGEMENT_LOCATION(
        "caseManagementLocation", new TypeReference<CaseManagementLocation>(){}),

    MAKE_AN_APPLICATION_TYPE(
        "makeAnApplicationType", new TypeReference<String>(){}),

    MAKE_AN_APPLICATION_DETAILS(
        "makeAnApplicationDetails", new TypeReference<String>(){}),

    MAKE_AN_APPLICATION_EVIDENCE(
        "makeAnApplicationEvidence", new TypeReference<List<IdValue<Document>>>(){}),

    MAKE_AN_APPLICATION_DATE(
        "makeAnApplicationDate", new TypeReference<String>(){}),

    MAKE_AN_APPLICATION_DECISION(
        "makeAnApplicationDecision", new TypeReference<MakeAnApplicationDecision>(){}),

    MAKE_AN_APPLICATION_DECISION_REASON(
        "makeAnApplicationDecisionReason", new TypeReference<String>(){}),

    MAKE_AN_APPLICATIONS(
        "makeAnApplications", new TypeReference<List<IdValue<MakeAnApplication>>>(){}),

    MAKE_AN_APPLICATION_FIELDS(
        "makeAnApplicationFields", new TypeReference<String>(){}),

    DECIDE_AN_APPLICATION_ID(
        "decideAnApplicationId", new TypeReference<String>(){}),

    STATE_BEFORE_END_APPEAL(
        "stateBeforeEndAppeal", new TypeReference<State>(){}),

    REINSTATE_APPEAL_DATE(
        "reinstateAppealDate", new TypeReference<String>(){}),

    REINSTATE_APPEAL_REASON(
        "reinstateAppealReason", new TypeReference<String>(){}),

    APPEAL_STATUS(
        "appealStatus", new TypeReference<AppealStatus>(){}),

    REINSTATED_DECISION_MAKER(
        "reinstatedDecisionMaker", new TypeReference<String>(){}),

    HAS_APPLICATIONS_TO_DECIDE(
        "hasApplicationsToDecide", new TypeReference<String>(){}),

    IS_REHEARD_APPEAL_ENABLED(
        "isReheardAppealEnabled", new TypeReference<YesOrNo>() {}),

    ;

    private final String value;
    private final TypeReference typeReference;

    AsylumCaseFieldDefinition(String value, TypeReference typeReference) {
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
