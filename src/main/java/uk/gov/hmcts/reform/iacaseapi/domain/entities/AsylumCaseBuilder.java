package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Component
public class AsylumCaseBuilder {

    // -----------------------------------------------------------------------------
    // legal rep appeal ...
    // -----------------------------------------------------------------------------

    private Optional<String> homeOfficeReferenceNumber = Optional.empty();
    private Optional<String> homeOfficeDecisionDate = Optional.empty();
    private Optional<String> appellantTitle = Optional.empty();
    private Optional<String> appellantGivenNames = Optional.empty();
    private Optional<String> appellantFamilyName = Optional.empty();
    private Optional<String> appellantDateOfBirth = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> appellantNationalities = Optional.empty();
    private Optional<YesOrNo> appellantHasFixedAddress = Optional.empty();
    private Optional<AddressUk> appellantAddress = Optional.empty();
    private Optional<AppealType> appealType = Optional.empty();
    private Optional<CheckValues<String>> appealGroundsProtection = Optional.empty();
    private Optional<CheckValues<String>> appealGroundsHumanRights = Optional.empty();
    private Optional<CheckValues<String>> appealGroundsRevocation = Optional.empty();
    private Optional<YesOrNo> hasNewMatters = Optional.empty();
    private Optional<String> newMatters = Optional.empty();
    private Optional<String> hasOtherAppeals = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> otherAppeals = Optional.empty();
    private Optional<String> legalRepReferenceNumber = Optional.empty();
    private Optional<String> appealReferenceNumber = Optional.empty();
    private Optional<String> appellantNameForDisplay = Optional.empty();
    private Optional<List<String>> appealGroundsForDisplay = Optional.empty();
    private Optional<HearingCentre> hearingCentre = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    private Optional<String> sendDirectionExplanation = Optional.empty();
    private Optional<Parties> sendDirectionParties = Optional.empty();
    private Optional<String> sendDirectionDateDue = Optional.empty();
    private Optional<List<IdValue<Direction>>> directions = Optional.empty();

    // -----------------------------------------------------------------------------
    // change direction due date ...
    // -----------------------------------------------------------------------------

    private Optional<List<IdValue<EditableDirection>>> editableDirections = Optional.empty();

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    private Optional<List<IdValue<DocumentWithMetadata>>> additionalEvidenceDocuments = Optional.empty();
    private Optional<List<IdValue<DocumentWithMetadata>>> hearingDocuments = Optional.empty();
    private Optional<List<IdValue<DocumentWithMetadata>>> legalRepresentativeDocuments = Optional.empty();
    private Optional<List<IdValue<DocumentWithMetadata>>> respondentDocuments = Optional.empty();

    // -----------------------------------------------------------------------------
    // upload respondent evidence ...
    // -----------------------------------------------------------------------------

    private Optional<List<IdValue<DocumentWithDescription>>> respondentEvidence = Optional.empty();

    // -----------------------------------------------------------------------------
    // case argument ...
    // -----------------------------------------------------------------------------

    private Optional<Document> caseArgumentDocument = Optional.empty();
    private Optional<String> caseArgumentDescription = Optional.empty();
    private Optional<List<IdValue<DocumentWithDescription>>> caseArgumentEvidence = Optional.empty();

    // -----------------------------------------------------------------------------
    // appeal response ...
    // -----------------------------------------------------------------------------

    private Optional<Document> appealResponseDocument = Optional.empty();
    private Optional<String> appealResponseDescription = Optional.empty();
    private Optional<List<IdValue<DocumentWithDescription>>> appealResponseEvidence = Optional.empty();

    // -----------------------------------------------------------------------------
    // out of time reason ...
    // -----------------------------------------------------------------------------

    private Optional<String> applicationOutOfTimeExplanation = Optional.empty();
    private Optional<Document> applicationOutOfTimeDocument = Optional.empty();

    // -----------------------------------------------------------------------------
    // upload additional evidence ...
    // -----------------------------------------------------------------------------

