package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class CaseDataMap extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> Optional<T> read(AsylumExtractor extractor) {

        Object o = this.get(extractor.value());

        if (o == null) {
            return Optional.empty();
        }

        T value = objectMapper.convertValue(o, extractor.getTypeReference());

        return Optional.of(value);
    }

    public Optional<String> getHomeOfficeReferenceNumber() {
        return read(HOME_OFFICE_REFERENCE_NUMBER);
    }

    public Optional<String> getHomeOfficeDecisionDate() {
       return read(HOME_OFFICE_DECISION_DATE);
    }

    public Optional<String> getAppellantGivenNames() {
        return read(APPELLANT_GIVEN_NAMES);
    }

    public Optional<String> getAppellantFamilyName() {
        return read(APPELLANT_FAMILY_NAME);
    }

    public Optional<YesOrNo> getAppellantHasFixedAddress() {
        return read(APPELLANT_HAS_FIXED_ADDRESS);
    }

    public Optional<AddressUk> getAppellantAddress() {
        return read(APPELLANT_ADDRESS);
    }

    public Optional<AppealType> getAppealType() {
        return read(APPEAL_TYPE);
    }

    public Optional<CheckValues<String>> getAppealGroundsProtection() {
        return read(APPEAL_GROUNDS_PROTECTION);
    }

    public Optional<CheckValues<String>> getAppealGroundsHumanRights() {
        return read(APPEAL_GROUNDS_HUMAN_RIGHTS);
    }

    public Optional<CheckValues<String>> getAppealGroundsRevocation() {
        return read(APPEAL_GROUNDS_REVOCATION);
    }

    public Optional<String> getAppealReferenceNumber() {
        return read(APPEAL_REFERENCE_NUMBER);
    }

    public Optional<HearingCentre> getHearingCentre() {
        return read(HEARING_CENTRE);
    }

    public void setHomeOfficeReferenceNumber(String homeOfficeReferenceNumber) {
        this.put(HOME_OFFICE_REFERENCE_NUMBER.value(), homeOfficeReferenceNumber);
    }

    public void setAppealReferenceNumber(String appealReferenceNumber) {
        this.put(APPEAL_REFERENCE_NUMBER.value(), appealReferenceNumber);
    }

    public void setAppellantNameForDisplay(String appellantNameForDisplay) {
        this.put(APPELLANT_NAME_FOR_DISPLAY.value(), appellantNameForDisplay);
    }

    public void setAppealGroundsForDisplay(List<String> appealGroundsForDisplay) {
        this.put(APPEAL_GROUNDS_FOR_DISPLAY.value(), appealGroundsForDisplay);
    }

    public void setHearingCentre(HearingCentre hearingCentre) {
        this.put(HEARING_CENTRE.value(), hearingCentre);
    }

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    public Optional<String> getSendDirectionExplanation() {
        return read(SEND_DIRECTION_EXPLANATION);
    }

    public Optional<Parties> getSendDirectionParties() {
        return read(SEND_DIRECTION_PARTIES);
    }

    public Optional<String> getSendDirectionDateDue() {
        return read(SEND_DIRECTION_DATE_DUE);
    }

    public Optional<List<IdValue<Direction>>> getDirections() {
        return read(DIRECTIONS);
    }

    public void clearSendDirectionExplanation() {
        this.put(SEND_DIRECTION_EXPLANATION.value(), null);
    }

    public void clearSendDirectionParties() {
        this.put(SEND_DIRECTION_PARTIES.value(), null);
    }
    public void clearSendDirectionDateDue() {
        this.put(SEND_DIRECTION_DATE_DUE.value(), null);
    }

    public void setSendDirectionExplanation(String sendDirectionExplanation) {
        this.put(SEND_DIRECTION_EXPLANATION.value(), sendDirectionExplanation);
    }

    public void setSendDirectionParties(Parties sendDirectionParties) {
        this.put(SEND_DIRECTION_PARTIES.value(), sendDirectionParties);
    }

    public void setSendDirectionDateDue(String sendDirectionDateDue) {
        this.put(SEND_DIRECTION_DATE_DUE.value(), sendDirectionDateDue);
    }

    public void setDirections(List<IdValue<Direction>> directions) {
        this.put(DIRECTIONS.value(), directions);
    }

    // -----------------------------------------------------------------------------
    // change direction due date ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<EditableDirection>>> getEditableDirections() {
        return read(EDITABLE_DIRECTIONS);
    }

    public void clearEditableDirections() {
        this.put(EDITABLE_DIRECTIONS.value(), null);
    }

    public void setEditableDirections(List<IdValue<EditableDirection>> editableDirections) {
        this.put(EDITABLE_DIRECTIONS.value(), editableDirections);
    }

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithMetadata>>> getAdditionalEvidenceDocuments() {
        return read(ADDITIONAL_EVIDENCE_DOCUMENTS);
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getHearingDocuments() {
        return read(HEARING_DOCUMENTS);
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getLegalRepresentativeDocuments() {
        return read(LEGAL_REPRESENTATIVE_DOCUMENTS);
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getRespondentDocuments() {
        return read(RESPONDENT_DOCUMENTS);
    }

    public void setAdditionalEvidenceDocuments(List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments) {
        this.put(ADDITIONAL_EVIDENCE_DOCUMENTS.value(), additionalEvidenceDocuments);
    }

    public void setHearingDocuments(List<IdValue<DocumentWithMetadata>> hearingDocuments) {
        this.put(HEARING_DOCUMENTS.value(), hearingDocuments);
    }

    public void setLegalRepresentativeDocuments(List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments) {
        this.put(LEGAL_REPRESENTATIVE_DOCUMENTS.value(), legalRepresentativeDocuments);
    }

    public void setRespondentDocuments(List<IdValue<DocumentWithMetadata>> respondentDocuments) {
        this.put(RESPONDENT_DOCUMENTS.value(), respondentDocuments);
    }

    // -----------------------------------------------------------------------------
    // upload respondent evidence ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithDescription>>> getRespondentEvidence() {
        return read(RESPONDENT_EVIDENCE);
    }

    public void clearRespondentEvidence() {
        this.put(RESPONDENT_EVIDENCE.value(), null);
    }

    // -----------------------------------------------------------------------------
    // case argument ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getCaseArgumentDocument() {
        return read(CASE_ARGUMENT_DOCUMENT);
    }

    public Optional<String> getCaseArgumentDescription() {
        return read(CASE_ARGUMENT_DESCRIPTION);
    }

    public Optional<List<IdValue<DocumentWithDescription>>> getCaseArgumentEvidence() {
        return read(CASE_ARGUMENT_EVIDENCE);
    }

    // -----------------------------------------------------------------------------
    // appeal response ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getAppealResponseDocument() {
        return read(APPEAL_RESPONSE_DOCUMENT);
    }

    public Optional<String> getAppealResponseDescription() {
        return read(APPEAL_RESPONSE_DESCRIPTION);
    }

    public Optional<List<IdValue<DocumentWithDescription>>> getAppealResponseEvidence() {
        return read(APPEAL_RESPONSE_EVIDENCE);
    }

    // -----------------------------------------------------------------------------
    // upload additional evidence ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithDescription>>> getAdditionalEvidence() {
        return read(ADDITIONAL_EVIDENCE);
    }

    public void clearAdditionalEvidence() {
        this.put(ADDITIONAL_EVIDENCE.value(), null);
    }

    // -----------------------------------------------------------------------------
    // list case ...
    // -----------------------------------------------------------------------------


    public void setListCaseHearingCentre(HearingCentre listCaseHearingCentre) {
        this.put(LIST_CASE_HEARING_CENTRE.value(), Optional.empty());
    }

    // -----------------------------------------------------------------------------
    // create case summary ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getCaseSummaryDocument() {
        return read(CASE_SUMMARY_DOCUMENT);
    }

    public Optional<String> getCaseSummaryDescription() {
        return read(CASE_SUMMARY_DESCRIPTION);
    }

    // -----------------------------------------------------------------------------
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    public Optional<String> getLegalRepresentativeName() {
        return read(LEGAL_REPRESENTATIVE_NAME);
    }

    public Optional<String> getLegalRepresentativeEmailAddress() {
        return read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
    }

    public Optional<YesOrNo> getUploadAdditionalEvidenceActionAvailable() {
        return read(UPLOAD_ADDITIONAL_EVIDENCE_ACTION_AVAILABLE);
    }

    public Optional<YesOrNo> getAppealResponseAvailable() {
        return read(APPEAL_RESPONSE_AVAILABLE);
    }

    public Optional<YesOrNo> getSubmissionOutOfTime() {
        return read(SUBMISSION_OUT_OF_TIME);
    }

    public void setLegalRepresentativeName(String legalRepresentativeName) {
        this.put(LEGAL_REPRESENTATIVE_NAME.value(), legalRepresentativeName);
    }

    public void setLegalRepresentativeEmailAddress(String legalRepresentativeEmailAddress) {
        this.put(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS.value(), legalRepresentativeEmailAddress);
    }

    public void setChangeDirectionDueDateActionAvailable(YesOrNo changeDirectionDueDateActionAvailable) {
        this.put(CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE.value(), changeDirectionDueDateActionAvailable);
    }

    public void setSendDirectionActionAvailable(YesOrNo sendDirectionActionAvailable) {
        this.put(SEND_DIRECTION_ACTION_AVAILABLE.value(), sendDirectionActionAvailable);
    }

    public void setUploadAdditionalEvidenceActionAvailable(YesOrNo uploadAdditionalEvidenceActionAvailable) {
        this.put(UPLOAD_ADDITIONAL_EVIDENCE_ACTION_AVAILABLE.value(), uploadAdditionalEvidenceActionAvailable);
    }

    public void setCurrentCaseStateVisibleToCaseOfficer(State currentCaseStateVisibleToCaseOfficer) {
        this.put(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER.value(), currentCaseStateVisibleToCaseOfficer);
    }

    public void setCurrentCaseStateVisibleToLegalRepresentative(State currentCaseStateVisibleToLegalRepresentative) {
        this.put(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE.value(), currentCaseStateVisibleToLegalRepresentative);
    }

    public void setCaseArgumentAvailable(YesOrNo caseArgumentAvailable) {
        this.put(CASE_ARGUMENT_AVAILABLE.value(), caseArgumentAvailable);
    }

    public void setAppealResponseAvailable(YesOrNo appealResponseAvailable) {
        this.put(APPEAL_RESPONSE_AVAILABLE.value(), appealResponseAvailable);
    }

    public void setSubmissionOutOfTime(YesOrNo submissionOutOfTime) {
        this.put(SUBMISSION_OUT_OF_TIME.value(), submissionOutOfTime);
    }


    // -----------------------------------------------------------------------------
    // start decision and reasons ...
    // -----------------------------------------------------------------------------

    public Optional<String> getCaseIntroductionDescription() {
        return read(CASE_INTRODUCTION_DESCRIPTION);
    }

    public Optional<String> getAppellantCaseSummaryTitle() {
        return read(APPELLANT_CASE_SUMMARY_TITLE);
    }

    public Optional<String> getAppellantCaseSummaryDescription() {
        return read(APPELLANT_CASE_SUMMARY_DESCRIPTION);
    }

    public Optional<YesOrNo> getImmigrationHistoryAgreement() {
        return read(IMMIGRATION_HISTORY_AGREEMENT);
    }

    public Optional<String> getAgreedImmigrationHistoryDescription() {
        return read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION);
    }

    public Optional<String> getImmigrationDisagreementInstructionsLabel() {
        return read(IMMIGRATION_DISAGREEMENT_INSTRUCTIONS_LABEL);
    }

    public Optional<String> getRespondentsImmigrationHistoryDescription() {
        return read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION);
    }

    public Optional<String> getImmigrationHistoryDisagreementDescription() {
        return read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION);
    }

    public Optional<YesOrNo> getScheduleOfIssuesAgreement() {
        return read(SCHEDULE_OF_ISSUES_AGREEMENT);
    }

    public Optional<String> getAgreedScheduleOfIssuesDescription() {
        return read(AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION);
    }

    public Optional<String> getScheduleOfIssuesAgreementInstructionsLabel() {
        return read(SCHEDULE_OF_ISSUES_AGREEMENT_INSTRUCTIONS_LABEL);
    }

    public Optional<String> getScheduleOfIssuesDisagreementInstructionsLabel() {
        return read(SCHEDULE_OF_ISSUES_DISAGREEMENT_INSTRUCTIONS_LABEL);
    }

    public Optional<String> getRespondentsScheduleOfIssuesDescription() {
        return read(RESPONDENTS_SCHEDULE_OF_ISSUES_DESCRIPTION);
    }

    public Optional<String> getScheduleOfIssuesDisagreementDescription() {
        return read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION);
    }


    // -----------------------------------------------------------------------------
    // sub-state flags ...
    // -----------------------------------------------------------------------------


    public void clearCaseBuildingReadyForSubmission() {
        this.put(SEND_DIRECTION_EXPLANATION.value(), null);
    }

    public void clearRespondentReviewAppealResponseAdded() {
        this.put(SEND_DIRECTION_EXPLANATION.value(), null);
    }

    public void setCaseBuildingReadyForSubmission(YesOrNo caseBuildingReadyForSubmission) {
        this.put(CASE_BUILDING_READY_FOR_SUBMISSION.value(), caseBuildingReadyForSubmission);
    }

    public void setRespondentReviewAppealResponseAdded(YesOrNo respondentReviewAppealResponseAdded) {
        this.put(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED.value(), respondentReviewAppealResponseAdded);
    }

}