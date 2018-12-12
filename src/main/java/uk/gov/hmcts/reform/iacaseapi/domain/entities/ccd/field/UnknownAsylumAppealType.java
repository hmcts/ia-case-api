package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

public class UnknownAsylumAppealType extends RuntimeException {
    public UnknownAsylumAppealType(String message) {
        super(message);
    }
}
