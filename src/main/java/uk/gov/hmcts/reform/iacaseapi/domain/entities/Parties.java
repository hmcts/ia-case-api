package uk.gov.hmcts.reform.iacaseapi.domain.entities;

public enum Parties {

    LEGAL_REPRESENTATIVE("legalRepresentative"),
    RESPONDENT("respondent"),
    BOTH("both");

    private final String id;

    Parties(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
