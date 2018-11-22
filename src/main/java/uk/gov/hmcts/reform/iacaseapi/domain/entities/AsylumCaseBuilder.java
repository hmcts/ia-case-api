package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Component
public class AsylumCaseBuilder {

    // -----------------------------------------------------------------------------
    // legal rep appeal form model ...
    // -----------------------------------------------------------------------------

    private Optional<String> homeOfficeReferenceNumber = Optional.empty();
    private Optional<String> homeOfficeDecisionDate = Optional.empty();
    private Optional<String> appellantTitle = Optional.empty();
    private Optional<String> appellantGivenNames = Optional.empty();
    private Optional<String> appellantLastName = Optional.empty();
    private Optional<String> appellantDateOfBirth = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> appellantNationalities = Optional.empty();
    private Optional<String> appellantHasFixedAddress = Optional.empty();
    private Optional<AddressUk> appellantAddress = Optional.empty();
    private Optional<String> appealType = Optional.empty();
    private Optional<String> hasNewMatters = Optional.empty();
    private Optional<List<String>> newMatters = Optional.empty();
    private Optional<String> hasOtherAppeals = Optional.empty();
    private Optional<List<IdValue<Map<String, String>>>> otherAppeals = Optional.empty();
    private Optional<String> legalRepReferenceNumber = Optional.empty();

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

    public Optional<String> getAppellantHasFixedAddress() {
        return appellantHasFixedAddress;
    }

    public Optional<AddressUk> getAppellantAddress() {
        return appellantAddress;
    }

    public Optional<String> getAppealType() {
        return appealType;
    }

    public Optional<String> getHasNewMatters() {
        return hasNewMatters;
    }

    public Optional<List<String>> getNewMatters() {
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

    public void setAppellantHasFixedAddress(Optional<String> appellantHasFixedAddress) {
        this.appellantHasFixedAddress = appellantHasFixedAddress;
    }

    public void setAppellantAddress(Optional<AddressUk> appellantAddress) {
        this.appellantAddress = appellantAddress;
    }

    public void setAppealType(Optional<String> appealType) {
        this.appealType = appealType;
    }

    public void setHasNewMatters(Optional<String> hasNewMatters) {
        this.hasNewMatters = hasNewMatters;
    }

    public void setNewMatters(Optional<List<String>> newMatters) {
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
}
