package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class CaseDetails {

    private Optional<String> caseStartDate = Optional.empty();
    private Optional<String> appellantName = Optional.empty();
    private Optional<String> appellantNationality = Optional.empty();
    private Optional<String> appellantDob = Optional.empty();
    private Optional<String> legalRepName = Optional.empty();
    private Optional<String> legalRepContactDetails = Optional.empty();

    public Optional<String> getCaseStartDate() {
        return caseStartDate;
    }

    public Optional<String> getAppellantName() {
        return appellantName;
    }

    public Optional<String> getAppellantNationality() {
        return appellantNationality;
    }

    public Optional<String> getAppellantDob() {
        return appellantDob;
    }

    public Optional<String> getLegalRepName() {
        return legalRepName;
    }

    public Optional<String> getLegalRepContactDetails() {
        return legalRepContactDetails;
    }

    public void setCaseStartDate(String caseStartDate) {
        this.caseStartDate = Optional.ofNullable(caseStartDate);
    }

    public void setAppellantName(String appellantName) {
        this.appellantName = Optional.ofNullable(appellantName);
    }

    public void setAppellantNationality(String appellantNationality) {
        this.appellantNationality = Optional.ofNullable(appellantNationality);
    }

    public void setAppellantDob(String appellantDob) {
        this.appellantDob = Optional.ofNullable(appellantDob);
    }

    public void setLegalRepName(String legalRepName) {
        this.legalRepName = Optional.ofNullable(legalRepName);
    }

    public void setLegalRepContactDetails(String legalRepContactDetails) {
        this.legalRepContactDetails = Optional.ofNullable(legalRepContactDetails);
    }
}
