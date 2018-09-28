package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;

public class AsylumCase implements CaseData {

    //
    // @todo explore:
    //       appeal form and other models should be separate types
    //       upon submission of the associated event, the correct type can be selected to
    //       deserialize into. when sending back to CCD, a mapper would need to map back
    //       into this full CCD model. this can then be moved into infrastructure to keep
    //       the domain focussed.
    //

    // -----------------------------------------------------------------------------
    // internal model ...
    // -----------------------------------------------------------------------------

    private Optional<State> currentCaseState = Optional.empty();

    // -----------------------------------------------------------------------------
    // legal rep draft appeal form model ...
    // -----------------------------------------------------------------------------

    private String homeOfficeReferenceNumber = "";
    private String homeOfficeDecisionDate = "";

    private Optional<Name> appellantName = Optional.empty();
    private Optional<String> appellantNameForDisplay = Optional.empty();
    private Optional<String> appellantDob = Optional.empty();
    private Optional<List<IdValue<String>>> appellantNationalities = Optional.empty();
    private Optional<String> appellantNationalityContested = Optional.empty();

    private Optional<String> appellantHasFixedAddress = Optional.empty();
    private Optional<AddressUK> appellantAddress = Optional.empty();

    private Optional<String> appealReason = Optional.empty();
    private Optional<List<String>> appealGrounds = Optional.empty();

    private Optional<List<String>> newMatters = Optional.empty();
    private Optional<String> newMattersOther = Optional.empty();
    private Optional<Document> newMattersOtherDocument = Optional.empty();

    private Optional<String> otherAppeals = Optional.empty();
    private Optional<List<IdValue<String>>> otherAppealNumbers = Optional.empty();

    private Optional<String> legalRepReference = Optional.empty();

    private Optional<String> applicationOutOfTime = Optional.empty();
    private Optional<String> applicationOutOfTimeExplanation = Optional.empty();
    private Optional<Document> applicationOutOfTimeExplanationDocument = Optional.empty();

    private Optional<String> legalRepDeclaration = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer send home office evidence direction model ...
    // -----------------------------------------------------------------------------

    private Optional<Direction> homeOfficeEvidenceDirection = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer upload home office evidence model ...
    // -----------------------------------------------------------------------------

    private Optional<DocumentWithMetadata> homeOfficeEvidence = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer send build appeal direction model ...
    // -----------------------------------------------------------------------------

    private Optional<Direction> buildAppealDirection = Optional.empty();

    // -----------------------------------------------------------------------------
    // legal rep build appeal model ...
    // -----------------------------------------------------------------------------

    private Optional<Document> legalArgumentDocument = Optional.empty();
    private Optional<String> legalArgumentDescription = Optional.empty();
    private Optional<Documents> legalArgumentEvidence = Optional.empty();

    // -----------------------------------------------------------------------------
    // legal rep upload document model ...
    // -----------------------------------------------------------------------------

    private Optional<DocumentWithMetadata> document = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer send home office review direction model ...
    // -----------------------------------------------------------------------------

    private Optional<Direction> homeOfficeReviewDirection = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer send direction model ...
    // -----------------------------------------------------------------------------

    private Optional<Direction> direction = Optional.empty();

    // -----------------------------------------------------------------------------
    // legal rep request time extension model ...
    // -----------------------------------------------------------------------------

    private Optional<TimeExtensionRequest> timeExtensionRequest = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer review time extension model ...
    // -----------------------------------------------------------------------------

    private Optional<TimeExtension> timeExtensionUnderReview = Optional.empty();
    private Optional<TimeExtensionReview> timeExtensionReview = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer add case note model ...
    // -----------------------------------------------------------------------------

    private Optional<String> caseNoteType = Optional.empty();
    private Optional<String> caseNoteNote = Optional.empty();
    private Optional<Document> caseNoteDocument = Optional.empty();
    private Optional<String> caseNoteCorrespondent = Optional.empty();
    private Optional<String> caseNoteCorrespondenceDate = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer add home office response model ...
    // -----------------------------------------------------------------------------

    private Optional<Document> homeOfficeResponseDocument = Optional.empty();
    private Optional<String> homeOfficeResponseDescription = Optional.empty();
    private Optional<Documents> homeOfficeResponseEvidence = Optional.empty();
    private Optional<String> homeOfficeResponseDate = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer record listing model ...
    // -----------------------------------------------------------------------------

