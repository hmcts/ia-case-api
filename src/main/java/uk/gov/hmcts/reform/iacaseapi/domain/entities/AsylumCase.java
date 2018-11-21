package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class AsylumCase implements CaseData {

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
    private Optional<List<IdValue<String>>> otherAppeals = Optional.empty();
    private Optional<String> legalRepReferenceNumber = Optional.empty();

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
        this.appellantLastName = asylumCaseBuilder.getAppellantLastName();
        this.appellantDateOfBirth = asylumCaseBuilder.getAppellantDateOfBirth();
        this.appellantNationalities = asylumCaseBuilder.getAppellantNationalities();
        this.appellantHasFixedAddress = asylumCaseBuilder.getAppellantHasFixedAddress();
        this.appellantAddress = asylumCaseBuilder.getAppellantAddress();
        this.appealType = asylumCaseBuilder.getAppealType();
        this.hasNewMatters = asylumCaseBuilder.getHasNewMatters();
        this.newMatters = asylumCaseBuilder.getNewMatters();
        this.hasOtherAppeals = asylumCaseBuilder.getHasOtherAppeals();
        this.otherAppeals = asylumCaseBuilder.getOtherAppeals();
        this.legalRepReferenceNumber = asylumCaseBuilder.getLegalRepReferenceNumber();
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

    public Optional<String> getAppellantLastName() {
        requireNonNull(appellantLastName);
        return appellantLastName;
    }

    public Optional<String> getAppellantDateOfBirth() {
        requireNonNull(appellantDateOfBirth);
        return appellantDateOfBirth;
    }

    public Optional<List<IdValue<Map<String, String>>>> getAppellantNationalities() {
        requireNonNull(appellantNationalities);
        return appellantNationalities;
    }

    public Optional<String> getAppellantHasFixedAddress() {
        requireNonNull(appellantHasFixedAddress);
        return appellantHasFixedAddress;
    }

    public Optional<AddressUk> getAppellantAddress() {
        requireNonNull(appellantAddress);
        return appellantAddress;
    }

    public Optional<String> getAppealType() {
        requireNonNull(appealType);
        return appealType;
    }

    public Optional<String> getHasNewMatters() {
        requireNonNull(hasNewMatters);
        return hasNewMatters;
    }

    public Optional<List<String>> getNewMatters() {
        requireNonNull(newMatters);
        return newMatters;
    }

    public Optional<String> getHasOtherAppeals() {
        requireNonNull(hasOtherAppeals);
        return hasOtherAppeals;
    }

    public Optional<List<IdValue<String>>> getOtherAppeals() {
        requireNonNull(otherAppeals);
        return otherAppeals;
    }

    public Optional<String> getLegalRepReferenceNumber() {
        requireNonNull(legalRepReferenceNumber);
        return legalRepReferenceNumber;
    }

    public void setHomeOfficeReferenceNumber(Optional<String> homeOfficeReferenceNumber) {
        requireNonNull(homeOfficeReferenceNumber);
        this.homeOfficeReferenceNumber = homeOfficeReferenceNumber;
    }

    public void setHomeOfficeReferenceNumber(String homeOfficeReferenceNumber) {
        this.homeOfficeReferenceNumber = Optional.ofNullable(homeOfficeReferenceNumber);
    }
}
