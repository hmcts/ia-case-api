package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
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
        this.sendDirectionActionAvailable = asylumCaseBuilder.getSendDirectionActionAvailable();
        this.sendDirectionExplanation = asylumCaseBuilder.getSendDirectionExplanation();
        this.sendDirectionParties = asylumCaseBuilder.getSendDirectionParties();
        this.sendDirectionDateDue = asylumCaseBuilder.getSendDirectionDateDue();
        this.directions = asylumCaseBuilder.getDirections();
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

    public Optional<YesOrNo> getAppellantHasFixedAddress() {
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

    public void setHomeOfficeReferenceNumber(String homeOfficeReferenceNumber) {
        this.homeOfficeReferenceNumber = Optional.ofNullable(homeOfficeReferenceNumber);
    }

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    public Optional<YesOrNo> getSendDirectionActionAvailable() {
        requireNonNull(sendDirectionActionAvailable);
        return sendDirectionActionAvailable;
    }

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

    public void setSendDirectionActionAvailable(YesOrNo sendDirectionActionAvailable) {
        this.sendDirectionActionAvailable = Optional.ofNullable(sendDirectionActionAvailable);
    }

    public void setDirections(List<IdValue<Direction>> directions) {
        this.directions = Optional.ofNullable(directions);
    }
}