    private Optional<String> listingHearingCentre = Optional.empty();
    private Optional<String> listingHearingLength = Optional.empty();
    private Optional<String> listingHearingDate = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer create hearing summary model ...
    // -----------------------------------------------------------------------------

    private Optional<DocumentWithMetadata> hearingSummary = Optional.empty();

    // -----------------------------------------------------------------------------
    // case details (tabs) display model ...
    // -----------------------------------------------------------------------------

    private Optional<CaseDetails> caseDetails = Optional.empty();
    private Optional<Listing> listing = Optional.empty();

    private Optional<CaseArgument> caseArgument = Optional.empty();

    private Optional<Documents> documents = Optional.empty();

    private Optional<SentDirections> sentDirections = Optional.empty();

    private Optional<CaseNotes> caseNotes = Optional.empty();

    private Optional<TimeExtensions> timeExtensions = Optional.empty();

    // -----------------------------------------------------------------------------

    private AsylumCase() {
        // noop -- for deserializer
    }

    // -----------------------------------------------------------------------------
    // internal model ...
    // -----------------------------------------------------------------------------

    public Optional<State> getCurrentCaseState() {
        return currentCaseState;
    }

    public void setCurrentCaseState(State currentCaseState) {
        this.currentCaseState = Optional.ofNullable(currentCaseState);
    }

    // -----------------------------------------------------------------------------
    // legal rep draft appeal model ...
    // -----------------------------------------------------------------------------

    public String getHomeOfficeReferenceNumber() {
        return homeOfficeReferenceNumber;
    }

    public String getHomeOfficeDecisionDate() {
        return homeOfficeDecisionDate;
    }

    public Optional<Name> getAppellantName() {
        return appellantName;
    }

    public Optional<String> getAppellantNameForDisplay() {
        return appellantNameForDisplay;
    }

    public Optional<String> getAppellantDob() {
        return appellantDob;
    }

    public Optional<List<IdValue<String>>> getAppellantNationalities() {
        return appellantNationalities;
    }

    public Optional<String> getAppellantNationalityContested() {
        return appellantNationalityContested;
    }

    public Optional<String> getAppellantHasFixedAddress() {
        return appellantHasFixedAddress;
    }

    public Optional<AddressUK> getAppellantAddress() {
        return appellantAddress;
    }

    public Optional<String> getAppealReason() {
        return appealReason;
    }

    public Optional<List<String>> getAppealGrounds() {
        return appealGrounds;
    }

    public Optional<List<String>> getNewMatters() {
        return newMatters;
    }

    public Optional<String> getNewMattersOther() {
        return newMattersOther;
    }

    public Optional<Document> getNewMattersOtherDocument() {
        return newMattersOtherDocument;
    }

    public Optional<String> getOtherAppeals() {
        return otherAppeals;
    }

    public Optional<List<IdValue<String>>> getOtherAppealNumbers() {
        return otherAppealNumbers;
    }

    public Optional<String> getLegalRepReference() {
        return legalRepReference;
    }

    public Optional<String> getApplicationOutOfTime() {
        return applicationOutOfTime;
    }

    public Optional<String> getApplicationOutOfTimeExplanation() {
        return applicationOutOfTimeExplanation;
    }

    public Optional<Document> getApplicationOutOfTimeExplanationDocument() {
        return applicationOutOfTimeExplanationDocument;
    }

    public Optional<String> getLegalRepDeclaration() {
        return legalRepDeclaration;
    }

    public void setAppellantName(Name appellantName) {
        this.appellantName = Optional.ofNullable(appellantName);
    }

    public void setAppellantNameForDisplay(String appellantNameForDisplay) {
        this.appellantNameForDisplay = Optional.ofNullable(appellantNameForDisplay);
    }

    public void setAppellantDob(String appellantDob) {
        this.appellantDob = Optional.ofNullable(appellantDob);
    }

    public void setAppellantNationalities(List<IdValue<String>> appellantNationalities) {
        this.appellantNationalities = Optional.ofNullable(appellantNationalities);
    }

    public void setAppellantNationalityContested(String appellantNationalityContested) {
        this.appellantNationalityContested = Optional.ofNullable(appellantNationalityContested);
    }

