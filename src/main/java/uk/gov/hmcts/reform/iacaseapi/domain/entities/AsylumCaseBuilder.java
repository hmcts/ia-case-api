package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
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
    private Optional<String> appellantLastName = Optional.empty();
    private Optional<String> appellantDateOfBirth = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> appellantNationalities = Optional.empty();
    private Optional<YesOrNo> appellantHasFixedAddress = Optional.empty();
    private Optional<AddressUk> appellantAddress = Optional.empty();
    private Optional<String> appealType = Optional.empty();
    private Optional<CheckValues<String>> appealGroundsProtection = Optional.empty();
    private Optional<CheckValues<String>> appealGroundsHumanRights = Optional.empty();
    private Optional<CheckValues<String>> appealGroundsRevocation = Optional.empty();
    private Optional<YesOrNo> hasNewMatters = Optional.empty();
    private Optional<String> newMatters = Optional.empty();
    private Optional<String> hasOtherAppeals = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> otherAppeals = Optional.empty();
    private Optional<String> legalRepReferenceNumber = Optional.empty();
    private Optional<String> appealReferenceNumber = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    private Optional<String> sendDirectionExplanation = Optional.empty();
    private Optional<Parties> sendDirectionParties = Optional.empty();
    private Optional<String> sendDirectionDateDue = Optional.empty();
    private Optional<List<IdValue<Direction>>> directions = Optional.empty();

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

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
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    private Optional<String> legalRepresentativeName = Optional.empty();
    private Optional<String> legalRepresentativeEmailAddress = Optional.empty();
    private Optional<List<IdValue<String>>> notificationsSent = Optional.empty();
    private Optional<YesOrNo> sendDirectionActionAvailable = Optional.empty();

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

    public Optional<String> getAppellantLastName() {
        return appellantLastName;
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

    public Optional<String> getAppealType() {
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

    public void setAppellantLastName(Optional<String> appellantLastName) {
        this.appellantLastName = appellantLastName;
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

    public AsylumCaseBuilder setAppealType(Optional<String> appealType) {
        this.appealType = appealType;
        return this;
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
    // case documents ...
    // -----------------------------------------------------------------------------


    public Optional<List<IdValue<DocumentWithMetadata>>> getLegalRepresentativeDocuments() {
        return legalRepresentativeDocuments;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getRespondentDocuments() {
        return respondentDocuments;
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

    public Optional<YesOrNo> getSendDirectionActionAvailable() {
        return sendDirectionActionAvailable;
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

    public void setSendDirectionActionAvailable(Optional<YesOrNo> sendDirectionActionAvailable) {
        this.sendDirectionActionAvailable = sendDirectionActionAvailable;
    }
}
