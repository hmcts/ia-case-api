package uk.gov.hmcts.reform.iacaseapi.domain.exceptions;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
