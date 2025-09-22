package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import java.util.List;

public class HomeOfficeReferenceData {

    private String uan;
    private List<Appellant> appellants;

    public HomeOfficeReferenceData() {
    }

    public String getUan() {
        return uan;
    }

    public void setUan(String uan) {
        this.uan = uan;
    }

    public List<Appellant> getAppellants() {
        return appellants;
    }

    public void setAppellants(List<Appellant> appellants) {
        this.appellants = appellants;
    }

    public static class Appellant {

        private String familyName;
        private String givenNames;
        private String dateOfBirth;
        private String nationality;
        private boolean roa;

        public Appellant() {
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getGivenNames() {
            return givenNames;
        }

        public void setGivenNames(String givenNames) {
            this.givenNames = givenNames;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public boolean isRoa() {
            return roa;
        }

        public void setRoa(boolean roa) {
            this.roa = roa;
        }
    }
}