package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AddressUK;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class AsylumCase implements CaseData {

    //
    // @todo explore:
    //       appeal form and other models should be separate types
    //       upon submission of the associated event, the correct type can be selected to
    //       deserialize into.
    //

    // -----------------------------------------------------------------------------
    // legal rep draft appeal form model ...
    // -----------------------------------------------------------------------------

    private String homeOfficeReferenceNumber = "";
    private String homeOfficeDecisionDate = "";

    private Optional<String> applicationOutOfTime = Optional.empty();
    private Optional<String> applicationOutOfTimeExplanation = Optional.empty();
    private Optional<Document> applicationOutOfTimeExplanationDocument = Optional.empty();

    private Optional<Name> appellantName = Optional.empty();
    private Optional<String> appellantNameForDisplay = Optional.empty();
    private Optional<String> appellantDob = Optional.empty();
    private Optional<List<IdValue<String>>> appellantNationalities = Optional.empty();
    private Optional<String> appellantNationalityContested = Optional.empty();

    private Optional<AddressUK> appellantAddress = Optional.empty();
    private Optional<String> appellantHasNoFixedAbode = Optional.empty();

    private Optional<String> appealReason = Optional.empty();
    private Optional<List<String>> appealGrounds = Optional.empty();

    private Optional<String> refugeeConventionExplanation = Optional.empty();
    private Optional<Document> refugeeConventionExplanationDocument = Optional.empty();

    private Optional<String> humanitarianProtectionExplanation = Optional.empty();
    private Optional<Document> humanitarianProtectionExplanationDocument = Optional.empty();

    private Optional<String> humanRightsConventionExplanation = Optional.empty();
    private Optional<Document> humanRightsConventionExplanationDocument = Optional.empty();

    private Optional<String> evidenceToUpload = Optional.empty();
    private Optional<Document> evidenceDocument = Optional.empty();
    private Optional<String> evidenceLabel = Optional.empty();

    private Optional<List<String>> newMatters = Optional.empty();
    private Optional<String> newMattersOther = Optional.empty();
    private Optional<Document> newMattersOtherDocument = Optional.empty();

    private Optional<String> personalVulnerabilitiesApply = Optional.empty();
    private Optional<List<String>> personalVulnerabilities = Optional.empty();
    private Optional<String> personalVulnerabilitiesOther = Optional.empty();

    private Optional<String> otherAppeals = Optional.empty();
    private Optional<List<IdValue<String>>> otherAppealNumbers = Optional.empty();

    private Optional<String> legalRepDeclaration = Optional.empty();
    private Optional<String> legalRepReference = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer update summary model ...
    // -----------------------------------------------------------------------------

    private Optional<GroundsForAppeal> groundsForAppeal = Optional.empty();
    private Optional<Issues> issues = Optional.empty();

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
    // legal rep add written legal argument model ...
    // -----------------------------------------------------------------------------

    private Optional<WrittenLegalArgument> writtenLegalArgument = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer add correspondence model ...
    // -----------------------------------------------------------------------------

    private Optional<Correspondence> correspondence = Optional.empty();

    // -----------------------------------------------------------------------------
    // case details (tabs) display model ...
    // -----------------------------------------------------------------------------

    private Optional<CaseDetails> caseDetails = Optional.empty();
    private Optional<CaseArgument> caseArgument = Optional.empty();
    private Optional<Documents> documents = Optional.empty();
    private Optional<SentDirections> sentDirections = Optional.empty();
    private Optional<TimeExtensions> timeExtensions = Optional.empty();
    private Optional<Correspondences> correspondences = Optional.empty();

    // -----------------------------------------------------------------------------

    private AsylumCase() {
        // noop -- for deserializer
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

    public Optional<String> getApplicationOutOfTime() {
        return applicationOutOfTime;
    }

    public Optional<String> getApplicationOutOfTimeExplanation() {
        return applicationOutOfTimeExplanation;
    }

    public Optional<Document> getApplicationOutOfTimeExplanationDocument() {
        return applicationOutOfTimeExplanationDocument;
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

    public Optional<AddressUK> getAppellantAddress() {
        return appellantAddress;
    }

    public Optional<String> getAppellantHasNoFixedAbode() {
        return appellantHasNoFixedAbode;
    }

    public Optional<String> getAppealReason() {
        return appealReason;
    }

    public Optional<List<String>> getAppealGrounds() {
        return appealGrounds;
    }

    public Optional<String> getRefugeeConventionExplanation() {
        return refugeeConventionExplanation;
    }

    public Optional<Document> getRefugeeConventionExplanationDocument() {
        return refugeeConventionExplanationDocument;
    }

    public Optional<String> getHumanitarianProtectionExplanation() {
        return humanitarianProtectionExplanation;
    }

    public Optional<Document> getHumanitarianProtectionExplanationDocument() {
        return humanitarianProtectionExplanationDocument;
    }

    public Optional<String> getHumanRightsConventionExplanation() {
        return humanRightsConventionExplanation;
    }

    public Optional<Document> getHumanRightsConventionExplanationDocument() {
        return humanRightsConventionExplanationDocument;
    }

    public Optional<String> getEvidenceToUpload() {
        return evidenceToUpload;
    }

    public Optional<Document> getEvidenceDocument() {
        return evidenceDocument;
    }

    public Optional<String> getEvidenceLabel() {
        return evidenceLabel;
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

    public Optional<String> getPersonalVulnerabilitiesApply() {
        return personalVulnerabilitiesApply;
    }

    public Optional<List<String>> getPersonalVulnerabilities() {
        return personalVulnerabilities;
    }

    public Optional<String> getPersonalVulnerabilitiesOther() {
        return personalVulnerabilitiesOther;
    }

    public Optional<String> getOtherAppeals() {
        return otherAppeals;
    }

    public Optional<List<IdValue<String>>> getOtherAppealNumbers() {
        return otherAppealNumbers;
    }

    public Optional<String> getLegalRepDeclaration() {
        return legalRepDeclaration;
    }

    public Optional<String> getLegalRepReference() {
        return legalRepReference;
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

    public void setAppellantAddress(AddressUK appellantAddress) {
        this.appellantAddress = Optional.ofNullable(appellantAddress);
    }

    public void setAppellantHasNoFixedAbode(String appellantHasNoFixedAbode) {
        this.appellantHasNoFixedAbode = Optional.ofNullable(appellantHasNoFixedAbode);
    }

    public void setAppealReason(String appealReason) {
        this.appealReason = Optional.ofNullable(appealReason);
    }

    public void setAppealGrounds(List<String> appealGrounds) {
        this.appealGrounds = Optional.ofNullable(appealGrounds);
    }

    public void setRefugeeConventionExplanation(String refugeeConventionExplanation) {
        this.refugeeConventionExplanation = Optional.ofNullable(refugeeConventionExplanation);
    }

    public void setRefugeeConventionExplanationDocument(Document refugeeConventionExplanationDocument) {
        this.refugeeConventionExplanationDocument = Optional.ofNullable(refugeeConventionExplanationDocument);
    }

    public void setHumanitarianProtectionExplanation(String humanitarianProtectionExplanation) {
        this.humanitarianProtectionExplanation = Optional.ofNullable(humanitarianProtectionExplanation);
    }

    public void setHumanitarianProtectionExplanationDocument(Document humanitarianProtectionExplanationDocument) {
        this.humanitarianProtectionExplanationDocument = Optional.ofNullable(humanitarianProtectionExplanationDocument);
    }

    public void setHumanRightsConventionExplanation(String humanRightsConventionExplanation) {
        this.humanRightsConventionExplanation = Optional.ofNullable(humanRightsConventionExplanation);
    }

    public void setHumanRightsConventionExplanationDocument(Document humanRightsConventionExplanationDocument) {
        this.humanRightsConventionExplanationDocument = Optional.ofNullable(humanRightsConventionExplanationDocument);
    }

    public void setEvidenceToUpload(String evidenceToUpload) {
        this.evidenceToUpload = Optional.ofNullable(evidenceToUpload);
    }

    public void setEvidenceDocument(Document evidenceDocument) {
        this.evidenceDocument = Optional.ofNullable(evidenceDocument);
    }

    public void setEvidenceLabel(String evidenceLabel) {
        this.evidenceLabel = Optional.ofNullable(evidenceLabel);
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

    public void setPersonalVulnerabilitiesApply(String personalVulnerabilitiesApply) {
        this.personalVulnerabilitiesApply = Optional.ofNullable(personalVulnerabilitiesApply);
    }

    public void setPersonalVulnerabilities(List<String> personalVulnerabilities) {
        this.personalVulnerabilities = Optional.ofNullable(personalVulnerabilities);
    }

    public void setPersonalVulnerabilitiesOther(String personalVulnerabilitiesOther) {
        this.personalVulnerabilitiesOther = Optional.ofNullable(personalVulnerabilitiesOther);
    }

    public void setOtherAppeals(String otherAppeals) {
        this.otherAppeals = Optional.ofNullable(otherAppeals);
    }

    public void setOtherAppealNumbers(List<IdValue<String>> otherAppealNumbers) {
        this.otherAppealNumbers = Optional.ofNullable(otherAppealNumbers);
    }

    public void setLegalRepDeclaration(String legalRepDeclaration) {
        this.legalRepDeclaration = Optional.ofNullable(legalRepDeclaration);
    }

    public void setLegalRepReference(String legalRepReference) {
        this.legalRepReference = Optional.ofNullable(legalRepReference);
    }

    // -----------------------------------------------------------------------------
    // case officer update summary model ...
    // -----------------------------------------------------------------------------

    public Optional<GroundsForAppeal> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public void setGroundsForAppeal(GroundsForAppeal groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }

    public void clearGroundsForAppeal() {
        this.groundsForAppeal = Optional.empty();
    }

    public Optional<Issues> getIssues() {
        return issues;
    }

    public void setIssues(Issues issues) {
        this.issues = Optional.ofNullable(issues);
    }

    public void clearIssues() {
        this.issues = Optional.empty();
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
    // legal rep add written legal argument model ...
    // -----------------------------------------------------------------------------

    public Optional<WrittenLegalArgument> getWrittenLegalArgument() {
        return writtenLegalArgument;
    }

    public void setWrittenLegalArgument(WrittenLegalArgument writtenLegalArgument) {
        this.writtenLegalArgument = Optional.ofNullable(writtenLegalArgument);
    }

    public void clearWrittenLegalArgument() {
        this.writtenLegalArgument = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case officer add correspondence model ...
    // -----------------------------------------------------------------------------

    public Optional<Correspondence> getCorrespondence() {
        return correspondence;
    }

    public void setCorrespondence(Correspondence correspondence) {
        this.correspondence = Optional.ofNullable(correspondence);
    }

    public void clearCorrespondence() {
        this.correspondence = Optional.empty();
    }

    // -----------------------------------------------------------------------------
    // case details (tabs) display model ...
    // -----------------------------------------------------------------------------

    public Optional<CaseDetails> getCaseDetails() {
        return caseDetails;
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

    public Optional<TimeExtensions> getTimeExtensions() {
        return timeExtensions;
    }

    public Optional<Correspondences> getCorrespondences() {
        return correspondences;
    }

    public void setCaseDetails(CaseDetails caseDetails) {
        this.caseDetails = Optional.ofNullable(caseDetails);
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

    public void setTimeExtensions(TimeExtensions timeExtensions) {
        this.timeExtensions = Optional.ofNullable(timeExtensions);
    }

    public void setCorrespondences(Correspondences correspondences) {
        this.correspondences = Optional.ofNullable(correspondences);
    }
}
