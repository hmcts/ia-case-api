package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class AsylumCase implements CaseData {

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

    private AsylumCase() {
        // noop -- for deserializers
    }

    public AsylumCase(
        AsylumCaseBuilder asylumCaseBuilder
    ) {
        this.homeOfficeReferenceNumber = asylumCaseBuilder.getHomeOfficeReferenceNumber();
        this.homeOfficeDecisionDate = asylumCaseBuilder.getHomeOfficeDecisionDate();
        this.appellantTitle = asylumCaseBuilder.getAppellantTitle();
        this.appellantGivenNames = asylumCaseBuilder.getAppellantGivenNames();
        this.appellantFamilyName = asylumCaseBuilder.getAppellantFamilyName();
        this.appellantDateOfBirth = asylumCaseBuilder.getAppellantDateOfBirth();
        this.appellantNationalities = asylumCaseBuilder.getAppellantNationalities();
        this.appellantHasFixedAddress = asylumCaseBuilder.getAppellantHasFixedAddress();
        this.appellantAddress = asylumCaseBuilder.getAppellantAddress();
        this.appealType = asylumCaseBuilder.getAppealType();
        this.appealGroundsProtection = asylumCaseBuilder.getAppealGroundsProtection();
        this.appealGroundsHumanRights = asylumCaseBuilder.getAppealGroundsHumanRights();
        this.appealGroundsRevocation = asylumCaseBuilder.getAppealGroundsRevocation();
        this.hasNewMatters = asylumCaseBuilder.getHasNewMatters();
        this.newMatters = asylumCaseBuilder.getNewMatters();
        this.hasOtherAppeals = asylumCaseBuilder.getHasOtherAppeals();
        this.otherAppeals = asylumCaseBuilder.getOtherAppeals();
        this.legalRepReferenceNumber = asylumCaseBuilder.getLegalRepReferenceNumber();
        this.appealReferenceNumber = asylumCaseBuilder.getAppealReferenceNumber();
        this.appellantNameForDisplay = asylumCaseBuilder.getAppellantNameForDisplay();
        this.appealGroundsForDisplay = asylumCaseBuilder.getAppealGroundsForDisplay();
        this.hearingCentre = asylumCaseBuilder.getHearingCentre();
        this.sendDirectionExplanation = asylumCaseBuilder.getSendDirectionExplanation();
        this.sendDirectionParties = asylumCaseBuilder.getSendDirectionParties();
        this.sendDirectionDateDue = asylumCaseBuilder.getSendDirectionDateDue();
        this.directions = asylumCaseBuilder.getDirections();
        this.editableDirections = asylumCaseBuilder.getEditableDirections();
        this.additionalEvidenceDocuments = asylumCaseBuilder.getAdditionalEvidenceDocuments();
        this.legalRepresentativeDocuments = asylumCaseBuilder.getLegalRepresentativeDocuments();
        this.respondentDocuments = asylumCaseBuilder.getRespondentDocuments();
        this.respondentEvidence = asylumCaseBuilder.getRespondentEvidence();
        this.caseArgumentDocument = asylumCaseBuilder.getCaseArgumentDocument();
        this.caseArgumentDescription = asylumCaseBuilder.getCaseArgumentDescription();
        this.caseArgumentEvidence = asylumCaseBuilder.getCaseArgumentEvidence();
        this.appealResponseDocument = asylumCaseBuilder.getAppealResponseDocument();
        this.appealResponseDescription = asylumCaseBuilder.getAppealResponseDescription();
        this.appealResponseEvidence = asylumCaseBuilder.getAppealResponseEvidence();
        this.applicationOutOfTimeExplanation = asylumCaseBuilder.getApplicationOutOfTimeExplanation();
        this.applicationOutOfTimeDocument = asylumCaseBuilder.getApplicationOutOfTimeDocument();
        this.additionalEvidence = asylumCaseBuilder.getAdditionalEvidence();
        this.legalRepresentativeName = asylumCaseBuilder.getLegalRepresentativeName();
        this.legalRepresentativeEmailAddress = asylumCaseBuilder.getLegalRepresentativeEmailAddress();
        this.notificationsSent = asylumCaseBuilder.getNotificationsSent();
        this.changeDirectionDueDateActionAvailable = asylumCaseBuilder.getChangeDirectionDueDateActionAvailable();
        this.sendDirectionActionAvailable = asylumCaseBuilder.getSendDirectionActionAvailable();
        this.uploadAdditionalEvidenceActionAvailable = asylumCaseBuilder.getUploadAdditionalEvidenceActionAvailable();
        this.currentCaseStateVisibleToCaseOfficer = asylumCaseBuilder.getCurrentCaseStateVisibleToCaseOfficer();
        this.currentCaseStateVisibleToLegalRepresentative = asylumCaseBuilder.getCurrentCaseStateVisibleToLegalRepresentative();
        this.caseArgumentAvailable = asylumCaseBuilder.getCaseArgumentAvailable();
        this.appealResponseAvailable = asylumCaseBuilder.getAppealResponseAvailable();
        this.submissionOutOfTime = asylumCaseBuilder.getSubmissionOutOfTime();
        this.caseBuildingReadyForSubmission = asylumCaseBuilder.getCaseBuildingReadyForSubmission();
        this.respondentReviewAppealResponseAdded = asylumCaseBuilder.getRespondentReviewAppealResponseAdded();
    }

    public Optional<String> getHomeOfficeReferenceNumber() {
        requireNonNull(homeOfficeReferenceNumber);
        return homeOfficeReferenceNumber;
    }

    public Optional<String> getHomeOfficeDecisionDate() {
        requireNonNull(homeOfficeDecisionDate);
        return homeOfficeDecisionDate;
    }

    public Optional<String> getAppellantTitle() {
        requireNonNull(appellantTitle);
        return appellantTitle;
    }

    public Optional<String> getAppellantGivenNames() {
        requireNonNull(appellantGivenNames);
        return appellantGivenNames;
    }

    public Optional<String> getAppellantFamilyName() {
        requireNonNull(appellantFamilyName);
        return appellantFamilyName;
    }

    public Optional<String> getAppellantDateOfBirth() {
        requireNonNull(appellantDateOfBirth);
        return appellantDateOfBirth;
    }

    public Optional<List<IdValue<Map<String, String>>>> getAppellantNationalities() {
        requireNonNull(appellantNationalities);
        return appellantNationalities;
    }

    public Optional<YesOrNo> getAppellantHasFixedAddress() {
        requireNonNull(appellantHasFixedAddress);
        return appellantHasFixedAddress;
    }

    public Optional<AddressUk> getAppellantAddress() {
        requireNonNull(appellantAddress);
        return appellantAddress;
    }

    public Optional<AppealType> getAppealType() {
        requireNonNull(appealType);
        return appealType;
    }

    public Optional<CheckValues<String>> getAppealGroundsProtection() {
        requireNonNull(appealGroundsProtection);
        return appealGroundsProtection;
    }

    public Optional<CheckValues<String>> getAppealGroundsHumanRights() {
        requireNonNull(appealGroundsHumanRights);
        return appealGroundsHumanRights;
    }

    public Optional<CheckValues<String>> getAppealGroundsRevocation() {
        requireNonNull(appealGroundsRevocation);
        return appealGroundsRevocation;
    }

    public Optional<YesOrNo> getHasNewMatters() {
        requireNonNull(hasNewMatters);
        return hasNewMatters;
    }

    public Optional<String> getNewMatters() {
        requireNonNull(newMatters);
        return newMatters;
    }

    public Optional<String> getHasOtherAppeals() {
        requireNonNull(hasOtherAppeals);
        return hasOtherAppeals;
    }

    public Optional<List<IdValue<Map<String, String>>>> getOtherAppeals() {
        requireNonNull(otherAppeals);
        return otherAppeals;
    }

    public Optional<String> getLegalRepReferenceNumber() {
        requireNonNull(legalRepReferenceNumber);
        return legalRepReferenceNumber;
    }

    public Optional<String> getAppealReferenceNumber() {
        requireNonNull(appealReferenceNumber);
        return appealReferenceNumber;
    }

    public Optional<String> getAppellantNameForDisplay() {
        requireNonNull(appellantNameForDisplay);
        return appellantNameForDisplay;
    }

    public Optional<List<String>> getAppealGroundsForDisplay() {
        requireNonNull(appealGroundsForDisplay);
        return appealGroundsForDisplay;
    }

    public Optional<HearingCentre> getHearingCentre() {
        requireNonNull(hearingCentre);
        return hearingCentre;
    }

    public void setHomeOfficeReferenceNumber(String homeOfficeReferenceNumber) {
        this.homeOfficeReferenceNumber = Optional.ofNullable(homeOfficeReferenceNumber);
    }

    public void setAppealReferenceNumber(String appealReferenceNumber) {
        this.appealReferenceNumber = Optional.ofNullable(appealReferenceNumber);
    }

    public void setAppellantNameForDisplay(String appellantNameForDisplay) {
        this.appellantNameForDisplay = Optional.ofNullable(appellantNameForDisplay);
    }

    public void setAppealGroundsForDisplay(List<String> appealGroundsForDisplay) {
        this.appealGroundsForDisplay = Optional.ofNullable(appealGroundsForDisplay);
    }

    public void setHearingCentre(HearingCentre hearingCentre) {
        this.hearingCentre = Optional.ofNullable(hearingCentre);
    }

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    public Optional<String> getSendDirectionExplanation() {
        requireNonNull(sendDirectionExplanation);
        return sendDirectionExplanation;
    }

    public Optional<Parties> getSendDirectionParties() {
        requireNonNull(sendDirectionParties);
        return sendDirectionParties;
    }

    public Optional<String> getSendDirectionDateDue() {
        requireNonNull(sendDirectionDateDue);
        return sendDirectionDateDue;
    }

    public Optional<List<IdValue<Direction>>> getDirections() {
        requireNonNull(directions);
        return directions;
    }

    public void clearSendDirectionExplanation() {
        this.sendDirectionExplanation = Optional.empty();
    }

    public void clearSendDirectionParties() {
        this.sendDirectionParties = Optional.empty();
    }

    public void clearSendDirectionDateDue() {
        this.sendDirectionDateDue = Optional.empty();
    }

    public void setSendDirectionExplanation(String sendDirectionExplanation) {
        this.sendDirectionExplanation = Optional.ofNullable(sendDirectionExplanation);
    }

    public void setSendDirectionParties(Parties sendDirectionParties) {
        this.sendDirectionParties = Optional.ofNullable(sendDirectionParties);
    }

    public void setSendDirectionDateDue(String sendDirectionDateDue) {
        this.sendDirectionDateDue = Optional.ofNullable(sendDirectionDateDue);
    }

    public void setDirections(List<IdValue<Direction>> directions) {
        this.directions = Optional.ofNullable(directions);
    }

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<EditableDirection>>> getEditableDirections() {
        requireNonNull(editableDirections);
        return editableDirections;
    }

    public void clearEditableDirections() {
        this.editableDirections = Optional.empty();
    }

    public void setEditableDirections(List<IdValue<EditableDirection>> editableDirections) {
        this.editableDirections = Optional.ofNullable(editableDirections);
    }

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithMetadata>>> getAdditionalEvidenceDocuments() {
        requireNonNull(additionalEvidenceDocuments);
        return additionalEvidenceDocuments;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getLegalRepresentativeDocuments() {
        requireNonNull(legalRepresentativeDocuments);
        return legalRepresentativeDocuments;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getRespondentDocuments() {
        requireNonNull(respondentDocuments);
        return respondentDocuments;
    }

    public void setAdditionalEvidenceDocuments(List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments) {
        this.additionalEvidenceDocuments = Optional.ofNullable(additionalEvidenceDocuments);
    }

    public void setLegalRepresentativeDocuments(List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments) {
        this.legalRepresentativeDocuments = Optional.ofNullable(legalRepresentativeDocuments);
    }

    public void setRespondentDocuments(List<IdValue<DocumentWithMetadata>> respondentDocuments) {
        this.respondentDocuments = Optional.ofNullable(respondentDocuments);
    }

    // -----------------------------------------------------------------------------
    // upload respondent evidence ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithDescription>>> getRespondentEvidence() {
        requireNonNull(respondentEvidence);
        return respondentEvidence;
    }

    public void clearRespondentEvidence() {
        this.respondentEvidence = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case argument ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getCaseArgumentDocument() {
        requireNonNull(caseArgumentDocument);
        return caseArgumentDocument;
    }

    public Optional<String> getCaseArgumentDescription() {
        requireNonNull(caseArgumentDescription);
        return caseArgumentDescription;
    }

    public Optional<List<IdValue<DocumentWithDescription>>> getCaseArgumentEvidence() {
        requireNonNull(caseArgumentEvidence);
        return caseArgumentEvidence;
    }

    // -----------------------------------------------------------------------------
    // appeal response ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getAppealResponseDocument() {
        requireNonNull(appealResponseDocument);
        return appealResponseDocument;
    }

    public Optional<String> getAppealResponseDescription() {
        requireNonNull(appealResponseDescription);
        return appealResponseDescription;
    }

    public Optional<List<IdValue<DocumentWithDescription>>> getAppealResponseEvidence() {
        requireNonNull(appealResponseEvidence);
        return appealResponseEvidence;
    }

    // -----------------------------------------------------------------------------
    // out of time reason ...
    // -----------------------------------------------------------------------------

    public Optional<String> getApplicationOutOfTimeExplanation() {
        requireNonNull(applicationOutOfTimeExplanation);
        return applicationOutOfTimeExplanation;
    }

    public Optional<Document> getApplicationOutOfTimeDocument() {
        requireNonNull(applicationOutOfTimeDocument);
        return applicationOutOfTimeDocument;
    }

    // -----------------------------------------------------------------------------
    // upload additional evidence ...
    // -----------------------------------------------------------------------------

    public Optional<List<IdValue<DocumentWithDescription>>> getAdditionalEvidence() {
        requireNonNull(additionalEvidence);
        return additionalEvidence;
    }

    public void clearAdditionalEvidence() {
        this.additionalEvidence = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    public Optional<String> getLegalRepresentativeName() {
        requireNonNull(legalRepresentativeName);
        return legalRepresentativeName;
    }

    public Optional<String> getLegalRepresentativeEmailAddress() {
        requireNonNull(legalRepresentativeEmailAddress);
        return legalRepresentativeEmailAddress;
    }

    public Optional<List<IdValue<String>>> getNotificationsSent() {
        requireNonNull(notificationsSent);
        return notificationsSent;
    }

    public Optional<YesOrNo> getChangeDirectionDueDateActionAvailable() {
        requireNonNull(changeDirectionDueDateActionAvailable);
        return changeDirectionDueDateActionAvailable;
    }

    public Optional<YesOrNo> getSendDirectionActionAvailable() {
        requireNonNull(sendDirectionActionAvailable);
        return sendDirectionActionAvailable;
    }

    public Optional<YesOrNo> getUploadAdditionalEvidenceActionAvailable() {
        requireNonNull(uploadAdditionalEvidenceActionAvailable);
        return uploadAdditionalEvidenceActionAvailable;
    }

    public Optional<State> getCurrentCaseStateVisibleToCaseOfficer() {
        requireNonNull(currentCaseStateVisibleToCaseOfficer);
        return currentCaseStateVisibleToCaseOfficer;
    }

    public Optional<State> getCurrentCaseStateVisibleToLegalRepresentative() {
        requireNonNull(currentCaseStateVisibleToLegalRepresentative);
        return currentCaseStateVisibleToLegalRepresentative;
    }

    public Optional<YesOrNo> getCaseArgumentAvailable() {
        requireNonNull(caseArgumentAvailable);
        return caseArgumentAvailable;
    }

    public Optional<YesOrNo> getAppealResponseAvailable() {
        requireNonNull(appealResponseAvailable);
        return appealResponseAvailable;
    }

    public Optional<YesOrNo> getSubmissionOutOfTime() {
        requireNonNull(submissionOutOfTime);
        return submissionOutOfTime;
    }

    public void setLegalRepresentativeName(String legalRepresentativeName) {
        this.legalRepresentativeName = Optional.ofNullable(legalRepresentativeName);
    }

    public void setLegalRepresentativeEmailAddress(String legalRepresentativeEmailAddress) {
        this.legalRepresentativeEmailAddress = Optional.ofNullable(legalRepresentativeEmailAddress);
    }

    public void setNotificationsSent(List<IdValue<String>> notificationsSent) {
        this.notificationsSent = Optional.ofNullable(notificationsSent);
    }

    public void setChangeDirectionDueDateActionAvailable(YesOrNo changeDirectionDueDateActionAvailable) {
        this.changeDirectionDueDateActionAvailable = Optional.ofNullable(changeDirectionDueDateActionAvailable);
    }

    public void setSendDirectionActionAvailable(YesOrNo sendDirectionActionAvailable) {
        this.sendDirectionActionAvailable = Optional.ofNullable(sendDirectionActionAvailable);
    }

    public void setUploadAdditionalEvidenceActionAvailable(YesOrNo uploadAdditionalEvidenceActionAvailable) {
        this.uploadAdditionalEvidenceActionAvailable = Optional.ofNullable(uploadAdditionalEvidenceActionAvailable);
    }

    public void setCurrentCaseStateVisibleToCaseOfficer(State currentCaseStateVisibleToCaseOfficer) {
        this.currentCaseStateVisibleToCaseOfficer = Optional.ofNullable(currentCaseStateVisibleToCaseOfficer);
    }

    public void setCurrentCaseStateVisibleToLegalRepresentative(State currentCaseStateVisibleToLegalRepresentative) {
        this.currentCaseStateVisibleToLegalRepresentative = Optional.ofNullable(currentCaseStateVisibleToLegalRepresentative);
    }

    public void setCaseArgumentAvailable(YesOrNo caseArgumentAvailable) {
        this.caseArgumentAvailable = Optional.ofNullable(caseArgumentAvailable);
    }

    public void setAppealResponseAvailable(YesOrNo appealResponseAvailable) {
        this.appealResponseAvailable = Optional.ofNullable(appealResponseAvailable);
    }

    public void setSubmissionOutOfTime(YesOrNo submissionOutOfTime) {
        this.submissionOutOfTime = Optional.ofNullable(submissionOutOfTime);
    }

    // -----------------------------------------------------------------------------
    // sub-state flags ...
    // -----------------------------------------------------------------------------

    public Optional<YesOrNo> getCaseBuildingReadyForSubmission() {
        requireNonNull(caseBuildingReadyForSubmission);
        return caseBuildingReadyForSubmission;
    }

    public Optional<YesOrNo> getRespondentReviewAppealResponseAdded() {
        requireNonNull(respondentReviewAppealResponseAdded);
        return respondentReviewAppealResponseAdded;
    }

    public void clearCaseBuildingReadyForSubmission() {
        this.caseBuildingReadyForSubmission = Optional.empty();
    }

    public void clearRespondentReviewAppealResponseAdded() {
        this.respondentReviewAppealResponseAdded = Optional.empty();
    }

    public void setCaseBuildingReadyForSubmission(YesOrNo caseBuildingReadyForSubmission) {
        this.caseBuildingReadyForSubmission = Optional.ofNullable(caseBuildingReadyForSubmission);
    }

    public void setRespondentReviewAppealResponseAdded(YesOrNo respondentReviewAppealResponseAdded) {
        this.respondentReviewAppealResponseAdded = Optional.ofNullable(respondentReviewAppealResponseAdded);
    }
}
