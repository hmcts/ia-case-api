package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
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
    private Optional<YesOrNo> hasNewMatters = Optional.empty();
    private Optional<String> newMatters = Optional.empty();
    private Optional<String> hasOtherAppeals = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> otherAppeals = Optional.empty();
    private Optional<String> legalRepReferenceNumber = Optional.empty();

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    private Optional<YesOrNo> sendDirectionActionAvailable = Optional.empty();
    private Optional<String> sendDirectionExplanation = Optional.empty();
    private Optional<Parties> sendDirectionParties = Optional.empty();
    private Optional<String> sendDirectionDateDue = Optional.empty();
    private Optional<List<IdValue<Direction>>> directions = Optional.empty();

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    private Optional<List<IdValue<DocumentWithDescription>>> uploadRespondentEvidence = Optional.empty();
    private Optional<List<IdValue<DocumentWithMetadata>>> respondentDocuments = Optional.empty();

    public AsylumCase build() {
        return new AsylumCase(this);
    }

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

    public Optional<YesOrNo> getSendDirectionActionAvailable() {
        return sendDirectionActionAvailable;
    }

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

    public Optional<List<IdValue<DocumentWithDescription>>> getUploadRespondentEvidence() {
        return uploadRespondentEvidence;
    }

    public Optional<List<IdValue<DocumentWithMetadata>>> getRespondentDocuments() {
        return respondentDocuments;
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

    public void setAppealType(Optional<String> appealType) {
        this.appealType = appealType;
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

    public void setSendDirectionActionAvailable(Optional<YesOrNo> sendDirectionActionAvailable) {
        this.sendDirectionActionAvailable = sendDirectionActionAvailable;
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

    public void setUploadRespondentEvidence(Optional<List<IdValue<DocumentWithDescription>>> uploadRespondentEvidence) {
        this.uploadRespondentEvidence = uploadRespondentEvidence;
    }

    public void setRespondentDocuments(Optional<List<IdValue<DocumentWithMetadata>>> respondentDocuments) {
        this.respondentDocuments = respondentDocuments;
    }
}
