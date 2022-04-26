package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

public class BailCaseServiceResponseException extends RuntimeException {

    public BailCaseServiceResponseException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