    public void setAppellantHasFixedAddress(String appellantHasFixedAddress) {
        this.appellantHasFixedAddress = Optional.ofNullable(appellantHasFixedAddress);
    }

    public void setAppellantAddress(AddressUK appellantAddress) {
        this.appellantAddress = Optional.ofNullable(appellantAddress);
    }

    public void setAppealReason(String appealReason) {
        this.appealReason = Optional.ofNullable(appealReason);
    }

    public void setAppealGrounds(List<String> appealGrounds) {
        this.appealGrounds = Optional.ofNullable(appealGrounds);
    }

    public void setNewMatters(List<String> newMatters) {
        this.newMatters = Optional.ofNullable(newMatters);
    }

    public void setNewMattersOther(String newMattersOther) {
        this.newMattersOther = Optional.ofNullable(newMattersOther);
    }

    public void setNewMattersOtherDocument(Document newMattersOtherDocument) {
        this.newMattersOtherDocument = Optional.ofNullable(newMattersOtherDocument);
    }

    public void setOtherAppeals(String otherAppeals) {
        this.otherAppeals = Optional.ofNullable(otherAppeals);
    }

    public void setOtherAppealNumbers(List<IdValue<String>> otherAppealNumbers) {
        this.otherAppealNumbers = Optional.ofNullable(otherAppealNumbers);
    }

    public void setLegalRepReference(String legalRepReference) {
        this.legalRepReference = Optional.ofNullable(legalRepReference);
    }

    public void setApplicationOutOfTime(String applicationOutOfTime) {
        this.applicationOutOfTime = Optional.ofNullable(applicationOutOfTime);
    }

    public void setApplicationOutOfTimeExplanation(String applicationOutOfTimeExplanation) {
        this.applicationOutOfTimeExplanation = Optional.ofNullable(applicationOutOfTimeExplanation);
    }

    public void setApplicationOutOfTimeExplanationDocument(Document applicationOutOfTimeExplanationDocument) {
        this.applicationOutOfTimeExplanationDocument = Optional.ofNullable(applicationOutOfTimeExplanationDocument);
    }

    public void setLegalRepDeclaration(String legalRepDeclaration) {
        this.legalRepDeclaration = Optional.ofNullable(legalRepDeclaration);
    }

    // -----------------------------------------------------------------------------
    // case officer send home office evidence direction model ...
    // -----------------------------------------------------------------------------

    public Optional<Direction> getHomeOfficeEvidenceDirection() {
        return homeOfficeEvidenceDirection;
    }

    public void setHomeOfficeEvidenceDirection(Direction homeOfficeEvidenceDirection) {
        this.homeOfficeEvidenceDirection = Optional.ofNullable(homeOfficeEvidenceDirection);
    }

