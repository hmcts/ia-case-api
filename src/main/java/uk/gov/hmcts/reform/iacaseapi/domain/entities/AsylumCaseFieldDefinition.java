package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public enum AsylumCaseFieldDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
            "homeOfficeReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_DECISION_DATE(
            "homeOfficeDecisionDate", new TypeReference<String>(){}),

    APPELLANT_GIVEN_NAMES(
            "appellantGivenNames", new TypeReference<String>(){}),

    APPELLANT_FAMILY_NAME(
            "appellantFamilyName", new TypeReference<String>(){}),

    APPELLANT_HAS_FIXED_ADDRESS(
            "appellantHasFixedAddress", new TypeReference<YesOrNo>(){}),

    APPELLANT_ADDRESS(
            "appellantAddress", new TypeReference<AddressUk>(){}),

    APPEAL_TYPE(
            "appealType", new TypeReference<AppealType>(){}),

    APPEAL_GROUNDS_PROTECTION(
            "appealGroundsProtection", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_HUMAN_RIGHTS(
            "appealGroundsHumanRights", new TypeReference<CheckValues<String>>(){}),

    APPEAL_GROUNDS_REVOCATION(
            "appealGroundsRevocation", new TypeReference<CheckValues<String>>(){}),

    HAS_OTHER_APPEALS(
            "hasOtherAppeals", new TypeReference<String>(){}),

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

    EDITABLE_DIRECTIONS(
            "editableDirections", new TypeReference<List<IdValue<EditableDirection>>>(){}),

    ADDITIONAL_EVIDENCE_DOCUMENTS(
            "additionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    HEARING_DOCUMENTS(
            "hearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    LEGAL_REPRESENTATIVE_DOCUMENTS(
            "legalRepresentativeDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RESPONDENT_DOCUMENTS(
            "respondentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RESPONDENT_EVIDENCE(
            "respondentEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CASE_ARGUMENT_DOCUMENT(
            "caseArgumentDocument", new TypeReference<Document>(){}),

    CASE_ARGUMENT_DESCRIPTION(
            "caseArgumentDescription", new TypeReference<String>(){}),

    CASE_ARGUMENT_EVIDENCE(
            "caseArgumentEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    APPEAL_RESPONSE_DOCUMENT(
            "appealResponseDocument", new TypeReference<Document>(){}),

    APPEAL_RESPONSE_DESCRIPTION(
            "appealResponseDescription", new TypeReference<String>(){}),

    APPEAL_RESPONSE_EVIDENCE(
            "appealResponseEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    ADDITIONAL_EVIDENCE(
            "additionalEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    LIST_CASE_HEARING_CENTRE(
            "listCaseHearingCentre", new TypeReference<HearingCentre>(){}),

    LIST_CASE_HEARING_DATE(
            "listCaseHearingDate", new TypeReference<String>(){}),

    CASE_SUMMARY_DOCUMENT(
            "caseSummaryDocument", new TypeReference<Document>(){}),

    CASE_SUMMARY_DESCRIPTION(
            "caseSummaryDescription", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_NAME(
            "legalRepresentativeName", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
            "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE(
            "changeDirectionDueDateActionAvailable", new TypeReference<YesOrNo>(){}),

    SEND_DIRECTION_ACTION_AVAILABLE(
            "sendDirectionActionAvailable", new TypeReference<YesOrNo>(){}),

    UPLOAD_ADDITIONAL_EVIDENCE_ACTION_AVAILABLE(
            "uploadAdditionalEvidenceActionAvailable", new TypeReference<YesOrNo>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER(
            "currentCaseStateVisibleToCaseOfficer", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE(
            "currentCaseStateVisibleToLegalRepresentative", new TypeReference<State>(){}),

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


    UPLOAD_ADDENDUM_EVIDENCE_ACTION_AVAILABLE(
        "uploadAddendumEvidenceActionAvailable", new TypeReference<YesOrNo>(){}),

    ADDENDUM_EVIDENCE(
        "addendumEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    ADDENDUM_EVIDENCE_DOCUMENTS(
        "addendumEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    IS_APPELLANT_RESPONDENT(
        "isAppellantRespondent", new TypeReference<String>(){}),
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

