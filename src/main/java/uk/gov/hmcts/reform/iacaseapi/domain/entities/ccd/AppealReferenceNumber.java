package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;

public class AppealReferenceNumber {

    final AsylumAppealType asylumAppealType;
    final String sequence;
    final String year;

    public AppealReferenceNumber(String appealReferenceNumber) {
        String[] bits = appealReferenceNumber.split("/");
        asylumAppealType = AsylumAppealType.valueOf(bits[0]);
        sequence = bits[1];
        year = bits[2];
    }

    public AppealReferenceNumber(AsylumAppealType type, String sequence, String year) {
        asylumAppealType = type;
        this.sequence = sequence;
        this.year = year;
    }

    public AsylumAppealType getType() {
        return asylumAppealType;
    }

    public String getSequence() {
        return sequence;
    }

    public String getYear() {
        return year;
    }

    @Override
    public String toString() {
        return asylumAppealType.name() + "/" + sequence + "/" + year;
    }
}