    private Optional<List<IdValue<DocumentWithDescription>>> additionalEvidence = Optional.empty();

    // -----------------------------------------------------------------------------
    // list case ...
    // -----------------------------------------------------------------------------

    private Optional<HearingCentre> listCaseHearingCentre = Optional.empty();
    private Optional<HearingLength> listCaseHearingLength = Optional.empty();
    private Optional<String> listCaseHearingDate = Optional.empty();
    private Optional<String> listCaseRequirementsVulnerabilities = Optional.empty();
    private Optional<String> listCaseRequirementsMultimedia = Optional.empty();
    private Optional<String> listCaseRequirementsSingleSexCourt = Optional.empty();
    private Optional<String> listCaseRequirementsInCameraCourt = Optional.empty();
    private Optional<String> listCaseRequirementsOther = Optional.empty();

    // -----------------------------------------------------------------------------
    // create case summary ...
    // -----------------------------------------------------------------------------

    private Optional<Document> caseSummaryDocument = Optional.empty();
    private Optional<String> caseSummaryDescription = Optional.empty();

    // -----------------------------------------------------------------------------
    // start decision and reasons ...
    // -----------------------------------------------------------------------------

    private Optional<String> caseIntroductionDescription = Optional.empty();
    private Optional<String> appellantCaseSummaryTitle = Optional.empty();
    private Optional<String> appellantCaseSummaryDescription = Optional.empty();
    private Optional<YesOrNo> immigrationHistoryAgreement = Optional.empty();
    private Optional<String> agreedImmigrationHistoryDescription = Optional.empty();
    private Optional<String> immigrationDisagreementInstructionsLabel = Optional.empty();
    private Optional<String> respondentsImmigrationHistoryDescription = Optional.empty();
    private Optional<String> immigrationHistoryDisagreementDescription = Optional.empty();
    private Optional<YesOrNo> scheduleOfIssuesAgreement = Optional.empty();
    private Optional<String> agreedScheduleOfIssuesDescription = Optional.empty();
    private Optional<String> scheduleOfIssuesAgreementInstructionsLabel = Optional.empty();
    private Optional<String> scheduleOfIssuesDisagreementInstructionsLabel = Optional.empty();
    private Optional<String> respondentsScheduleOfIssuesDescription = Optional.empty();
    private Optional<String> scheduleOfIssuesDisagreementDescription = Optional.empty();

    // -----------------------------------------------------------------------------
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    private Optional<String> legalRepresentativeName = Optional.empty();
    private Optional<String> legalRepresentativeEmailAddress = Optional.empty();
    private Optional<List<IdValue<String>>> notificationsSent = Optional.empty();
    private Optional<YesOrNo> changeDirectionDueDateActionAvailable = Optional.empty();
    private Optional<YesOrNo> sendDirectionActionAvailable = Optional.empty();
    private Optional<YesOrNo> uploadAdditionalEvidenceActionAvailable = Optional.empty();
    private Optional<State> currentCaseStateVisibleToCaseOfficer = Optional.empty();
    private Optional<State> currentCaseStateVisibleToLegalRepresentative = Optional.empty();
    private Optional<YesOrNo> caseArgumentAvailable = Optional.empty();
    private Optional<YesOrNo> appealResponseAvailable = Optional.empty();
    private Optional<YesOrNo> submissionOutOfTime = Optional.empty();

    // -----------------------------------------------------------------------------
    // sub-state flags ...
    // -----------------------------------------------------------------------------

    private Optional<YesOrNo> caseBuildingReadyForSubmission = Optional.empty();
    private Optional<YesOrNo> respondentReviewAppealResponseAdded = Optional.empty();

    // -----------------------------------------------------------------------------

    public AsylumCase build() {
        return new AsylumCase(this);
    }

    // -----------------------------------------------------------------------------
    // legal rep appeal ...
    // -----------------------------------------------------------------------------