    public void clearHomeOfficeEvidenceDirection() {
        this.homeOfficeEvidenceDirection = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer upload home office evidence model ...
    // -----------------------------------------------------------------------------

    public Optional<DocumentWithMetadata> getHomeOfficeEvidence() {
        return homeOfficeEvidence;
    }

    public void setHomeOfficeEvidence(DocumentWithMetadata homeOfficeEvidence) {
        this.homeOfficeEvidence = Optional.ofNullable(homeOfficeEvidence);
    }

    public void clearHomeOfficeEvidence() {
        this.homeOfficeEvidence = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer send build appeal direction model ...
    // -----------------------------------------------------------------------------

    public Optional<Direction> getBuildAppealDirection() {
        return buildAppealDirection;
    }

    public void setBuildAppealDirection(Direction buildAppealDirection) {
        this.buildAppealDirection = Optional.ofNullable(buildAppealDirection);
    }

    public void clearBuildAppealDirection() {
        this.buildAppealDirection = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // legal rep build appeal model ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getLegalArgumentDocument() {
        return legalArgumentDocument;
    }

    public Optional<String> getLegalArgumentDescription() {
        return legalArgumentDescription;
    }

    public Optional<Documents> getLegalArgumentEvidence() {
        return legalArgumentEvidence;
    }

    public void setLegalArgumentDocument(Document legalArgumentDocument) {
        this.legalArgumentDocument = Optional.ofNullable(legalArgumentDocument);
    }

    public void setLegalArgumentDescription(String legalArgumentDescription) {
        this.legalArgumentDescription = Optional.ofNullable(legalArgumentDescription);
    }

    public void setLegalArgumentEvidence(Documents legalArgumentEvidence) {
        this.legalArgumentEvidence = Optional.ofNullable(legalArgumentEvidence);
    }

    // -----------------------------------------------------------------------------
    // legal rep upload document model ...
    // -----------------------------------------------------------------------------

    public Optional<DocumentWithMetadata> getDocument() {
        return document;
    }

    public void setDocument(DocumentWithMetadata document) {
        this.document = Optional.ofNullable(document);
    }

    public void clearDocument() {
        this.document = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer send home office review direction model ...
    // -----------------------------------------------------------------------------

    public Optional<Direction> getHomeOfficeReviewDirection() {
        return homeOfficeReviewDirection;
    }

    public void setHomeOfficeReviewDirection(Direction homeOfficeReviewDirection) {
        this.homeOfficeReviewDirection = Optional.ofNullable(homeOfficeReviewDirection);
    }

    public void clearHomeOfficeReviewDirection() {
        this.homeOfficeReviewDirection = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer send direction model ...
    // -----------------------------------------------------------------------------

    public Optional<Direction> getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = Optional.ofNullable(direction);
    }

    public void clearDirection() {
        this.direction = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // legal rep request time extension model ...
    // -----------------------------------------------------------------------------

    public Optional<TimeExtensionRequest> getTimeExtensionRequest() {
        return timeExtensionRequest;
    }

    public void setTimeExtensionRequest(TimeExtensionRequest timeExtensionRequest) {
        this.timeExtensionRequest = Optional.ofNullable(timeExtensionRequest);
    }

    public void clearTimeExtensionRequest() {
        this.timeExtensionRequest = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer review time extension model ...
    // -----------------------------------------------------------------------------

    public Optional<TimeExtension> getTimeExtensionUnderReview() {
        return timeExtensionUnderReview;
    }

    public Optional<TimeExtensionReview> getTimeExtensionReview() {
        return timeExtensionReview;
    }

    public void setTimeExtensionUnderReview(TimeExtension timeExtensionUnderReview) {
        this.timeExtensionUnderReview = Optional.ofNullable(timeExtensionUnderReview);
    }

    public void setTimeExtensionReview(TimeExtensionReview timeExtensionReview) {
        this.timeExtensionReview = Optional.ofNullable(timeExtensionReview);
    }

    public void clearTimeExtensionUnderReview() {
        this.timeExtensionUnderReview = Optional.empty();
    }

    public void clearTimeExtensionReview() {
        this.timeExtensionReview = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer add home office response model ...
    // -----------------------------------------------------------------------------

    public Optional<Document> getHomeOfficeResponseDocument() {
        return homeOfficeResponseDocument;
    }

    public Optional<String> getHomeOfficeResponseDescription() {
        return homeOfficeResponseDescription;
    }

    public Optional<Documents> getHomeOfficeResponseEvidence() {
        return homeOfficeResponseEvidence;
    }

    public Optional<String> getHomeOfficeResponseDate() {
        return homeOfficeResponseDate;
    }

    public void setHomeOfficeResponseDocument(Document homeOfficeResponseDocument) {
        this.homeOfficeResponseDocument = Optional.ofNullable(homeOfficeResponseDocument);
    }

    public void setHomeOfficeResponseDescription(String homeOfficeResponseDescription) {
        this.homeOfficeResponseDescription = Optional.ofNullable(homeOfficeResponseDescription);
    }

    public void setHomeOfficeResponseEvidence(Documents homeOfficeResponseEvidence) {
        this.homeOfficeResponseEvidence = Optional.ofNullable(homeOfficeResponseEvidence);
    }

    public void setHomeOfficeResponseDate(String homeOfficeResponseDate) {
        this.homeOfficeResponseDate = Optional.ofNullable(homeOfficeResponseDate);
    }

    public void clearHomeOfficeResponse() {
        this.homeOfficeResponseDocument = Optional.empty();
        this.homeOfficeResponseDescription = Optional.empty();
        this.homeOfficeResponseEvidence = Optional.empty();
        this.homeOfficeResponseDate = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer record listing model ...
    // -----------------------------------------------------------------------------

    public Optional<String> getListingHearingCentre() {
        return listingHearingCentre;
    }

    public Optional<String> getListingHearingLength() {
        return listingHearingLength;
    }

    public Optional<String> getListingHearingDate() {
        return listingHearingDate;
    }

    public void setListingHearingCentre(String listingHearingCentre) {
        this.listingHearingCentre = Optional.ofNullable(listingHearingCentre);
    }

    public void setListingHearingLength(String listingHearingLength) {
        this.listingHearingLength = Optional.ofNullable(listingHearingLength);
    }

    public void setListingHearingDate(String listingHearingDate) {
        this.listingHearingDate = Optional.ofNullable(listingHearingDate);
    }

    public void clearListing() {
        this.listingHearingCentre = Optional.empty();
        this.listingHearingLength = Optional.empty();
        this.listingHearingDate = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer create hearing summary model ...
    // -----------------------------------------------------------------------------

    public Optional<DocumentWithMetadata> getHearingSummary() {
        return hearingSummary;
    }

    public void setHearingSummary(DocumentWithMetadata hearingSummary) {
        this.hearingSummary = Optional.ofNullable(hearingSummary);
    }

    public void clearHearingSummary() {
        this.hearingSummary = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer add case note model ...
    // -----------------------------------------------------------------------------

    public Optional<String> getCaseNoteType() {
        return caseNoteType;
    }

    public Optional<String> getCaseNoteNote() {
        return caseNoteNote;
    }

    public Optional<Document> getCaseNoteDocument() {
        return caseNoteDocument;
    }

    public Optional<String> getCaseNoteCorrespondent() {
        return caseNoteCorrespondent;
    }

    public Optional<String> getCaseNoteCorrespondenceDate() {
        return caseNoteCorrespondenceDate;
    }

    public void setCaseNoteType(String caseNoteType) {
        this.caseNoteType = Optional.ofNullable(caseNoteType);
    }

    public void setCaseNoteNote(String caseNoteNote) {
        this.caseNoteNote = Optional.ofNullable(caseNoteNote);
    }

    public void setCaseNoteDocument(Document caseNoteDocument) {
        this.caseNoteDocument = Optional.ofNullable(caseNoteDocument);
    }

    public void setCaseNoteCorrespondent(String caseNoteCorrespondent) {
        this.caseNoteCorrespondent = Optional.ofNullable(caseNoteCorrespondent);
    }

    public void setCaseNoteCorrespondenceDate(String caseNoteCorrespondenceDate) {
        this.caseNoteCorrespondenceDate = Optional.ofNullable(caseNoteCorrespondenceDate);
    }

    public void clearCaseNote() {
        this.caseNoteType = Optional.empty();
        this.caseNoteNote = Optional.empty();
        this.caseNoteDocument = Optional.empty();
        this.caseNoteCorrespondent = Optional.empty();
        this.caseNoteCorrespondenceDate = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case details (tabs) display model ...
    // -----------------------------------------------------------------------------

    public Optional<CaseDetails> getCaseDetails() {
        return caseDetails;
    }

    public Optional<Listing> getListing() {
        return listing;
    }

    public Optional<CaseArgument> getCaseArgument() {
        return caseArgument;
    }

    public Optional<Documents> getDocuments() {
        return documents;
    }

    public Optional<SentDirections> getSentDirections() {
        return sentDirections;
    }

    public Optional<CaseNotes> getCaseNotes() {
        return caseNotes;
    }

    public Optional<TimeExtensions> getTimeExtensions() {
        return timeExtensions;
    }

    public void setCaseDetails(CaseDetails caseDetails) {
        this.caseDetails = Optional.ofNullable(caseDetails);
    }

    public void setListing(Listing listing) {
        this.listing = Optional.ofNullable(listing);
    }

    public void setCaseArgument(CaseArgument caseArgument) {
        this.caseArgument = Optional.ofNullable(caseArgument);
    }

    public void setDocuments(Documents documents) {
        this.documents = Optional.ofNullable(documents);
    }

    public void setSentDirections(SentDirections sentDirections) {
        this.sentDirections = Optional.ofNullable(sentDirections);
    }

    public void setCaseNotes(CaseNotes caseNotes) {
        this.caseNotes = Optional.ofNullable(caseNotes);
    }

    public void setTimeExtensions(TimeExtensions timeExtensions) {
        this.timeExtensions = Optional.ofNullable(timeExtensions);
    }
}
