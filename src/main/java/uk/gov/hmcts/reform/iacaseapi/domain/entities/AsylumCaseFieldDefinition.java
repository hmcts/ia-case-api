package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

import java.util.List;

public enum AsylumCaseFieldDefinition {

    WA_DUMMY_POSTCODE("waDummyPostcode", new TypeReference<String>(){}),

    CHANGE_ORGANISATION_REQUEST_FIELD(
        "changeOrganisationRequestField", new TypeReference<ChangeOrganisationRequest>(){}),

    BUNDLE_CONFIGURATION(
        "bundleConfiguration", new TypeReference<String>(){}),

    CASE_BUNDLES(
        "caseBundles", new TypeReference<List<IdValue<Bundle>>>(){}),

    STITCHING_STATUS(
        "stitchingStatus", new TypeReference<String>(){}),

    STITCHING_STATUS_UPPER_TRIBUNAL(
        "stitchingStatusUpperTribunal", new TypeReference<String>(){}),

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

    APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J(
        "appellantHasFixedAddressAdminJ", new TypeReference<YesOrNo>(){}),

    APPELLANT_ADDRESS(
        "appellantAddress", new TypeReference<AddressUk>(){}),

    ADDRESS_LINE_1_ADMIN_J(
        "addressLine1AdminJ", new TypeReference<String>(){}),

    ADDRESS_LINE_2_ADMIN_J(
        "addressLine2AdminJ", new TypeReference<String>(){}),

    ADDRESS_LINE_3_ADMIN_J(
        "addressLine3AdminJ", new TypeReference<String>(){}),

    ADDRESS_LINE_4_ADMIN_J(
        "addressLine4AdminJ", new TypeReference<String>(){}),

    COUNTRY_GOV_UK_OOC_ADMIN_J(
        "countryGovUkOocAdminJ", new TypeReference<NationalityFieldValue>(){}),

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

    APPEAL_TYPE_FOR_DISPLAY(
        "appealTypeForDisplay", new TypeReference<AppealTypeForDisplay>(){}),

    APPEAL_TYPE_PREVIOUS_SELECTION(
        "appealTypePreviousSelection", new TypeReference<AppealType>(){}),

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

    APPEAL_GROUNDS_DECISION_HUMAN_RIGHTS_REFUSAL(
        "appealGroundsDecisionHumanRightsRefusal", new TypeReference<CheckValues<String>>(){}),

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

    END_APPEAL_OUTCOME(
            "endAppealOutcome", new TypeReference<String>(){}),
    END_APPEAL_OUTCOME_REASON(
            "endAppealOutcomeReason", new TypeReference<String>(){}),

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

    REHEARD_HEARING_DOCUMENTS(
        "reheardHearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    LEGAL_REPRESENTATIVE_DOCUMENTS(
        "legalRepresentativeDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    UPLOADED_LEGAL_REP_BUILD_CASE_DOCS(
        "uploadedLegalRepBuildCaseDocs", new TypeReference<String>(){}),

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

    FTPA_APPELLANT_GROUNDS(
            "ftpaAppellantGrounds", new TypeReference<String>(){}),