    public Optional<String> getHomeOfficeReferenceNumber() {
        return homeOfficeReferenceNumber;
    }

    public Optional<String> getHomeOfficeDecisionDate() {
        return homeOfficeDecisionDate;
    }

    public Optional<String> getAppellantTitle() {
        return appellantTitle;
    }

    public Optional<String> getAppellantGivenNames() {
        return appellantGivenNames;
    }

    public Optional<String> getAppellantFamilyName() {
        return appellantFamilyName;
    }

    public Optional<String> getAppellantDateOfBirth() {
        return appellantDateOfBirth;
    }

    public Optional<List<IdValue<Map<String, String>>>> getAppellantNationalities() {
        return appellantNationalities;
    }

    public Optional<YesOrNo> getAppellantHasFixedAddress() {
        return appellantHasFixedAddress;
    }

    public Optional<AddressUk> getAppellantAddress() {
        return appellantAddress;
    }

    public Optional<AppealType> getAppealType() {
        return appealType;
    }

    public Optional<CheckValues<String>> getAppealGroundsProtection() {
        return appealGroundsProtection;
    }

    public Optional<CheckValues<String>> getAppealGroundsHumanRights() {
        return appealGroundsHumanRights;
    }

    public Optional<CheckValues<String>> getAppealGroundsRevocation() {
        return appealGroundsRevocation;
    }

    public Optional<YesOrNo> getHasNewMatters() {
        return hasNewMatters;
    }

    public Optional<String> getNewMatters() {
        return newMatters;
    }

    public Optional<String> getHasOtherAppeals() {
        return hasOtherAppeals;
    }

    public Optional<List<IdValue<Map<String, String>>>> getOtherAppeals() {
        return otherAppeals;
    }

    public Optional<String> getLegalRepReferenceNumber() {
        return legalRepReferenceNumber;
    }

    public Optional<String> getAppealReferenceNumber() {
        return appealReferenceNumber;
    }

    public Optional<String> getAppellantNameForDisplay() {
        return appellantNameForDisplay;
    }

    public Optional<List<String>> getAppealGroundsForDisplay() {
        return appealGroundsForDisplay;
    }

    public Optional<HearingCentre> getHearingCentre() {
        return hearingCentre;
    }

    public void setHomeOfficeReferenceNumber(Optional<String> homeOfficeReferenceNumber) {
        this.homeOfficeReferenceNumber = homeOfficeReferenceNumber;
    }

    public void setHomeOfficeDecisionDate(Optional<String> homeOfficeDecisionDate) {
        this.homeOfficeDecisionDate = homeOfficeDecisionDate;
    }

    public void setAppellantTitle(Optional<String> appellantTitle) {
        this.appellantTitle = appellantTitle;
    }

    public void setAppellantGivenNames(Optional<String> appellantGivenNames) {
        this.appellantGivenNames = appellantGivenNames;
    }

    public void setAppellantFamilyName(Optional<String> appellantFamilyName) {
        this.appellantFamilyName = appellantFamilyName;
    }

    public void setAppellantDateOfBirth(Optional<String> appellantDateOfBirth) {
        this.appellantDateOfBirth = appellantDateOfBirth;
    }

    public void setAppellantNationalities(Optional<List<IdValue<Map<String, String>>>> appellantNationalities) {
        this.appellantNationalities = appellantNationalities;
    }

    public void setAppellantHasFixedAddress(Optional<YesOrNo> appellantHasFixedAddress) {
        this.appellantHasFixedAddress = appellantHasFixedAddress;
    }

    public void setAppellantAddress(Optional<AddressUk> appellantAddress) {
        this.appellantAddress = appellantAddress;
    }

    public void setAppealType(Optional<AppealType> appealType) {
        this.appealType = appealType;
    }

    public void setAppealGroundsProtection(Optional<CheckValues<String>> appealGroundsProtection) {
        this.appealGroundsProtection = appealGroundsProtection;
    }

