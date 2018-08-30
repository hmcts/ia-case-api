package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AddressUK;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.MultiSelectValue;

public class AsylumCase implements CaseData {

    private String homeOfficeReferenceNumber = "";
    private String homeOfficeDecisionDate = "";

    private Optional<String> applicationOutOfTime = Optional.empty();
    private Optional<String> applicationOutOfTimeExplanation = Optional.empty();
    //private Optional<Document> applicationOutOfTimeExplanationDocument = Optional.empty();

    private Optional<Name> appellantName = Optional.empty();
    private Optional<String> appellantDob = Optional.empty();
    private Optional<List<MultiSelectValue>> appellantNationalities = Optional.empty();
    private Optional<String> appellantNationalityContested = Optional.empty();

    private Optional<AddressUK> appellantAddress = Optional.empty();
    private Optional<String> appellantHasNoFixedAbode = Optional.empty();

    private Optional<String> appealReason = Optional.empty();
    private Optional<List<String>> appealGrounds = Optional.empty();

    private Optional<String> refugeeConventionExplanation = Optional.empty();
    //private Optional<Document> refugeeConventionExplanationDocument = Optional.empty();

    private Optional<String> humanitarianProtectionExplanation = Optional.empty();
    //private Optional<Document> humanitarianProtectionExplanationDocument = Optional.empty();

    private Optional<String> humanRightsConventionExplanation = Optional.empty();
    //private Optional<Document> humanRightsConventionExplanationDocument = Optional.empty();

    private Optional<String> evidenceToUpload = Optional.empty();
    //private Optional<Document> evidenceDocument = Optional.empty();
    private Optional<String> evidenceLabel = Optional.empty();

    private Optional<List<String>> newMatters = Optional.empty();
    private Optional<String> newMattersOther = Optional.empty();
    //private Optional<Document> newMattersOtherDocument = Optional.empty();

    private Optional<String> personalVulnerabilitiesApply = Optional.empty();
    private Optional<List<String>> personalVulnerabilities = Optional.empty();
    private Optional<String> personalVulnerabilitiesOther = Optional.empty();

    private Optional<String> otherAppeals = Optional.empty();
    private Optional<List<MultiSelectValue>> otherAppealNumbers = Optional.empty();

    private Optional<String> legalRepDeclaration = Optional.empty();
    private Optional<String> legalRepReference = Optional.empty();

    private AsylumCase() {
        // noop -- for deserializer
    }

    public AsylumCase(
        String homeOfficeReferenceNumber,
        String homeOfficeDecisionDate,
        String applicationOutOfTime,
        String applicationOutOfTimeExplanation,
        Name appellantName,
        String appellantDob,
        List<MultiSelectValue> appellantNationalities,
        String appellantNationalityContested,
        AddressUK appellantAddress,
        String appellantHasNoFixedAbode,
        String appealReason,
        List<String> appealGrounds,
        String refugeeConventionExplanation,
        String humanitarianProtectionExplanation,
        String humanRightsConventionExplanation,
        String evidenceToUpload,
        String evidenceLabel,
        List<String> newMatters,
        String newMattersOther,
        String personalVulnerabilitiesApply,
        List<String> personalVulnerabilities,
        String personalVulnerabilitiesOther,
        String otherAppeals,
        List<MultiSelectValue> otherAppealNumbers,
        String legalRepDeclaration,
        String legalRepReference
    ) {
        this.homeOfficeReferenceNumber = homeOfficeReferenceNumber;
        this.homeOfficeDecisionDate = homeOfficeDecisionDate;
        this.applicationOutOfTime = Optional.ofNullable(applicationOutOfTime);
        this.applicationOutOfTimeExplanation = Optional.ofNullable(applicationOutOfTimeExplanation);
        this.appellantName = Optional.ofNullable(appellantName);
        this.appellantDob = Optional.ofNullable(appellantDob);
        this.appellantNationalities = Optional.ofNullable(appellantNationalities);
        this.appellantNationalityContested = Optional.ofNullable(appellantNationalityContested);
        this.appellantAddress = Optional.ofNullable(appellantAddress);
        this.appellantHasNoFixedAbode = Optional.ofNullable(appellantHasNoFixedAbode);
        this.appealReason = Optional.ofNullable(appealReason);
        this.appealGrounds = Optional.ofNullable(appealGrounds);
        this.refugeeConventionExplanation = Optional.ofNullable(refugeeConventionExplanation);
        this.humanitarianProtectionExplanation = Optional.ofNullable(humanitarianProtectionExplanation);
        this.humanRightsConventionExplanation = Optional.ofNullable(humanRightsConventionExplanation);
        this.evidenceToUpload = Optional.ofNullable(evidenceToUpload);
        this.evidenceLabel = Optional.ofNullable(evidenceLabel);
        this.newMatters = Optional.ofNullable(newMatters);
        this.newMattersOther = Optional.ofNullable(newMattersOther);
        this.personalVulnerabilitiesApply = Optional.ofNullable(personalVulnerabilitiesApply);
        this.personalVulnerabilities = Optional.ofNullable(personalVulnerabilities);
        this.personalVulnerabilitiesOther = Optional.ofNullable(personalVulnerabilitiesOther);
        this.otherAppeals = Optional.ofNullable(otherAppeals);
        this.otherAppealNumbers = Optional.ofNullable(otherAppealNumbers);
        this.legalRepDeclaration = Optional.ofNullable(legalRepDeclaration);
        this.legalRepReference = Optional.ofNullable(legalRepReference);
    }

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

    public Optional<Name> getAppellantName() {
        return appellantName;
    }

    public Optional<String> getAppellantDob() {
        return appellantDob;
    }

    public Optional<List<MultiSelectValue>> getAppellantNationalities() {
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

    public Optional<String> getHumanitarianProtectionExplanation() {
        return humanitarianProtectionExplanation;
    }

    public Optional<String> getHumanRightsConventionExplanation() {
        return humanRightsConventionExplanation;
    }

    public Optional<String> getEvidenceToUpload() {
        return evidenceToUpload;
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

    public Optional<List<MultiSelectValue>> getOtherAppealNumbers() {
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

    public void setAppellantName(Name appellantName) {
        this.appellantName = Optional.ofNullable(appellantName);
    }

    public void setAppellantDob(String appellantDob) {
        this.appellantDob = Optional.ofNullable(appellantDob);
    }

    public void setAppellantNationalities(List<MultiSelectValue> appellantNationalities) {
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

    public void setHumanitarianProtectionExplanation(String humanitarianProtectionExplanation) {
        this.humanitarianProtectionExplanation = Optional.ofNullable(humanitarianProtectionExplanation);
    }

    public void setHumanRightsConventionExplanation(String humanRightsConventionExplanation) {
        this.humanRightsConventionExplanation = Optional.ofNullable(humanRightsConventionExplanation);
    }

    public void setEvidenceToUpload(String evidenceToUpload) {
        this.evidenceToUpload = Optional.ofNullable(evidenceToUpload);
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

    public void setOtherAppealNumbers(List<MultiSelectValue> otherAppealNumbers) {
        this.otherAppealNumbers = Optional.ofNullable(otherAppealNumbers);
    }

    public void setLegalRepDeclaration(String legalRepDeclaration) {
        this.legalRepDeclaration = Optional.ofNullable(legalRepDeclaration);
    }

    public void setLegalRepReference(String legalRepReference) {
        this.legalRepReference = Optional.ofNullable(legalRepReference);
    }
}