    FTPA_APPELLANT_APPLICATION_DATE(
        "ftpaAppellantApplicationDate", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_DOCUMENT(
        "ftpaAppellantDecisionDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPLICATION_APPELLANT_DOCUMENT(
        "ftpaApplicationAppellantDocument", new TypeReference<Document>(){}),
    FTPA_APPELLANT_NOTICE_DOCUMENT(
        "ftpaAppellantNoticeDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_DECISION_DOCUMENT(
        "ftpaRespondentDecisionDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_APPLICATION_RESPONDENT_DOCUMENT(
        "ftpaApplicationRespondentDocument", new TypeReference<Document>(){}),

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

    FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT(
        "ftpaAppellantDecisionRemadeRule32Text", new TypeReference<String>(){}),

    FTPA_RESPONDENT_RJ_NEW_DECISION_OF_APPEAL(
        "ftpaRespondentRjNewDecisionOfAppeal", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_REMADE_RULE_32(
        "ftpaRespondentDecisionRemadeRule32", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT(
        "ftpaRespondentDecisionRemadeRule32Text", new TypeReference<String>(){}),
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

    UPLOAD_THE_NOTICE_OF_DECISION_DOCS(
        "uploadTheNoticeOfDecisionDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    UPLOAD_THE_APPEAL_FORM_DOCS(
        "uploadTheAppealFormDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

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

    LIST_CASE_HEARING_CENTRE_ADDRESS(
        "listCaseHearingCentreAddress",  new TypeReference<String>(){}),

    LISTING_LOCATION(
        "listingLocation", new TypeReference<DynamicList>(){}),

    IS_CASE_USING_LOCATION_REF_DATA(
        "isCaseUsingLocationRefData", new TypeReference<YesOrNo>(){}),

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

    LEGAL_REP_FAMILY_NAME(
            "legalRepFamilyName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_NAME(
            "legalRepCompanyName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_ADDRESS(
            "legalRepCompanyAddress", new TypeReference<AddressUk>(){}),

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
        "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    LEGAL_REP_MOBILE_PHONE_NUMBER(
            "legalRepMobilePhoneNumber", new TypeReference<String>(){}),

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

    LAST_MODIFIED_DIRECTION(
        "lastModifiedDirection", new TypeReference<Direction>(){}),

    LAST_MODIFIED_APPLICATION(
            "lastModifiedApplication", new TypeReference<MakeAnApplication>(){}),

    DECISION_AND_REASONS_AVAILABLE(
        "decisionAndReasonsAvailable", new TypeReference<YesOrNo>(){}),

    FTPA_APPLICATION_DEADLINE(
            "ftpaApplicationDeadline", new TypeReference<String>(){}),

    APPEAL_SUBMISSION_INTERNAL_DATE(
            "appealSubmissionInternalDate", new TypeReference<String>(){}),

    TRIBUNAL_RECEIVED_DATE(
            "tribunalReceivedDate", new TypeReference<String>(){}),

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

    HEARING_TYPE_RESULT(
        "hearingTypeResult", new TypeReference<YesOrNo>(){}),

    LETTER_SENT_OR_RECEIVED(
            "letterSentOrReceived", new TypeReference<String>(){}),

    PREV_JOURNEY_TYPE(
        "prevJourneyType", new TypeReference<JourneyType>(){}),

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

    IS_WITNESSES_ATTENDING(
        "isWitnessesAttending", new TypeReference<YesOrNo>() {}),

    IS_INTERPRETER_SERVICES_NEEDED(
        "isInterpreterServicesNeeded", new TypeReference<YesOrNo>(){}),

    IS_SIGN_SERVICES_NEEDED(
            "isSignServicesNeeded", new TypeReference<YesOrNo>(){}),

    LANGUAGE_MANUAL_ENTER(
            "languageManualEnter", new TypeReference<String>(){}),

    IS_ANY_WITNESS_INTERPRETER_REQUIRED(
        "isAnyWitnessInterpreterRequired", new TypeReference<YesOrNo>(){}),

    WITNESS_DETAILS(
        "witnessDetails", new TypeReference<List<IdValue<WitnessDetails>>>() {}),

    INCLUSIVE_WITNESS_DETAILS(
        "inclusiveWitnessDetails", new TypeReference<List<IdValue<WitnessDetails>>>() {}),

    WHICH_WITNESSES_NEED_INTERPRETER(
        "whichWitnessesNeedInterpreter", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_1(
        "witness1", new TypeReference<WitnessDetails>() {}),

    WITNESS_2(
        "witness2", new TypeReference<WitnessDetails>() {}),

    WITNESS_3(
        "witness3", new TypeReference<WitnessDetails>() {}),

    WITNESS_4(
        "witness4", new TypeReference<WitnessDetails>() {}),

    WITNESS_5(
        "witness5", new TypeReference<WitnessDetails>() {}),

    WITNESS_6(
        "witness6", new TypeReference<WitnessDetails>() {}),

    WITNESS_7(
        "witness7", new TypeReference<WitnessDetails>() {}),

    WITNESS_8(
        "witness8", new TypeReference<WitnessDetails>() {}),

    WITNESS_9(
        "witness9", new TypeReference<WitnessDetails>() {}),

    WITNESS_10(
        "witness10", new TypeReference<WitnessDetails>() {}),

    WITNESS_LIST_ELEMENT_1(
        "witnessListElement1", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_2(
        "witnessListElement2", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_3(
        "witnessListElement3", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_4(
        "witnessListElement4", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_5(
        "witnessListElement5", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_6(
        "witnessListElement6", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_7(
        "witnessListElement7", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_8(
        "witnessListElement8", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_9(
        "witnessListElement9", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_LIST_ELEMENT_10(
        "witnessListElement10", new TypeReference<DynamicMultiSelectList>() {}),

    WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY(
        "witness1InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY(
        "witness2InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY(
        "witness3InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_4_INTERPRETER_LANGUAGE_CATEGORY(
        "witness4InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_5_INTERPRETER_LANGUAGE_CATEGORY(
        "witness5InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_6_INTERPRETER_LANGUAGE_CATEGORY(
        "witness6InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_7_INTERPRETER_LANGUAGE_CATEGORY(
        "witness7InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_8_INTERPRETER_LANGUAGE_CATEGORY(
        "witness8InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_9_INTERPRETER_LANGUAGE_CATEGORY(
        "witness9InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_10_INTERPRETER_LANGUAGE_CATEGORY(
        "witness10InterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE(
        "witness1InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_1_INTERPRETER_SIGN_LANGUAGE(
        "witness1InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE(
        "witness2InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_2_INTERPRETER_SIGN_LANGUAGE(
        "witness2InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE(
        "witness3InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_3_INTERPRETER_SIGN_LANGUAGE(
        "witness3InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE(
        "witness4InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_4_INTERPRETER_SIGN_LANGUAGE(
        "witness4InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE(
        "witness5InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_5_INTERPRETER_SIGN_LANGUAGE(
        "witness5InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE(
        "witness6InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_6_INTERPRETER_SIGN_LANGUAGE(
        "witness6InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE(
        "witness7InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_7_INTERPRETER_SIGN_LANGUAGE(
        "witness7InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE(
        "witness8InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_8_INTERPRETER_SIGN_LANGUAGE(
        "witness8InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE(
        "witness9InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_9_INTERPRETER_SIGN_LANGUAGE(
        "witness9InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE(
        "witness10InterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    WITNESS_10_INTERPRETER_SIGN_LANGUAGE(
        "witness10InterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    IS_WITNESS_1_INTERPRETER_NEEDED(
        "isWitness1InterpreterNeeded", new TypeReference<String>() {}),

    WITNESS_DETAILS_READONLY(
        "witnessDetailsReadonly", new TypeReference<List<IdValue<WitnessDetails>>>() {}),

    HEARING_CHANNEL(
            "hearingChannel", new TypeReference<DynamicList>(){}),

    INTERPRETER_LANGUAGE(
        "interpreterLanguage", new TypeReference<List<IdValue<InterpreterLanguage>>>() {}),

    APPELLANT_INTERPRETER_SPOKEN_LANGUAGE(
        "appellantInterpreterSpokenLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    APPELLANT_INTERPRETER_SIGN_LANGUAGE(
        "appellantInterpreterSignLanguage", new TypeReference<InterpreterLanguageRefData>() {}),

    APPELLANT_INTERPRETER_LANGUAGE_CATEGORY(
        "appellantInterpreterLanguageCategory", new TypeReference<List<String>>() {}),

    INTERPRETER_LANGUAGE_READONLY(
        "interpreterLanguageReadonly", new TypeReference<List<IdValue<InterpreterLanguage>>>() {}),

    REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE(
        "remoteVideoCallTribunalResponse", new TypeReference<String>() {}),

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

    IS_REMOTE_HEARING_ALLOWED(
            "isRemoteHearingAllowed", new TypeReference<String>() {}),

    IS_VULNERABILITIES_ALLOWED(
            "isVulnerabilitiesAllowed", new TypeReference<String>() {}),

    IS_MULTIMEDIA_ALLOWED(
            "isMultimediaAllowed", new TypeReference<String>() {}),

    IS_SINGLE_SEX_COURT_ALLOWED(
            "isSingleSexCourtAllowed", new TypeReference<String>() {}),

    IS_IN_CAMERA_COURT_ALLOWED(
            "isInCameraCourtAllowed", new TypeReference<String>() {}),

    IS_ADDITIONAL_ADJUSTMENTS_ALLOWED(
            "isAdditionalAdjustmentsAllowed", new TypeReference<String>() {}),

    REMOTE_HEARING_DECISION_FOR_DISPLAY(
            "remoteHearingDecisionForDisplay", new TypeReference<String>() {}),

    MULTIMEDIA_DECISION_FOR_DISPLAY(
            "multimediaDecisionForDisplay", new TypeReference<String>() {}),

    SINGLE_SEX_COURT_DECISION_FOR_DISPLAY(
            "singleSexCourtDecisionForDisplay", new TypeReference<String>() {}),

    IN_CAMERA_COURT_DECISION_FOR_DISPLAY(
            "inCameraCourtDecisionForDisplay", new TypeReference<String>() {}),

    VULNERABILITIES_DECISION_FOR_DISPLAY(
            "vulnerabilitiesDecisionForDisplay", new TypeReference<String>() {}),

    OTHER_DECISION_FOR_DISPLAY(
            "otherDecisionForDisplay", new TypeReference<String>() {}),

    REASON_TO_FORCE_REQUEST_CASE_BUILDING(
        "reasonToForceRequestCaseBuilding", new TypeReference<String>(){}),

    SUBMIT_HEARING_REQUIREMENTS_AVAILABLE(
        "submitHearingRequirementsAvailable", new TypeReference<YesOrNo>() {}),

    ACCELERATED_DETAINED_APPEAL_LISTED(
        "acceleratedDetainedAppealListed", new TypeReference<YesOrNo>() {}),

    ADA_HEARING_REQUIREMENTS_SUBMITTABLE(
        "adaHearingRequirementsSubmittable", new TypeReference<YesOrNo>() {}),

    ADA_HEARING_REQUIREMENTS_TO_REVIEW(
        "adaHearingRequirementsToReview", new TypeReference<YesOrNo>() {}),

    ADA_HEARING_REQUIREMENTS_UPDATABLE(
            "adaHearingRequirementsUpdatable", new TypeReference<YesOrNo>() {}),

    ADA_HEARING_ADJUSTMENTS_UPDATABLE(
            "adaHearingAdjustmentsUpdatable", new TypeReference<YesOrNo>() {}),

    ADA_EDIT_LISTING_AVAILABLE(
            "adaEditListingAvailable", new TypeReference<YesOrNo>() {}),

    AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS(
        "automaticDirectionRequestingHearingRequirements", new TypeReference<String>(){}),

    AUTOMATIC_END_APPEAL_TIMED_EVENT_ID(
            "automaticEndAppealTimedEventId", new TypeReference<String>(){}),

    AUTOMATIC_SEND_PAYMENT_REMINDER_TIMED_EVENT_ID(
        "automaticSendPaymentReminderTimedEventId", new TypeReference<String>(){}),

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

    LEGACY_CASE_FLAGS(
        "legacyCaseFlags", new TypeReference<List<IdValue<LegacyCaseFlag>>>(){}),

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

    CASE_FLAG_S94B_OUT_OF_COUNTRY_EXISTS(
        "caseFlagS94bOutOfCountryExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_S94B_OUT_OF_COUNTRY_ADDITIONAL_INFORMATION(
        "caseFlagS94bOutOfCountryAdditionalInformation", new TypeReference<String>() {}),

    CASE_FLAG_APPEAL_ON_HOLD_EXISTS(
            "caseFlagAppealOnHoldExists", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_APPEAL_ON_HOLD_ADDITIONAL_INFORMATION(
            "caseFlagAppealOnHoldAdditionalInformation", new TypeReference<String>() {}),

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
    REASONS_FOR_APPEAL_DECISION(
        "reasonsForAppealDecision", new TypeReference<String>(){}),
    REASONS_FOR_APPEAL_DATE_UPLOADED(
        "reasonsForAppealDateUploaded", new TypeReference<String>(){}),
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

    UPDATE_LEGAL_REP_FAMILY_NAME(
            "updateLegalRepFamilyName", new TypeReference<String>() {}),

    UPDATE_LEGAL_REP_EMAIL_ADDRESS(
        "updateLegalRepEmailAddress", new TypeReference<String>() {}),

    UPDATE_LEGAL_REP_MOBILE_PHONE_NUMBER(
            "updateLegalRepMobilePhoneNumber", new TypeReference<String>() {}),

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

    PA_APPEAL_TYPE_AIP_PAYMENT_OPTION(
            "paAppealTypeAipPaymentOption", new TypeReference<String>() {}),

    EA_HU_APPEAL_TYPE_PAYMENT_OPTION(
        "eaHuAppealTypePaymentOption", new TypeReference<String>() {}),
    APPEAL_FEE_HEARING_DESC(
            "appealFeeHearingDesc", new TypeReference<String>(){}),
    APPEAL_FEE_WITHOUT_HEARING_DESC(
            "appealFeeWithoutHearingDesc", new TypeReference<String>(){}),
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
    FEE_AMOUNT_GBP(
        "feeAmountGbp", new TypeReference<String>(){}),
    PREVIOUS_FEE_AMOUNT_GBP(
        "previousFeeAmountGbp", new TypeReference<String>(){}),
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

    FEE_WITH_HEARING(
        "feeWithHearing", new TypeReference<String>(){}),

    FEE_WITHOUT_HEARING(
        "feeWithoutHearing", new TypeReference<String>(){}),

    DATES_TO_AVOID_YES_NO(
        "datesToAvoidYesNo", new TypeReference<YesOrNo>(){}),

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

    HOME_OFFICE_NOTIFICATIONS_ELIGIBLE(
        "homeOfficeNotificationsEligible", new TypeReference<YesOrNo>() {}),

    HOME_OFFICE_SEARCH_STATUS_MESSAGE(
        "homeOfficeSearchStatusMessage", new TypeReference<String>() {}),

    HOME_OFFICE_INSTRUCT_STATUS(
        "homeOfficeInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REQUEST_REVIEW_INSTRUCT_STATUS(
        "homeOfficeRequestReviewInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_HEARING_INSTRUCT_STATUS(
        "homeOfficeHearingInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REQUEST_EVIDENCE_INSTRUCT_STATUS(
        "homeOfficeRequestEvidenceInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_EDIT_LISTING_INSTRUCT_STATUS(
        "homeOfficeEditListingInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_ADJOURN_WITHOUT_DATE_INSTRUCT_STATUS(
        "homeOfficeAdjournWithoutDateInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS(
        "homeOfficeHearingBundleReadyInstructStatus", new TypeReference<String>() {}),

    IS_HEARING_BUNDLE_UPDATED(
        "isHearingBundleUpdated", new TypeReference<YesOrNo>() {}),

    HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS(
        "homeOfficeAppealDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_APPELLANT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaAppellantDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_RESPONDENT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaRespondentDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS(
        "homeOfficeEndAppealInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS(
        "homeOfficeAmendBundleInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_AMEND_RESPONSE_INSTRUCT_STATUS(
        "homeOfficeAmendResponseInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REVIEW_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS(
        "homeOfficeReviewChangeDirectionDueDateInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_EVIDENCE_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS(
        "homeOfficeEvidenceChangeDirectionDueDateInstructStatus", new TypeReference<String>() {}),

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

    CUSTOM_FTPA_APPELLANT_EVIDENCE_DOCS(
            "customFtpaAppellantEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS(
            "customAppAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS(
            "customRespAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_FTPA_APPELLANT_DOCS(
            "customFtpaAppellantDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_FTPA_RESPONDENT_DOCS(
            "customFtpaRespondentDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_FINAL_DECISION_AND_REASONS_DOCS(
            "customFinalDecisionAndReasonsDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS(
            "customAppAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    APPELLANT_ADDENDUM_EVIDENCE_DOCS(
            "appellantAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS(
            "customRespAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    RESPONDENT_ADDENDUM_EVIDENCE_DOCS(
            "respondentAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CUSTOM_REHEARD_HEARING_DOCS(
            "customReheardHearingDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    APP_ADDITIONAL_EVIDENCE_DOCS(
        "appAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RESP_ADDITIONAL_EVIDENCE_DOCS(
        "respAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),


    ATTENDING_JUDGE(
        "attendingJudge", new TypeReference<String>(){}),

    ATTENDING_APPELLANT(
        "attendingAppellant", new TypeReference<String>(){}),

    ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE(
        "attendingHomeOfficeLegalRepresentative", new TypeReference<String>(){}),

    LIST_CASE_HEARING_LENGTH(
        "listCaseHearingLength", new TypeReference<String>() {}),

    LISTING_LENGTH(
        "listingLength", new TypeReference<HoursMinutes>() {}),

    HEARING_REQUIREMENTS(
        "hearingRequirements", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ACTUAL_CASE_HEARING_LENGTH(
        "actualCaseHearingLength", new TypeReference<HoursAndMinutes>() {}),

    PREVIOUS_HEARINGS(
        "previousHearings", new TypeReference<List<IdValue<PreviousHearing>>>(){}),

    CURRENT_HEARING_DETAILS_VISIBLE(
        "currentHearingDetailsVisible", new TypeReference<YesOrNo>() {}),

    ATTENDING_TCW(
        "attendingTCW", new TypeReference<String>(){}),

    ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE(
        "attendingAppellantsLegalRepresentative", new TypeReference<String>(){}),

    HEARING_CONDUCTION_OPTIONS(
        "hearingConductionOptions", new TypeReference<List<IdValue<HearingConductionOptions>>>(){}),

    REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS(
        "reheardCaseListedWithoutHearingRequirements", new TypeReference<YesOrNo>() {}),

    LIST_CASE_HEARING_LENGTH_VISIBLE(
        "listCaseHearingLengthVisible", new TypeReference<YesOrNo>() {}),

    PREVIOUS_HEARING_REQUIREMENTS(
        "previousHearingRequirements", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    PREVIOUS_HEARING_RECORDING_DOCUMENTS(
        "previousHearingRecordingDocuments", new TypeReference<List<IdValue<HearingRecordingDocument>>>(){}),

    CASE_INTRODUCTION_DESCRIPTION(
        "caseIntroductionDescription", new TypeReference<String>(){}),

    APPELLANT_CASE_SUMMARY_DESCRIPTION(
        "appellantCaseSummaryDescription", new TypeReference<String>(){}),

    IMMIGRATION_HISTORY_AGREEMENT(
        "immigrationHistoryAgreement", new TypeReference<YesOrNo>() {}),

    AGREED_IMMIGRATION_HISTORY_DESCRIPTION(
        "agreedImmigrationHistoryDescription", new TypeReference<String>(){}),

    SCHEDULE_OF_ISSUES_AGREEMENT(
        "scheduleOfIssuesAgreement", new TypeReference<YesOrNo>() {}),

    SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION(
        "scheduleOfIssuesDisagreementDescription", new TypeReference<String>(){}),

    ANONYMITY_ORDER(
        "anonymityOrder", new TypeReference<YesOrNo>() {}),

    APPELLANT_REPRESENTATIVE(
        "appellantRepresentative", new TypeReference<String>(){}),

    RESPONDENT_REPRESENTATIVE(
        "respondentRepresentative", new TypeReference<String>(){}),

    FINAL_DECISION_AND_REASONS_DOCUMENT(
        "finalDecisionAndReasonsDocument", new TypeReference<Document>(){}),

    IS_DOCUMENT_SIGNED_TODAY(
        "isDocumentSignedToday", new TypeReference<YesOrNo>() {}),

    IS_FEE_CONSISTENT_WITH_DECISION(
        "isFeeConsistentWithDecision", new TypeReference<YesOrNo>() {}),

    IS_REMISSIONS_ENABLED(
        "isRemissionsEnabled", new TypeReference<YesOrNo>(){}),

    RP_DC_APPEAL_HEARING_OPTION(
        "rpDcAppealHearingOption", new TypeReference<String>(){}),

    REMISSION_TYPE(
        "remissionType", new TypeReference<RemissionType>(){}),

    LATE_REMISSION_TYPE(
        "lateRemissionType", new TypeReference<RemissionType>(){}),

    HAS_PREVIOUS_REMISSION(
        "hasPreviousRemission", new TypeReference<YesOrNo>(){}),

    REMISSION_CLAIM(
        "remissionClaim", new TypeReference<String>(){}),

    FEE_REMISSION_TYPE(
        "feeRemissionType", new TypeReference<String>(){}),

    ASYLUM_SUPPORT_REFERENCE(
        "asylumSupportReference", new TypeReference<String>(){}),

    ASYLUM_SUPPORT_DOCUMENT(
        "asylumSupportDocument", new TypeReference<Document>(){}),

    LEGAL_AID_ACCOUNT_NUMBER(
        "legalAidAccountNumber", new TypeReference<String>(){}),

    SECTION17_DOCUMENT(
        "section17Document", new TypeReference<Document>(){}),

    SECTION20_DOCUMENT(
        "section20Document", new TypeReference<Document>(){}),

    HOME_OFFICE_WAIVER_DOCUMENT(
        "homeOfficeWaiverDocument", new TypeReference<Document>(){}),

    EXCEPTIONAL_CIRCUMSTANCES(
        "exceptionalCircumstances", new TypeReference<String>(){}),

    REMISSION_EC_EVIDENCE_DOCUMENTS(
        "remissionEcEvidenceDocuments", new TypeReference<List<IdValue<Document>>>(){}),

    REMISSION_DECISION(
        "remissionDecision", new TypeReference<RemissionDecision>(){}),

    AMOUNT_REMITTED(
        "amountRemitted", new TypeReference<String>(){}),

    AMOUNT_LEFT_TO_PAY(
        "amountLeftToPay", new TypeReference<String>(){}),

    REMISSION_REJECTED_DATE_PLUS_14DAYS(
        "remissionRejectedDatePlus14days", new TypeReference<String>(){}),

    CASE_WORKER_NAME_LIST(
            "caseWorkerNameList", new TypeReference<DynamicList>() {}),

    CASE_WORKER_LOCATION_LIST(
            "caseWorkerLocationList", new TypeReference<String>() {}),

    CASE_WORKER_NAME(
            "caseWorkerName", new TypeReference<String>() {}),

    ALLOCATE_THE_CASE_TO(
            "allocateTheCaseTo", new TypeReference<String>() {}),

    LOCAL_AUTHORITY_POLICY(
        "localAuthorityPolicy", new TypeReference<OrganisationPolicy>(){}),

    REVOKE_ACCESS_FOR_USER_ID(
            "revokeAccessForUserId", new TypeReference<String>(){}),

    REVOKE_ACCESS_FOR_USER_ORG_ID(
            "revokeAccessForUserOrgId", new TypeReference<String>(){}),

    FEE_UPDATE_RECORDED(
        "feeUpdateRecorded", new TypeReference<CheckValues<String>>(){}),

    FEE_UPDATE_STATUS(
        "feeUpdateStatus", new TypeReference<CheckValues<String>>(){}),

    DISPLAY_FEE_UPDATE_STATUS(
        "displayFeeUpdateStatus", new TypeReference<YesOrNo>() {}),

    FEE_UPDATE_COMPLETED_STAGES(
        "feeUpdateCompletedStages", new TypeReference<List<String>>(){}),

    FEE_UPDATE_REASON(
        "feeUpdateReason", new TypeReference<FeeUpdateReason>(){}),

    NEW_FEE_AMOUNT(
        "newFeeAmount", new TypeReference<String>(){}),

    APPELLANT_IN_UK(
        "appellantInUk", new TypeReference<YesOrNo>() {}),

    APPELLANT_IN_UK_PREVIOUS_SELECTION(
        "appellantInUkPreviousSelection", new TypeReference<YesOrNo>() {}),

    APPEAL_OUT_OF_COUNTRY(
        "appealOutOfCountry", new TypeReference<YesOrNo>() {}),

    IS_AGE_ASSESSMENT_ENABLED(
        "isAgeAssessmentEnabled", new TypeReference<YesOrNo>() {}),

    IS_AGE_ASSESSMENT_VISIBLE(
        "isAgeAssessmentVisible", new TypeReference<YesOrNo>() {}),

    IS_NABA_ENABLED(
            "isNabaEnabled", new TypeReference<YesOrNo>() {}),

    IS_NABA_ENABLED_OOC(
        "isNabaEnabledOoc", new TypeReference<YesOrNo>() {}),
    IS_NABA_ADA_ENABLED(
        "isNabaAdaEnabled", new TypeReference<YesOrNo>() {}),
    HAS_CORRESPONDENCE_ADDRESS(
        "hasCorrespondenceAddress", new TypeReference<YesOrNo>() {}),

    APPELLANT_OUT_OF_COUNTRY_ADDRESS(
        "appellantOutOfCountryAddress", new TypeReference<String>(){}),

    HAS_SPONSOR(
        "hasSponsor", new TypeReference<YesOrNo>(){}),

    GWF_REFERENCE_NUMBER(
        "gwfReferenceNumber", new TypeReference<String>(){}),
    DATE_ENTRY_CLEARANCE_DECISION(
        "dateEntryClearanceDecision", new TypeReference<String>(){}),

    OUT_OF_COUNTRY_DECISION_TYPE(
        "outOfCountryDecisionType", new TypeReference<OutOfCountryDecisionType>(){}),

    DECISION_LETTER_RECEIVED_DATE(
        "decisionLetterReceivedDate", new TypeReference<String>(){}),

    DATE_CLIENT_LEAVE_UK(
        "dateClientLeaveUk", new TypeReference<String>(){}),

    DATE_CLIENT_LEAVE_UK_ADMIN_J(
        "dateClientLeaveUkAdminJ", new TypeReference<String>(){}),

    OUT_OF_COUNTRY_MOBILE_NUMBER(
        "outOfCountryMobileNumber", new TypeReference<String>(){}),

    SPONSOR_GIVEN_NAMES(
        "sponsorGivenNames", new TypeReference<String>(){}),

    SPONSOR_FAMILY_NAME(
        "sponsorFamilyName", new TypeReference<String>(){}),

    SPONSOR_ADDRESS(
        "sponsorAddress", new TypeReference<AddressUk>(){}),

    SPONSOR_CONTACT_PREFERENCE(
        "sponsorContactPreference", new TypeReference<ContactPreference>(){}),

    SPONSOR_EMAIL(
        "sponsorEmail", new TypeReference<String>(){}),

    SPONSOR_MOBILE_NUMBER(
        "sponsorMobileNumber", new TypeReference<String>(){}),

    SPONSOR_AUTHORISATION(
        "sponsorAuthorisation", new TypeReference<YesOrNo>(){}),

    SPONSOR_NAME_FOR_DISPLAY(
        "sponsorNameForDisplay", new TypeReference<String>(){}),

    SPONSOR_ADDRESS_FOR_DISPLAY(
        "sponsorAddressForDisplay", new TypeReference<String>(){}),

    DEPORTATION_ORDER_OPTIONS(
        "deportationOrderOptions", new TypeReference<YesOrNo>(){}),

    CCD_REFERENCE_NUMBER_FOR_DISPLAY(
        "ccdReferenceNumberForDisplay", new TypeReference<String>(){}),

    REMISSION_DECISION_REASON(
        "remissionDecisionReason", new TypeReference<String>(){}),

    HELP_WITH_FEES_REFERENCE_NUMBER(
        "helpWithFeesReferenceNumber", new TypeReference<String>(){}),

    PREVIOUS_REMISSION_DETAILS(
        "previousRemissionDetails", new TypeReference<List<IdValue<RemissionDetails>>>(){}),

    TEMP_PREVIOUS_REMISSION_DETAILS(
        "tempPreviousRemissionDetails", new TypeReference<List<IdValue<RemissionDetails>>>(){}),

    OUT_OF_TIME_DECISION_MAKER(
        "outOfTimeDecisionMaker", new TypeReference<String>(){}),

    RECORDED_OUT_OF_TIME_DECISION(
        "recordedOutOfTimeDecision", new TypeReference<YesOrNo>(){}),

    OUT_OF_TIME_DECISION_DOCUMENT(
        "outOfTimeDecisionDocument", new TypeReference<Document>(){}),

    OUT_OF_TIME_DECISION_DOCUMENTS(
        "outOfTimeDecisionDocuments", new TypeReference<List<IdValue<Document>>>(){}),

    PREVIOUS_OUT_OF_TIME_DECISION_DETAILS(
        "previousOutOfTimeDecisionDetails", new TypeReference<List<IdValue<OutOfTimeDecisionDetails>>>(){}),

    OUT_OF_TIME_DECISION_TYPE(
        "outOfTimeDecisionType", new TypeReference<OutOfTimeDecisionType>(){}),

    IS_EVIDENCE_FROM_OUTSIDE_UK_OOC(
        "isEvidenceFromOutsideUkOoc", new TypeReference<YesOrNo>() {}),

    IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY(
        "isEvidenceFromOutsideUkInCountry", new TypeReference<YesOrNo>() {}),

    PREVIOUS_REPRESENTATIONS(
        "previousRepresentations", new TypeReference<List<IdValue<PreviousRepresentation>>>(){}),

    CASE_MANAGEMENT_CATEGORY(
        "caseManagementCategory", new TypeReference<DynamicList>(){}),

    HOME_OFFICE_SEARCH_NO_MATCH(
            "homeOfficeSearchNoMatch", new TypeReference<>(){}),

    HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT(
            "homeOfficeReferenceNumberBeforeEdit", new TypeReference<String>(){}),

    UPPER_TRIBUNAL_DOCUMENTS(
        "upperTribunalDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    SECOND_FTPA_DECISION_EXISTS(
        "secondFtpaDecisionExists", new TypeReference<YesOrNo>(){}),

    HMCTS_CASE_NAME_INTERNAL(
        "hmctsCaseNameInternal", new TypeReference<String>() {}),

    CASE_NAME_HMCTS_INTERNAL(
        "caseNameHmctsInternal", new TypeReference<String>() {}),

    APPEAL_REVIEW_OUTCOME(
        "appealReviewOutcome", new TypeReference<AppealReviewOutcome>(){}),

    MARK_EVIDENCE_AS_REVIEWED_ACTION_AVAILABLE(
            "markEvidenceAsReviewedActionAvailable", new TypeReference<YesOrNo>(){}),

    PRE_CLARIFYING_STATE(
        "preClarifyingState", new TypeReference<State>(){}),

    IS_APPEAL_REFERENCE_NUMBER_AVAILABLE(
        "isAppealReferenceNumberAvailable", new TypeReference<YesOrNo>(){}),

    SUBSCRIPTIONS(
        "subscriptions", new TypeReference<List<IdValue<Subscriber>>>(){}),

    SPONSOR_SUBSCRIPTIONS(
        "sponsorSubscriptions", new TypeReference<List<IdValue<Subscriber>>>(){}),

    AIP_SPONSOR_EMAIL_FOR_DISPLAY(
        "aipSponsorEmailForDisplay", new TypeReference<String>(){}),

    AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY(
        "aipSponsorMobileNumberForDisplay", new TypeReference<String>(){}),

    OUTSIDE_UK_WHEN_APPLICATION_MADE(
        "outsideUkWhenApplicationMade", new TypeReference<YesOrNo>() {}),

    OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION(
        "outsideUkWhenApplicationMadePreviousSelection", new TypeReference<YesOrNo>() {}),

    MARK_ADDENDUM_EVIDENCE_AS_REVIEWED_ACTION_AVAILABLE(
            "markAddendumEvidenceAsReviewedActionAvailable", new TypeReference<YesOrNo>(){}),

    PAYMENT_REQUEST_SENT_DATE(
            "paymentRequestSentDate", new TypeReference<String>(){}),

    PAYMENT_REQUEST_SENT_NOTE_DESCRIPTION(
            "paymentRequestSentNoteDescription", new TypeReference<String>(){}),

    PAYMENT_REQUEST_SENT_DOCUMENT(
            "paymentRequestSentDocument", new TypeReference<Document>(){}),

    HMCTS_CASE_CATEGORY(
        "hmctsCaseCategory", new TypeReference<String>(){}),

    HAS_SERVICE_REQUEST_ALREADY(
        "hasServiceRequestAlready", new TypeReference<YesOrNo>(){}),

    SERVICE_REQUEST_REFERENCE(
            "serviceRequestReference", new TypeReference<String>(){}),

    IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS(
        "isServiceRequestTabVisibleConsideringRemissions", new TypeReference<YesOrNo>(){}),

    DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION(
        "displayMarkAsPaidEventForPartialRemission", new TypeReference<YesOrNo>(){}),

    REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST(

        "requestFeeRemissionFlagForServiceRequest", new TypeReference<YesOrNo>(){}),

    APPELLANT_IN_DETENTION(
        "appellantInDetention", new TypeReference<YesOrNo>(){}),

    IS_ACCELERATED_DETAINED_APPEAL(
        "isAcceleratedDetainedAppeal", new TypeReference<YesOrNo>(){}),

    DETENTION_STATUS(
        "detentionStatus", new TypeReference<String>(){}),

    DETENTION_FACILITY(
        "detentionFacility", new TypeReference<String>(){}),

    PRISON_NOMS(
        "prisonNOMSNumber", new TypeReference<PrisonNomsNumber>(){}),

    PRISON_NOMS_AO(
        "prisonNOMSNumberAo", new TypeReference<PrisonNomsNumber>(){}),

    IRC_NAME(
        "ircName", new TypeReference<String>(){}),

    PRISON_NAME(
        "prisonName", new TypeReference<String>(){}),

    OTHER_DETENTION_FACILITY_NAME(
        "otherDetentionFacilityName", new TypeReference<OtherDetentionFacilityName>(){}),

    CUSTODIAL_SENTENCE(
        "releaseDateProvided", new TypeReference<YesOrNo>(){}),

    DATE_CUSTODIAL_SENTENCE(
        "releaseDate", new TypeReference<CustodialSentenceDate>(){}),

    DATE_CUSTODIAL_SENTENCE_AO(
        "dateCustodialSentenceAo", new TypeReference<CustodialSentenceDate>(){}),

    BAIL_APPLICATION_NUMBER(
             "bailApplicationNumber", new TypeReference<String>(){}),

    HAS_PENDING_BAIL_APPLICATIONS(
             "hasPendingBailApplications", new TypeReference<BailApplicationStatus>(){}),

    DATE_ON_DECISION_LETTER(
        "dateOnDecisionLetter", new TypeReference<String>(){}),

    AGE_ASSESSMENT(
        "ageAssessment", new TypeReference<YesOrNo>(){}),

    AA_APPELLANT_DATE_OF_BIRTH(
        "aaAppellantDateOfBirth", new TypeReference<String>() {}),

    REMOVAL_ORDER_OPTIONS(
        "removalOrderOptions", new TypeReference<YesOrNo>(){}),

    REMOVAL_ORDER_DATE(
        "removalOrderDate", new TypeReference<String>(){}),

    APPELLANT_PIN_IN_POST(
        "appellantPinInPost", new TypeReference<PinInPostDetails>(){}),

    S94B_STATUS(
        "s94bStatus", new TypeReference<YesOrNo>(){}),

    APPELLANT_LEVEL_FLAGS("appellantLevelFlags", new TypeReference<StrategicCaseFlag>() {
    }),

    IS_ADMIN(
        "isAdmin", new TypeReference<YesOrNo>() {}),

    WITNESS_LEVEL_FLAGS(
        "witnessLevelFlags", new TypeReference<List<PartyFlagIdValue>>() {}),

    INTERPRETER_LEVEL_FLAGS(
            "interpreterLevelFlags", new TypeReference<List<PartyFlagIdValue>>() {}),

    CASE_LEVEL_FLAGS(
        "caseFlags", new TypeReference<StrategicCaseFlag>(){}),

    IS_HEARING_ROOM_NEEDED(
            "isHearingRoomNeeded", new TypeReference<YesOrNo>(){}),

    IS_HEARING_LOOP_NEEDED(
            "isHearingLoopNeeded", new TypeReference<YesOrNo>(){}),

    IN_CAMERA_COURT("inCameraCourt", new TypeReference<YesOrNo>(){}),

    MULTIMEDIA_EVIDENCE("multimediaEvidence", new TypeReference<YesOrNo>(){}),

    APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING(
        "appellantInterpreterSpokenLanguageBooking", new TypeReference<String>() {}),

    APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS(
        "appellantInterpreterSpokenLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>() {}),

    APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING(
        "appellantInterpreterSignLanguageBooking", new TypeReference<String>() {}),

    APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS(
        "appellantInterpreterSignLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1(
        "witnessInterpreterSpokenLanguageBooking1", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2(
        "witnessInterpreterSpokenLanguageBooking2", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3(
        "witnessInterpreterSpokenLanguageBooking3", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4(
        "witnessInterpreterSpokenLanguageBooking4", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_5(
        "witnessInterpreterSpokenLanguageBooking5", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_6(
        "witnessInterpreterSpokenLanguageBooking6", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_7(
        "witnessInterpreterSpokenLanguageBooking7", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_8(
        "witnessInterpreterSpokenLanguageBooking8", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_9(
        "witnessInterpreterSpokenLanguageBooking9", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_10(
        "witnessInterpreterSpokenLanguageBooking10", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1(
        "witnessInterpreterSpokenLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2(
        "witnessInterpreterSpokenLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3(
        "witnessInterpreterSpokenLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4(
        "witnessInterpreterSpokenLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5(
        "witnessInterpreterSpokenLanguageBookingStatus5", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6(
        "witnessInterpreterSpokenLanguageBookingStatus6", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7(
        "witnessInterpreterSpokenLanguageBookingStatus7", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8(
        "witnessInterpreterSpokenLanguageBookingStatus8", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9(
        "witnessInterpreterSpokenLanguageBookingStatus9", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10(
        "witnessInterpreterSpokenLanguageBookingStatus10", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1(
        "witnessInterpreterSignLanguageBooking1", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2(
        "witnessInterpreterSignLanguageBooking2", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3(
        "witnessInterpreterSignLanguageBooking3", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4(
        "witnessInterpreterSignLanguageBooking4", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_5(
        "witnessInterpreterSignLanguageBooking5", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_6(
        "witnessInterpreterSignLanguageBooking6", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_7(
        "witnessInterpreterSignLanguageBooking7", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_8(
        "witnessInterpreterSignLanguageBooking8", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_9(
        "witnessInterpreterSignLanguageBooking9", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_10(
        "witnessInterpreterSignLanguageBooking10", new TypeReference<String>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1(
        "witnessInterpreterSignLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2(
        "witnessInterpreterSignLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3(
        "witnessInterpreterSignLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4(
        "witnessInterpreterSignLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5(
        "witnessInterpreterSignLanguageBookingStatus5", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6(
        "witnessInterpreterSignLanguageBookingStatus6", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7(
        "witnessInterpreterSignLanguageBookingStatus7", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8(
        "witnessInterpreterSignLanguageBookingStatus8", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9(
        "witnessInterpreterSignLanguageBookingStatus9", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10(
        "witnessInterpreterSignLanguageBookingStatus10", new TypeReference<InterpreterBookingStatus>() {}),

    INTERPRETER_DETAILS(
        "interpreterDetails", new TypeReference<List<IdValue<InterpreterDetails>>>() {}),

    APPELLANT_PARTY_ID(
            "appellantPartyId", new TypeReference<String>() {}),

    LEGAL_REP_INDIVIDUAL_PARTY_ID(
            "legalRepIndividualPartyId", new TypeReference<String>() {}),

    LEGAL_REP_ORGANISATION_PARTY_ID(
            "legalRepOrganisationPartyId", new TypeReference<String>() {}),

    SPONSOR_PARTY_ID(
            "sponsorPartyId", new TypeReference<String>() {}),

    CHANGE_HEARINGS(
            "changeHearings", new TypeReference<DynamicList>(){}),

    CHANGE_HEARING_VENUE(
            "changeHearingVenue", new TypeReference<String>(){}),
    IS_INTEGRATED(
            "isIntegrated", new TypeReference<YesOrNo>(){}),

    MANUAL_CANCEL_HEARINGS_REQUIRED(
        "manualCanHearingRequired", new TypeReference<YesOrNo>(){}),

    MANUAL_UPDATE_HEARING_REQUIRED(
        "manualUpdHearingRequired", new TypeReference<YesOrNo>(){}),

    MANUAL_CREATE_HEARING_REQUIRED(
        "manualCreHearingRequired", new TypeReference<YesOrNo>(){}),

    UPDATE_HMC_REQUEST_SUCCESS(
            "updateHmcRequestSuccess", new TypeReference<YesOrNo>() {}),
    NEXT_HEARING_FORMAT(
        "nextHearingFormat", new TypeReference<DynamicList>(){}),

    HEARING_ADJOURNMENT_WHEN(
        "hearingAdjournmentWhen", new TypeReference<HearingAdjournmentDay>(){}),

    RELIST_CASE_IMMEDIATELY(
        "relistCaseImmediately", new TypeReference<YesOrNo>(){}),

    NEXT_HEARING_VENUE(
        "nextHearingVenue", new TypeReference<DynamicList>(){}),

    NEXT_HEARING_DURATION(
        "nextHearingDuration", new TypeReference<String>(){}),

    HEARING_ADJOURNMENT_DECISION_PARTY(
        "hearingAdjournmentDecisionParty", new TypeReference<String>(){}),

    HEARING_ADJOURNMENT_DECISION_PARTY_NAME(
        "hearingAdjournmentDecisionPartyName", new TypeReference<String>(){}),

    HEARING_ADJOURNMENT_REQUESTING_PARTY(
        "hearingAdjournmentRequestingParty", new TypeReference<String>(){}),

    ANY_ADDITIONAL_ADJOURNMENT_INFO(
        "anyAdditionalAdjournmentInfo", new TypeReference<YesOrNo>(){}),

    ADDITIONAL_ADJOURNMENT_INFO(
        "additionalAdjournmentInfo", new TypeReference<String>(){}),

    NEXT_HEARING_DATE(
        "nextHearingDate", new TypeReference<String>(){}),

    NEXT_HEARING_DATE_FIXED(
        "nextHearingDateFixed", new TypeReference<String>(){}),

    NEXT_HEARING_DATE_RANGE_EARLIEST(
        "nextHearingDateRangeEarliest", new TypeReference<String>(){}),

    NEXT_HEARING_DATE_RANGE_LATEST(
        "nextHearingDateRangeLatest", new TypeReference<String>(){}),

    SHOULD_RESERVE_OR_EXCLUDE_JUDGE(
        "shouldReserveOrExcludeJudge", new TypeReference<YesOrNo>(){}),

    RESERVE_OR_EXCLUDE_JUDGE(
        "reserveOrExcludeJudge", new TypeReference<String>(){}),

    ADJOURNMENT_DETAILS_HEARING(
        "adjournmentDetailsHearing", new TypeReference<DynamicList>(){}),

    CURRENT_ADJOURNMENT_DETAIL(
            "currentAdjournmentDetail", new TypeReference<AdjournmentDetail>(){}),

    PREVIOUS_ADJOURNMENT_DETAILS(
        "previousAdjournmentDetails", new TypeReference<List<IdValue<AdjournmentDetail>>>(){}),

    HEARING_REASON_TO_CANCEL(
        "hearingReasonToCancel", new TypeReference<DynamicList>(){}),

    HEARING_REASON_TO_UPDATE(
        "hearingReasonToUpdate", new TypeReference<DynamicList>(){}),

    IS_APPEAL_SUITABLE_TO_FLOAT(
            "isAppealSuitableToFloat", new TypeReference<YesOrNo>(){}),

    AUTO_HEARING_REQUEST_ENABLED("autoHearingRequestEnabled", new TypeReference<YesOrNo>(){}),

    HEARING_LOCATION("hearingLocation", new TypeReference<DynamicList>(){}),

    AUTO_REQUEST_HEARING("autoRequestHearing", new TypeReference<YesOrNo>(){}),

    AUTO_LIST_HEARING("autoListHearing", new TypeReference<YesOrNo>(){}),

    IS_PANEL_REQUIRED("isPanelRequired", new TypeReference<YesOrNo>(){}),

    CHANGE_HEARING_DATE_YES_NO("changeHearingDateYesNo", new TypeReference<String>(){}),

    CHANGE_HEARING_DATE_TYPE("changeHearingDateType", new TypeReference<String>(){}),

    CHANGE_HEARING_DATE_RANGE_EARLIEST("changeHearingDateRangeEarliest", new TypeReference<String>(){}),

    CHANGE_HEARING_DATE_RANGE_LATEST("changeHearingDateRangeLatest", new TypeReference<String>(){}),

    LISTING_AVAILABLE_FOR_ADA(
        "listingAvailableForAda", new TypeReference<YesOrNo>(){}),

    CALCULATED_HEARING_DATE(
        "calculatedHearingDate", new TypeReference<String>(){}),

    ORGANISATION_ON_DECISION_LETTER(
            "organisationOnDecisionLetter", new TypeReference<String>(){}),

    LOCAL_AUTHORITY(
            "localAuthority", new TypeReference<String>(){}),

    HSC_TRUST(
            "hscTrust", new TypeReference<String>(){}),

    LITIGATION_FRIEND(
            "litigationFriend", new TypeReference<YesOrNo>(){}),

    LITIGATION_FRIEND_GIVEN_NAME(
            "litigationFriendGivenName", new TypeReference<String>(){}),

    LITIGATION_FRIEND_FAMILY_NAME(
            "litigationFriendFamilyName", new TypeReference<String>(){}),

    LITIGATION_FRIEND_COMPANY(
            "litigationFriendCompany", new TypeReference<String>(){}),

    LITIGATION_FRIEND_CONTACT_PREFERENCE(
            "litigationFriendContactPreference", new TypeReference<ContactPreference>(){}),

    LITIGATION_FRIEND_EMAIL(
            "litigationFriendEmail", new TypeReference<String>(){}),

    LITIGATION_FRIEND_PHONE_NUMBER(
            "litigationFriendPhoneNumber", new TypeReference<String>(){}),

    DECISION_LETTER_REFERENCE_NUMBER(
            "decisionLetterReferenceNumber", new TypeReference<String>(){}),

    SUITABILITY_REVIEW_DECISION(
            "suitabilityReviewDecision", new TypeReference<AdaSuitabilityReviewDecision>(){}),

    HAS_TRANSFERRED_OUT_OF_ADA(
            "hasTransferredOutOfAda", new TypeReference<YesOrNo>(){}),

    TRANSFER_OUT_OF_ADA_DATE(
            "transferOutOfAdaDate", new TypeReference<String>(){}),

    ADA_SUFFIX(
        "adaSuffix", new TypeReference<String>(){}),

    HAS_ADDED_LEGAL_REP_DETAILS(
        "hasAddedLegalRepDetails", new TypeReference<YesOrNo>(){}),

    HEARING_REQ_SUFFIX(
        "hearingReqSuffix", new TypeReference<String>(){}),

    INTERNAL_APPELLANT_EMAIL(
            "internalAppellantEmail", new TypeReference<String>(){}),

    INTERNAL_APPELLANT_MOBILE_NUMBER(
            "internalAppellantMobileNumber", new TypeReference<String>(){}),

    DATE_MARKED_AS_ADA(
            "dateMarkedAsAda", new TypeReference<String>(){}),

    MARK_APPEAL_AS_ADA_EXPLANATION(
        "markAppealAsAdaExplanation", new TypeReference<String>(){}),

    REASON_APPEAL_MARKED_AS_ADA(
        "reasonAppealMarkedAsAda", new TypeReference<String>(){}),

    ADA_HEARING_REQUIREMENTS_SUBMITTED(
            "adaHearingRequirementsSubmitted", new TypeReference<YesOrNo>(){}),

    UT_APPEAL_REFERENCE_NUMBER(
            "utAppealReferenceNumber", new TypeReference<String>() {}),

    UT_INSTRUCTION_DATE(
            "utInstructionDate", new TypeReference<String>(){}),

    NOTICE_OF_DECISION_UT_TRANSFER_DOCUMENT(
        "noticeOfDecisionUtTransferDocument", new TypeReference<Document>(){}),

    APPEAL_READY_FOR_UT_TRANSFER(
            "appealReadyForUtTransfer", new TypeReference<YesOrNo>(){}),

    APPEAL_READY_FOR_UT_TRANSFER_OUTCOME(
            "appealReadyForUtTransferOutcome", new TypeReference<String>(){}),

    PREVIOUS_DETENTION_LOCATION(
            "previousDetentionLocation", new TypeReference<String>() {}),

    SUITABILITY_HEARING_TYPE_YES_OR_NO(
        "suitabilityHearingTypeYesOrNo", new TypeReference<YesOrNo>() {}),

    SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1(
        "suitabilityAppellantAttendanceYesOrNo1", new TypeReference<YesOrNo>() {}),

    SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2(
        "suitabilityAppellantAttendanceYesOrNo2", new TypeReference<YesOrNo>() {}),

    SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO(
        "suitabilityInterpreterServicesYesOrNo", new TypeReference<YesOrNo>() {}),

    SUITABILITY_INTERPRETER_SERVICES_LANGUAGE(
        "suitabilityInterpreterServicesLanguage", new TypeReference<String>() {}),

    SOURCE_OF_APPEAL(
        "sourceOfAppeal", new TypeReference<SourceOfAppeal>(){}),

    APPLIED_COSTS_TYPES(
            "appliedCostsTypes", new TypeReference<DynamicList>(){}),

    ARGUMENTS_AND_EVIDENCE_DETAILS(
        "argumentsAndEvidenceDetails", new TypeReference<String>(){}),

    ARGUMENTS_AND_EVIDENCE_DOCUMENTS(
        "argumentsAndEvidenceDocuments", new TypeReference<List<IdValue<Document>>>(){}),

    SCHEDULE_OF_COSTS_DOCUMENTS(
        "scheduleOfCostsDocuments", new TypeReference<List<IdValue<Document>>>(){}),

    APPLY_FOR_COSTS_HEARING_TYPE(
        "applyForCostsHearingType", new TypeReference<YesOrNo>(){}),

    APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION(
        "applyForCostsHearingTypeExplanation", new TypeReference<String>(){}),

    APPLIES_FOR_COSTS(
        "appliesForCosts", new TypeReference<List<IdValue<ApplyForCosts>>>(){}),

    APPLY_FOR_COSTS_DECISION(
            "applyForCostsDecision", new TypeReference<CostsDecision>(){}),

    APPLY_FOR_COSTS_APPLICANT_TYPE(
            "applyForCostsApplicantType", new TypeReference<String>(){}),

    APPLY_FOR_COSTS_CREATION_DATE(
            "applyForCostsCreationDate", new TypeReference<String>(){}),

    IS_APPLIED_FOR_COSTS(
            "isAppliedForCosts", new TypeReference<String>(){}),

    UPPER_TRIBUNAL_REFERENCE_NUMBER(
        "upperTribunalReferenceNumber", new TypeReference<String>() {}),

    IS_EJP(
        "isEjp", new TypeReference<YesOrNo>() {}),

    IS_NOTIFICATION_TURNED_OFF(
            "isNotificationTurnedOff", new TypeReference<YesOrNo>() {}),

    UT_TRANSFER_DOC(
            "utTransferDoc", new TypeReference<List<IdValue<Document>>>(){}),

    UPLOAD_EJP_APPEAL_FORM_DOCS(
            "uploadEjpAppealFormDocs", new TypeReference<List<IdValue<Document>>>(){}),

    IS_LEGALLY_REPRESENTED_EJP(
        "isLegallyRepresentedEjp", new TypeReference<YesOrNo>() {}),

    CONTACT_PREFERENCE_UNREP(
        "contactPreferenceUnrep", new TypeReference<List<ContactPreferenceUnrep>>(){}),

    EMAIL_UNREP(
        "emailUnrep", new TypeReference<String>(){}),

    MOBILE_NUMBER_UNREP(
        "mobileNumberUnrep", new TypeReference<String>(){}),

    IS_APPLY_FOR_COSTS_OOT(
            "isApplyForCostsOot", new TypeReference<YesOrNo>() {}),

    SEND_DECISIONS_AND_REASONS_DATE(
        "sendDecisionsAndReasonsDate", new TypeReference<String>(){}),

    APPLY_FOR_COSTS_OOT_EXPLANATION(
            "applyForCostsOotExplanation", new TypeReference<String>(){}),

    OOT_UPLOAD_EVIDENCE_DOCUMENTS(
            "ootUploadEvidenceDocuments", new TypeReference<List<IdValue<Document>>>(){}),

    RESPOND_TO_COSTS_LIST(
        "respondToCostsList", new TypeReference<DynamicList>(){}),

    RESPONSE_TO_APPLICATION_TEXT_AREA(
        "responseToApplicationTextArea", new TypeReference<String>(){}),

    RESPONSE_TO_APPLICATION_EVIDENCE(
        "responseToApplicationEvidence", new TypeReference<List<IdValue<Document>>>(){}),

    TYPE_OF_HEARING_OPTION(
        "typeOfHearingOption", new TypeReference<YesOrNo>(){}),

    TYPE_OF_HEARING_EXPLANATION(
        "typeOfHearingExplanation", new TypeReference<String>(){}),

    ADD_EVIDENCE_FOR_COSTS_LIST(
        "addEvidenceForCostsList", new TypeReference<DynamicList>(){}),

    ADDITIONAL_EVIDENCE_FOR_COSTS(
        "additionalEvidenceForCosts", new TypeReference<List<IdValue<Document>>>(){}),

    DECIDE_COSTS_APPLICATION_LIST(
        "decideCostsApplicationList", new TypeReference<DynamicList>(){}),

    COSTS_DECISION_TYPE(
        "costsDecisionType", new TypeReference<CostsDecisionType>(){}),

    COSTS_ORAL_HEARING_DATE(
        "costsOralHearingDate", new TypeReference<String>(){}),

    UPLOAD_COSTS_ORDER(
        "uploadCostsOrder", new TypeReference<List<IdValue<Document>>>(){}),

    JUDGE_APPLIED_COSTS_TYPES(
        "judgeAppliedCostsTypes", new TypeReference<DynamicList>(){}),

    RESPONDENT_TO_COSTS_ORDER(
        "respondentToCostsOrder", new TypeReference<String>(){}),

    TRIBUNAL_CONSIDERING_REASON(
    "tribunalConsideringReason", new TypeReference<String>(){}),

    JUDGE_EVIDENCE_FOR_COSTS_ORDER(
    "judgeEvidenceForCostsOrder", new TypeReference<List<IdValue<Document>>>(){}),

    LEGAL_REP_COMPANY_EJP(
        "legalRepCompanyEjp", new TypeReference<String>(){}),

    LEGAL_REP_GIVEN_NAME_EJP(
        "legalRepGivenNameEjp", new TypeReference<String>(){}),

    LEGAL_REP_FAMILY_NAME_EJP(
        "legalRepFamilyNameEjp", new TypeReference<String>(){}),

    LEGAL_REP_EMAIL_EJP(
        "legalRepEmailEjp", new TypeReference<String>(){}),

    LEGAL_REP_REFERENCE_EJP(
        "legalRepReferenceEjp", new TypeReference<String>(){}),

    FIRST_TIER_TRIBUNAL_TRANSFER_DATE(
        "firstTierTribunalTransferDate", new TypeReference<String>(){}),

    STATE_OF_THE_APPEAL(
        "stateOfTheAppeal", new TypeReference<String>(){}),

    LEGAL_PRACTICE_ADDRESS_EJP(
        "legalPracticeAddressEjp", new TypeReference<AddressUk>(){}),

    IS_OUT_OF_COUNTRY_ENABLED(
        "isOutOfCountryEnabled", new TypeReference<YesOrNo>() {}),

    FTPA_APPELLANT_DECISION_OBJECTIONS(
            "ftpaAppellantDecisionObjections", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_OBJECTIONS(
            "ftpaRespondentDecisionObjections", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_LST_INS(
            "ftpaAppellantDecisionLstIns", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_LST_INS(
            "ftpaRespondentDecisionLstIns", new TypeReference<String>(){}),

    FTPA_APPELLANT_RJ_DECISION_NOTES_POINTS(
            "ftpaAppellantRjDecisionNotesPoints", new TypeReference<FtpaDecisionCheckValues<String>>(){}),

    FTPA_RESPONDENT_RJ_DECISION_NOTES_POINTS(
            "ftpaRespondentRjDecisionNotesPoints", new TypeReference<FtpaDecisionCheckValues<String>>(){}),

    FTPA_APPELLANT_RJ_DECISION_NOTES_DESCRIPTION(
            "ftpaAppellantRjDecisionNotesDescription", new TypeReference<String>(){}),

    FTPA_RESPONDENT_RJ_DECISION_NOTES_DESCRIPTION(
            "ftpaRespondentRjDecisionNotesDescription", new TypeReference<String>(){}),

    FTPA_APPELLANT_REASON_REHEARING(
            "ftpaAppellantReasonRehearing", new TypeReference<String>(){}),

    FTPA_RESPONDENT_REASON_REHEARING(
            "ftpaRespondentReasonRehearing", new TypeReference<String>(){}),

    FTPA_LIST(
            "ftpaList", new TypeReference<List<IdValue<FtpaApplications>>>(){}),

    IS_FTPA_LIST_VISIBLE(
            "isFtpaListVisible", new TypeReference<YesOrNo>(){}),

    IS_DLRM_SET_ASIDE_ENABLED(
            "isDlrmSetAsideEnabled", new TypeReference<YesOrNo>(){}),

    IS_DLRM_FEE_REMISSION_ENABLED(
        "isDlrmFeeRemissionEnabled", new TypeReference<YesOrNo>(){}),

    FTPA_APPELLANT_R35_LISTING_ADDITIONAL_INS(
            "ftpaAppellantR35ListingAdditionalIns", new TypeReference<String>(){}),

    FTPA_RESPONDENT_R35_LISTING_ADDITIONAL_INS(
            "ftpaRespondentR35ListingAdditionalIns", new TypeReference<String>(){}),

    FTPA_R35_APPELLANT_DOCUMENT(
            "ftpaR35AppellantDocument", new TypeReference<Document>(){}),

    FTPA_R35_RESPONDENT_DOCUMENT(
            "ftpaR35RespondentDocument", new TypeReference<Document>(){}),

    FTPA_APPELLANT_R35_DECISION_OBJECTIONS(
            "ftpaAppellantR35DecisionObjections", new TypeReference<String>(){}),

    FTPA_RESPONDENT_R35_DECISION_OBJECTIONS(
            "ftpaRespondentR35DecisionObjections", new TypeReference<String>(){}),

    FTPA_APPELLANT_R35_NOTICE_DOCUMENT(
            "ftpaAppellantR35NoticeDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    FTPA_RESPONDENT_R35_NOTICE_DOCUMENT(
            "ftpaRespondentR35NoticeDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    RULE_32_NOTICE_DOCUMENT(
            "rule32NoticeDocument", new TypeReference<Document>(){}),

    ALL_SET_ASIDE_DOCS(
            "allSetAsideDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    TYPES_OF_UPDATE_TRIBUNAL_DECISION(
        "typesOfUpdateTribunalDecision", new TypeReference<DynamicList>(){}),

    DECISION_AND_REASON_DOCS_UPLOAD(
        "decisionAndReasonDocsUpload", new TypeReference<Document>(){}),

    UPDATE_TRIBUNAL_DECISION_LIST(
            "updateTribunalDecisionList", new TypeReference<UpdateTribunalRules>(){}),

    UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK(
            "updateTribunalDecisionAndReasonsFinalCheck", new TypeReference<YesOrNo>(){}),

    UPDATED_APPEAL_DECISION(
        "updatedAppealDecision", new TypeReference<String>(){}),

    UPLOAD_REMITTAL_DECISION_DOC(
        "uploadRemittalDecisionDoc", new TypeReference<Document>(){}),

    UPLOAD_OTHER_REMITTAL_DOCS(
        "uploadOtherRemittalDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    REMITTAL_DOCUMENTS(
        "remittalDocuments", new TypeReference<List<IdValue<RemittalDocument>>>(){}),

    COURT_REFERENCE_NUMBER(
        "courtReferenceNumber", new TypeReference<String>(){}),

    REMISSION_OPTION(
        "remissionOption", new TypeReference<RemissionOption>(){}),

    HELP_WITH_FEES_OPTION(
        "helpWithFeesOption", new TypeReference<HelpWithFeesOption>(){}),

    CORRECTED_DECISION_AND_REASONS(
            "correctedDecisionAndReasons", new TypeReference<List<IdValue<DecisionAndReasons>>>(){}),

    SUMMARISE_TRIBUNAL_DECISION_AND_REASONS_DOCUMENT(
            "summariseTribunalDecisionAndReasonsDocument", new TypeReference<String>(){}),

    APPEAL_DECISION_LABEL(
        "appealDecisionLabel", new TypeReference<String>(){}),

    UPDATE_TRIBUNAL_DECISION_DATE(
        "updateTribunalDecisionDate", new TypeReference<String>(){}),

    UPDATE_TRIBUNAL_DECISION_DATE_RULE_32(
            "updateTribunalDecisionDateRule32", new TypeReference<String>(){}),

    REASON_REHEARING_RULE_32(
            "reasonRehearingRule32", new TypeReference<String>(){}),

    IS_DLRM_FEE_REFUND_ENABLED(
            "isDlrmFeeRefundEnabled", new TypeReference<YesOrNo>(){}),

    IS_LATE_REMISSION_REQUEST(
            "isLateRemissionRequest", new TypeReference<YesOrNo>(){}),

    ASYLUM_SUPPORT_REF_NUMBER(
        "asylumSupportRefNumber", new TypeReference<String>(){}),

    HELP_WITH_FEES_REF_NUMBER(
        "helpWithFeesRefNumber", new TypeReference<String>(){}),

    LOCAL_AUTHORITY_LETTERS(
        "localAuthorityLetters", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    LATE_REMISSION_OPTION(
        "lateRemissionOption", new TypeReference<RemissionOption>(){}),

    LATE_ASYLUM_SUPPORT_REF_NUMBER(
        "lateAsylumSupportRefNumber", new TypeReference<String>(){}),

    LATE_HELP_WITH_FEES_OPTION(
        "lateHelpWithFeesOption", new TypeReference<HelpWithFeesOption>(){}),

    LATE_HELP_WITH_FEES_REF_NUMBER(
        "lateHelpWithFeesRefNumber", new TypeReference<String>(){}),

    LATE_LOCAL_AUTHORITY_LETTERS(
        "lateLocalAuthorityLetters", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    SOURCE_OF_REMITTAL(
        "sourceOfRemittal", new TypeReference<SourceOfRemittal>(){}),

    JUDGES_NAMES_TO_EXCLUDE(
        "judgesNamesToExclude", new TypeReference<String>(){}),

    REMITTED_ADDITIONAL_INSTRUCTIONS(
        "remittedAdditionalInstructions", new TypeReference<String>(){}),

    APPEAL_REMITTED_DATE(
        "appealRemittedDate", new TypeReference<String>() {}),

    REHEARING_REASON(
        "rehearingReason", new TypeReference<String>() {}),

    REHEARD_HEARING_DOCUMENTS_COLLECTION("reheardHearingDocumentsCollection", new TypeReference<List<IdValue<ReheardHearingDocuments>>>(){}),

    HEARING_CENTRE_DYNAMIC_LIST("hearingCentreDynamicList", new TypeReference<DynamicList>(){}),

    IS_DECISION_WITHOUT_HEARING("isDecisionWithoutHearing", new TypeReference<YesOrNo>(){}),

    IS_ADDITIONAL_INSTRUCTION_ALLOWED("isAdditionalInstructionAllowed", new TypeReference<YesOrNo>(){}),

    ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE("additionalInstructionsTribunalResponse", new TypeReference<String>(){}),

    ADDITIONAL_INSTRUCTIONS("additionalInstructions", new TypeReference<YesOrNo>(){}),

    ADDITIONAL_INSTRUCTIONS_DESCRIPTION("additionalInstructionsDescription", new TypeReference<String>(){}),

    CASE_MANAGEMENT_LOCATION_REF_DATA("caseManagementLocationRefData", new TypeReference<CaseManagementLocationRefData>(){}),

    IS_VIRTUAL_HEARING("isVirtualHearing", new TypeReference<YesOrNo>(){}),

    NEXT_HEARING_DETAILS("nextHearingDetails", new TypeReference<NextHearingDetails>(){}),

    OOC_APPEAL_ADMIN_J(
    "oocAppealAdminJ", new TypeReference<OutOfCountryCircumstances>(){}),

    IS_DECISION_RULE31_CHANGED(
            "isDecisionRule31Changed", new TypeReference<YesOrNo>(){}),
    APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS(
        "appealNotSubmittedReasonDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    // Used to store generated letter notification docs which will be stitched together
    LETTER_NOTIFICATION_DOCUMENTS(
        "letterNotificationDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    APPELLANTS_REPRESENTATION(
        "appellantsRepresentation", new TypeReference<YesOrNo>(){}),

    APPEAL_WAS_NOT_SUBMITTED_REASON(
        "appealWasNotSubmittedReason", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_PAPER_J(
        "legalRepCompanyPaperJ", new TypeReference<String>(){}),

    LEGAL_REP_GIVEN_NAME(
        "legalRepGivenName", new TypeReference<String>(){}),

    LEGAL_REP_FAMILY_NAME_PAPER_J(
        "legalRepFamilyNamePaperJ", new TypeReference<String>(){}),

    LEGAL_REP_EMAIL(
        "legalRepEmail", new TypeReference<String>(){}),

    LEGAL_REP_REF_NUMBER_PAPER_J(
        "legalRepRefNumberPaperJ", new TypeReference<String>(){}),

    LEGAL_REP_ADDRESS_U_K(
        "legalRepAddressUK", new TypeReference<AddressUk>(){}),

    OOC_ADDRESS_LINE_1(
        "oocAddressLine1", new TypeReference<String>(){}),

    OOC_ADDRESS_LINE_2(
        "oocAddressLine2", new TypeReference<String>(){}),

    SELECTED_HEARING_CENTRE_REF_DATA("selectedHearingCentreRefData", new TypeReference<String>(){}),

    IS_REMOTE_HEARING("isRemoteHearing", new TypeReference<YesOrNo>(){}),

    NOTIFICATIONS("notifications", new TypeReference<List<IdValue<StoredNotification>>>(){}),
    NOTIFICATIONS_SENT("notificationsSent", new TypeReference<List<IdValue<String>>>(){}),

    REQUEST_FEE_REMISSION_DATE(
            "requestFeeRemissionDate", new TypeReference<String>(){}),

    OOC_ADDRESS_LINE_3(
        "oocAddressLine3", new TypeReference<String>(){}),

    FEE_UPDATE_TRIBUNAL_ACTION(
           "feeUpdateTribunalAction", new TypeReference<FeeTribunalAction>(){}),

    AUTOMATIC_REMISSION_REMINDER_LEGAL_REP(
            "automaticRemissionReminderLegalRep", new TypeReference<String>() {}),

    REFUND_CONFIRMATION_APPLIED(
            "refundConfirmationApplied", new TypeReference<YesOrNo>() {}),

    OOC_ADDRESS_LINE_4(
        "oocAddressLine4", new TypeReference<String>(){}),

    OOC_COUNTRY_LINE(
        "oocCountryLine", new TypeReference<String>(){}),

    OOC_LR_COUNTRY_GOV_UK_ADMIN_J(
        "oocLrCountryGovUkAdminJ", new TypeReference<NationalityFieldValue>(){}),

    LEGAL_REP_HAS_ADDRESS(
        "legalRepHasAddress", new TypeReference<YesOrNo>(){}),

    IS_ARIA_MIGRATED(
        "isAriaMigrated", new TypeReference<YesOrNo>(){}),

    // Temporary value to set the case state as 'Migrated'
    IS_ARIA_MIGRATED_TEMPORARY(
            "isAriaMigratedTemporary", new TypeReference<YesOrNo>(){}),

    ARIA_DESIRED_STATE(
        "ariaDesiredState", new TypeReference<State>(){}),

    ARIA_DESIRED_STATE_SELECTED_VALUE(
        "ariaDesiredStateSelectedValue", new TypeReference<String>(){}),

    DESIRED_STATE_CORRECT(
        "desiredStateCorrect", new TypeReference<YesOrNo>(){}),

    MIGRATION_MAIN_TEXT(
        "migrationMainText", new TypeReference<String>(){}),

    MIGRATION_MAIN_TEXT_VISIBLE(
        "migrationMainTextVisible", new TypeReference<String>(){}),

    MIGRATION_HMC_SECOND_PART_VISIBLE(
        "migrationHmcSecondPartVisible", new TypeReference<String>(){}),

    ADD_CASE_NOTES_MIGRATION(
        "addCaseNotesMigration", new TypeReference<List<IdValue<CaseNoteMigration>>>(){}),

    IS_ARIA_MIGRATED_FILTER(
            "isAriaMigratedFilter", new TypeReference<YesOrNo>(){}),

    ARIA_MIGRATION_TASK_DUE_DAYS(
            "ariaMigrationTaskDueDays", new TypeReference<String>(){}),

    DECISION_TYPE_CHANGED_WITH_REFUND_FLAG(
            "decisionTypeChangedWithRefundFlag", new TypeReference<YesOrNo>(){}),

    PREVIOUS_DECISION_HEARING_FEE_OPTION(
            "previousDecisionHearingFeeOption", new TypeReference<String>(){}),

    UPDATED_DECISION_HEARING_FEE_OPTION(
            "updatedDecisionHearingFeeOption", new TypeReference<String>(){}),

    CURRENT_HEARING_ID(
            "currentHearingId", new TypeReference<String>() {}),

    HEARING_ID_LIST(
            "hearingIdList", new TypeReference<List<IdValue<String>>>(){}),

    CASE_ID_LIST(
        "caseIdList", new TypeReference<String>() {}),

    HEARING_DECISION_LIST(
            "hearingDecisionList", new TypeReference<List<IdValue<HearingDecision>>>(){}),

    REMISSION_REQUESTED_BY(
        "remissionRequestedBy", new TypeReference<UserRoleLabel>(){}),
  
    GENERATE_LIST_CMR_TASK_REQUESTED(
            "generateListCmrTaskRequested", new TypeReference<YesOrNo>(){}),

    DETENTION_BUILDING(
          "detentionBuilding", new TypeReference<String>(){}),

    DETENTION_ADDRESS_LINES(
          "detentionAddressLines", new TypeReference<String>(){}),

    DETENTION_POSTCODE(
          "detentionPostcode", new TypeReference<String>(){}),

    DETENTION_REMOVAL_DATE("detentionRemovalDate",
            new TypeReference<String>(){}),

    DETENTION_REMOVAL_REASON("detentionRemovalReason",
            new TypeReference<String>(){}),

    REASON_APPELLANT_WAS_DETAINED("addReasonAppellantWasDetained",
            new TypeReference<String>(){}),

    APPELLANT_DETAINED_DATE("appellantDetainedDate",
            new TypeReference<String>(){}),

    STATUTORY_TIMEFRAME_24_WEEKS(
      "statutoryTimeframe24Weeks", new TypeReference<StatutoryTimeframe24Weeks>(){}),

    STATUTORY_TIMEFRAME_24_WEEKS_REASON(
        "statutoryTimeframe24WeeksReason", new TypeReference<String>(){}),
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