    public void setAppealGroundsHumanRights(Optional<CheckValues<String>> appealGroundsHumanRights) {
        this.appealGroundsHumanRights = appealGroundsHumanRights;
    }

    public void setAppealGroundsRevocation(Optional<CheckValues<String>> appealGroundsRevocation) {
        this.appealGroundsRevocation = appealGroundsRevocation;
    }

    public void setHasNewMatters(Optional<YesOrNo> hasNewMatters) {
        this.hasNewMatters = hasNewMatters;
    }

    public void setNewMatters(Optional<String> newMatters) {
        this.newMatters = newMatters;
    }

    public void setHasOtherAppeals(Optional<String> hasOtherAppeals) {
        this.hasOtherAppeals = hasOtherAppeals;
    }

    public void setOtherAppeals(Optional<List<IdValue<Map<String, String>>>> otherAppeals) {
        this.otherAppeals = otherAppeals;
    }

    public void setLegalRepReferenceNumber(Optional<String> legalRepReferenceNumber) {
        this.legalRepReferenceNumber = legalRepReferenceNumber;
    }

    public void setAppealReferenceNumber(Optional<String> appealReferenceNumber) {
        this.appealReferenceNumber = appealReferenceNumber;
    }

    public void setAppellantNameForDisplay(Optional<String> appellantNameForDisplay) {
        this.appellantNameForDisplay = appellantNameForDisplay;
    }

    public void setAppealGroundsForDisplay(Optional<List<String>> appealGroundsForDisplay) {
        this.appealGroundsForDisplay = appealGroundsForDisplay;
    }

    public void setHearingCentre(Optional<HearingCentre> hearingCentre) {
        this.hearingCentre = hearingCentre;
    }

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    public Optional<String> getSendDirectionExplanation() {
        return sendDirectionExplanation;
    }

    public Optional<Parties> getSendDirectionParties() {
        return sendDirectionParties;
    }

    public Optional<String> getSendDirectionDateDue() {
        return sendDirectionDateDue;
    }

    public Optional<List<IdValue<Direction>>> getDirections() {
        return directions;
    }

    public void setSendDirectionExplanation(Optional<String> sendDirectionExplanation) {
        this.sendDirectionExplanation = sendDirectionExplanation;
    }

    public void setSendDirectionParties(Optional<Parties> sendDirectionParties) {
        this.sendDirectionParties = sendDirectionParties;
    }

    public void setSendDirectionDateDue(Optional<String> sendDirectionDateDue) {
        this.sendDirectionDateDue = sendDirectionDateDue;
    }

    public void setDirections(Optional<List<IdValue<Direction>>> directions) {
        this.directions = directions;
    }

