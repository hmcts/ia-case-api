package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;

public class CaseDetails {

    private Optional<String> caseStartDate = Optional.empty();
    private Optional<String> appellantName = Optional.empty();
    private Optional<String> appellantNationality = Optional.empty();
    private Optional<String> appellantDob = Optional.empty();
    private Optional<String> typeOfAppeal = Optional.empty();
    private Optional<String> legalRepName = Optional.empty();

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

    public Optional<String> getTypeOfAppeal() {
        return typeOfAppeal;
    }

    public Optional<String> getLegalRepName() {
        return legalRepName;
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

    public void setTypeOfAppeal(String typeOfAppeal) {
        this.typeOfAppeal = Optional.ofNullable(typeOfAppeal);
    }

    public void setLegalRepName(String legalRepName) {
        this.legalRepName = Optional.ofNullable(legalRepName);
    }
}