    // -----------------------------------------------------------------------------
    // change direction due date ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<EditableDirection>>> getEditableDirections() {
        return editableDirections;
    }

    public void setEditableDirections(Optional<List<IdValue<EditableDirection>>> editableDirections) {
        this.editableDirections = editableDirections;
    }

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithMetadata>>> getAdditionalEvidenceDocuments() {
        return additionalEvidenceDocuments;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getHearingDocuments() {
        return hearingDocuments;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getLegalRepresentativeDocuments() {
        return legalRepresentativeDocuments;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getRespondentDocuments() {
        return respondentDocuments;
    }

    public void setAdditionalEvidenceDocuments(Optional<List<IdValue<DocumentWithMetadata>>> additionalEvidenceDocuments) {
        this.additionalEvidenceDocuments = additionalEvidenceDocuments;
    }

    public void setHearingDocuments(Optional<List<IdValue<DocumentWithMetadata>>> hearingDocuments) {
        this.hearingDocuments = hearingDocuments;
    }

    public void setLegalRepresentativeDocuments(Optional<List<IdValue<DocumentWithMetadata>>> legalRepresentativeDocuments) {
        this.legalRepresentativeDocuments = legalRepresentativeDocuments;
    }

    public void setRespondentDocuments(Optional<List<IdValue<DocumentWithMetadata>>> respondentDocuments) {
        this.respondentDocuments = respondentDocuments;
    }

    // -----------------------------------------------------------------------------
    // upload respondent evidence ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithDescription>>> getRespondentEvidence() {
        return respondentEvidence;
    }

    public void setRespondentEvidence(Optional<List<IdValue<DocumentWithDescription>>> respondentEvidence) {
        this.respondentEvidence = respondentEvidence;
    }

    // -----------------------------------------------------------------------------
    // case argument ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getCaseArgumentDocument() {
        return caseArgumentDocument;
    }

    public Optional<String> getCaseArgumentDescription() {
        return caseArgumentDescription;
    }

    public Optional<List<IdValue<DocumentWithDescription>>> getCaseArgumentEvidence() {
        return caseArgumentEvidence;
    }

    public void setCaseArgumentDocument(Optional<Document> caseArgumentDocument) {
        this.caseArgumentDocument = caseArgumentDocument;
    }

    public void setCaseArgumentDescription(Optional<String> caseArgumentDescription) {
        this.caseArgumentDescription = caseArgumentDescription;
    }

    public void setCaseArgumentEvidence(Optional<List<IdValue<DocumentWithDescription>>> caseArgumentEvidence) {
        this.caseArgumentEvidence = caseArgumentEvidence;
    }

    // -----------------------------------------------------------------------------
    // appeal response ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getAppealResponseDocument() {
        return appealResponseDocument;
    }

    public Optional<String> getAppealResponseDescription() {
        return appealResponseDescription;
    }

    public Optional<List<IdValue<DocumentWithDescription>>> getAppealResponseEvidence() {
        return appealResponseEvidence;
    }

    public void setAppealResponseDocument(Optional<Document> appealResponseDocument) {
        this.appealResponseDocument = appealResponseDocument;
    }

    public void setAppealResponseDescription(Optional<String> appealResponseDescription) {
        this.appealResponseDescription = appealResponseDescription;
    }

    public void setAppealResponseEvidence(Optional<List<IdValue<DocumentWithDescription>>> appealResponseEvidence) {
        this.appealResponseEvidence = appealResponseEvidence;
    }

    // -----------------------------------------------------------------------------
    // out of time reason ...
    // -----------------------------------------------------------------------------

    public void setApplicationOutOfTimeExplanation(Optional<String> applicationOutOfTimeExplanation) {
        this.applicationOutOfTimeExplanation = applicationOutOfTimeExplanation;
    }

    public void setApplicationOutOfTimeDocument(Optional<Document> applicationOutOfTimeDocument) {
        this.applicationOutOfTimeDocument = applicationOutOfTimeDocument;
    }

    // -----------------------------------------------------------------------------
    // upload additional evidence ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithDescription>>> getAdditionalEvidence() {
        return additionalEvidence;
    }

    public void setAdditionalEvidence(Optional<List<IdValue<DocumentWithDescription>>> additionalEvidence) {
        this.additionalEvidence = additionalEvidence;
    }

    // -----------------------------------------------------------------------------
    // list case ...
    // -----------------------------------------------------------------------------

    public Optional<HearingCentre> getListCaseHearingCentre() {
        return listCaseHearingCentre;
    }

    public Optional<HearingLength> getListCaseHearingLength() {
        return listCaseHearingLength;
    }

    public Optional<String> getListCaseHearingDate() {
        return listCaseHearingDate;
    }

    public Optional<String> getListCaseRequirementsVulnerabilities() {
        return listCaseRequirementsVulnerabilities;
    }

    public Optional<String> getListCaseRequirementsMultimedia() {
        return listCaseRequirementsMultimedia;
    }

    public Optional<String> getListCaseRequirementsSingleSexCourt() {
        return listCaseRequirementsSingleSexCourt;
    }

    public Optional<String> getListCaseRequirementsInCameraCourt() {
        return listCaseRequirementsInCameraCourt;
    }

    public Optional<String> getListCaseRequirementsOther() {
        return listCaseRequirementsOther;
    }

    public void setListCaseHearingCentre(Optional<HearingCentre> listCaseHearingCentre) {
        this.listCaseHearingCentre = listCaseHearingCentre;
    }

    public void setListCaseHearingLength(Optional<HearingLength> listCaseHearingLength) {
        this.listCaseHearingLength = listCaseHearingLength;
    }

    public void setListCaseHearingDate(Optional<String> listCaseHearingDate) {
        this.listCaseHearingDate = listCaseHearingDate;
    }

    public void setListCaseRequirementsVulnerabilities(Optional<String> listCaseRequirementsVulnerabilities) {
        this.listCaseRequirementsVulnerabilities = listCaseRequirementsVulnerabilities;
    }

    public void setListCaseRequirementsMultimedia(Optional<String> listCaseRequirementsMultimedia) {
        this.listCaseRequirementsMultimedia = listCaseRequirementsMultimedia;
    }

    public void setListCaseRequirementsSingleSexCourt(Optional<String> listCaseRequirementsSingleSexCourt) {
        this.listCaseRequirementsSingleSexCourt = listCaseRequirementsSingleSexCourt;
    }

    public void setListCaseRequirementsInCameraCourt(Optional<String> listCaseRequirementsInCameraCourt) {
        this.listCaseRequirementsInCameraCourt = listCaseRequirementsInCameraCourt;
    }

    public void setListCaseRequirementsOther(Optional<String> listCaseRequirementsOther) {
        this.listCaseRequirementsOther = listCaseRequirementsOther;
    }

    // -----------------------------------------------------------------------------
    // create case summary ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getCaseSummaryDocument() {
        return caseSummaryDocument;
    }

    public Optional<String> getCaseSummaryDescription() {
        return caseSummaryDescription;
    }

    public void setCaseSummaryDocument(Optional<Document> caseSummaryDocument) {
        this.caseSummaryDocument = caseSummaryDocument;
    }

    public void setCaseSummaryDescription(Optional<String> caseSummaryDescription) {
        this.caseSummaryDescription = caseSummaryDescription;
    }

    // -----------------------------------------------------------------------------
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    public Optional<String> getLegalRepresentativeName() {
        return legalRepresentativeName;
    }

    public Optional<String> getLegalRepresentativeEmailAddress() {
        return legalRepresentativeEmailAddress;
    }

    public Optional<List<IdValue<String>>> getNotificationsSent() {
        return notificationsSent;
    }

    public Optional<YesOrNo> getChangeDirectionDueDateActionAvailable() {
        return changeDirectionDueDateActionAvailable;
    }

    public Optional<YesOrNo> getSendDirectionActionAvailable() {
        return sendDirectionActionAvailable;
    }

    public Optional<YesOrNo> getUploadAdditionalEvidenceActionAvailable() {
        return uploadAdditionalEvidenceActionAvailable;
    }

    public Optional<State> getCurrentCaseStateVisibleToCaseOfficer() {
        return currentCaseStateVisibleToCaseOfficer;
    }

    public Optional<State> getCurrentCaseStateVisibleToLegalRepresentative() {
        return currentCaseStateVisibleToLegalRepresentative;
    }

    public Optional<YesOrNo> getCaseArgumentAvailable() {
        return caseArgumentAvailable;
    }

    public Optional<YesOrNo> getAppealResponseAvailable() {
        return appealResponseAvailable;
    }

    public Optional<YesOrNo> getSubmissionOutOfTime() {
        return submissionOutOfTime;
    }

    public Optional<String> getApplicationOutOfTimeExplanation() {
        return applicationOutOfTimeExplanation;
    }

    public Optional<Document> getApplicationOutOfTimeDocument() {
        return applicationOutOfTimeDocument;
    }

    public void setLegalRepresentativeName(Optional<String> legalRepresentativeName) {
        this.legalRepresentativeName = legalRepresentativeName;
    }

    public void setLegalRepresentativeEmailAddress(Optional<String> legalRepresentativeEmailAddress) {
        this.legalRepresentativeEmailAddress = legalRepresentativeEmailAddress;
    }

    public void setNotificationsSent(Optional<List<IdValue<String>>> notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    public void setChangeDirectionDueDateActionAvailable(Optional<YesOrNo> changeDirectionDueDateActionAvailable) {
        this.changeDirectionDueDateActionAvailable = changeDirectionDueDateActionAvailable;
    }

    public void setSendDirectionActionAvailable(Optional<YesOrNo> sendDirectionActionAvailable) {
        this.sendDirectionActionAvailable = sendDirectionActionAvailable;
    }

    public void setUploadAdditionalEvidenceActionAvailable(Optional<YesOrNo> uploadAdditionalEvidenceActionAvailable) {
        this.uploadAdditionalEvidenceActionAvailable = uploadAdditionalEvidenceActionAvailable;
    }

    public void setCurrentCaseStateVisibleToCaseOfficer(Optional<State> currentCaseStateVisibleToCaseOfficer) {
        this.currentCaseStateVisibleToCaseOfficer = currentCaseStateVisibleToCaseOfficer;
    }

    public void setCurrentCaseStateVisibleToLegalRepresentative(Optional<State> currentCaseStateVisibleToLegalRepresentative) {
        this.currentCaseStateVisibleToLegalRepresentative = currentCaseStateVisibleToLegalRepresentative;
    }

    public void setCaseArgumentAvailable(Optional<YesOrNo> caseArgumentAvailable) {
        this.caseArgumentAvailable = caseArgumentAvailable;
    }

    public void setAppealResponseAvailable(Optional<YesOrNo> appealResponseAvailable) {
        this.appealResponseAvailable = appealResponseAvailable;
    }

    public void setSubmissionOutOfTime(Optional<YesOrNo> submissionOutOfTime) {
        this.submissionOutOfTime = submissionOutOfTime;
    }

    // -----------------------------------------------------------------------------
    // start decision and reasons ...
    // -----------------------------------------------------------------------------

    public Optional<String> getCaseIntroductionDescription() {
        return caseIntroductionDescription;
    }

    public void setCaseIntroductionDescription(Optional<String> caseIntroductionDescription) {
        this.caseIntroductionDescription = caseIntroductionDescription;
    }

    public Optional<String> getAppellantCaseSummaryTitle() {
        return appellantCaseSummaryTitle;
    }

    public void setAppellantCaseSummaryTitle(Optional<String> appellantCaseSummaryTitle) {
        this.appellantCaseSummaryTitle = appellantCaseSummaryTitle;
    }

    public Optional<String> getAppellantCaseSummaryDescription() {
        return appellantCaseSummaryDescription;
    }

    public void setAppellantCaseSummaryDescription(Optional<String> appellantCaseSummaryDescription) {
        this.appellantCaseSummaryDescription = appellantCaseSummaryDescription;
    }

    public Optional<YesOrNo> getImmigrationHistoryAgreement() {
        return immigrationHistoryAgreement;
    }

    public void setImmigrationHistoryAgreement(Optional<YesOrNo> immigrationHistoryAgreement) {
        this.immigrationHistoryAgreement = immigrationHistoryAgreement;
    }

    public Optional<String> getAgreedImmigrationHistoryDescription() {
        return agreedImmigrationHistoryDescription;
    }

    public void setAgreedImmigrationHistoryDescription(Optional<String> agreedImmigrationHistoryDescription) {
        this.agreedImmigrationHistoryDescription = agreedImmigrationHistoryDescription;
    }

    public Optional<String> getImmigrationDisagreementInstructionsLabel() {
        return immigrationDisagreementInstructionsLabel;
    }

    public void setImmigrationDisagreementInstructionsLabel(Optional<String> immigrationDisagreementInstructionsLabel) {
        this.immigrationDisagreementInstructionsLabel = immigrationDisagreementInstructionsLabel;
    }

    public Optional<String> getRespondentsImmigrationHistoryDescription() {
        return respondentsImmigrationHistoryDescription;
    }

    public void setRespondentsImmigrationHistoryDescription(Optional<String> respondentsImmigrationHistoryDescription) {
        this.respondentsImmigrationHistoryDescription = respondentsImmigrationHistoryDescription;
    }

    public Optional<String> getImmigrationHistoryDisagreementDescription() {
        return immigrationHistoryDisagreementDescription;
    }

    public void setImmigrationHistoryDisagreementDescription(Optional<String> immigrationHistoryDisagreementDescription) {
        this.immigrationHistoryDisagreementDescription = immigrationHistoryDisagreementDescription;
    }

    public Optional<YesOrNo> getScheduleOfIssuesAgreement() {
        return scheduleOfIssuesAgreement;
    }

    public void setScheduleOfIssuesAgreement(Optional<YesOrNo> scheduleOfIssuesAgreement) {
        this.scheduleOfIssuesAgreement = scheduleOfIssuesAgreement;
    }

    public Optional<String> getAgreedScheduleOfIssuesDescription() {
        return agreedScheduleOfIssuesDescription;
    }

    public void setAgreedScheduleOfIssuesDescription(Optional<String> agreedScheduleOfIssuesDescription) {
        this.agreedScheduleOfIssuesDescription = agreedScheduleOfIssuesDescription;
    }

    public Optional<String> getScheduleOfIssuesAgreementInstructionsLabel() {
        return scheduleOfIssuesAgreementInstructionsLabel;
    }

    public void setScheduleOfIssuesAgreementInstructionsLabel(Optional<String> scheduleOfIssuesAgreementInstructionsLabel) {
        this.scheduleOfIssuesAgreementInstructionsLabel = scheduleOfIssuesAgreementInstructionsLabel;
    }

    public Optional<String> getScheduleOfIssuesDisagreementInstructionsLabel() {
        return scheduleOfIssuesDisagreementInstructionsLabel;
    }

    public void setScheduleOfIssuesDisagreementInstructionsLabel(Optional<String> scheduleOfIssuesDisagreementInstructionsLabel) {
        this.scheduleOfIssuesDisagreementInstructionsLabel = scheduleOfIssuesDisagreementInstructionsLabel;
    }

    public Optional<String> getRespondentsScheduleOfIssuesDescription() {
        return respondentsScheduleOfIssuesDescription;
    }

    public void setRespondentsScheduleOfIssuesDescription(Optional<String> respondentsScheduleOfIssuesDescription) {
        this.respondentsScheduleOfIssuesDescription = respondentsScheduleOfIssuesDescription;
    }

    public Optional<String> getScheduleOfIssuesDisagreementDescription() {
        return scheduleOfIssuesDisagreementDescription;
    }

    public void setScheduleOfIssuesDisagreementDescription(Optional<String> scheduleOfIssuesDisagreementDescription) {
        this.scheduleOfIssuesDisagreementDescription = scheduleOfIssuesDisagreementDescription;
    }


    // -----------------------------------------------------------------------------
    // sub-state flags ...
    // -----------------------------------------------------------------------------

    public Optional<YesOrNo> getCaseBuildingReadyForSubmission() {
        return caseBuildingReadyForSubmission;
    }

    public Optional<YesOrNo> getRespondentReviewAppealResponseAdded() {
        return respondentReviewAppealResponseAdded;
    }

    public void setCaseBuildingReadyForSubmission(Optional<YesOrNo> caseBuildingReadyForSubmission) {
        this.caseBuildingReadyForSubmission = caseBuildingReadyForSubmission;
    }

    public void setRespondentReviewAppealResponseAdded(Optional<YesOrNo> respondentReviewAppealResponseAdded) {
        this.respondentReviewAppealResponseAdded = respondentReviewAppealResponseAdded;
    }
}
