package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

public class AsylumCaseServiceResponseException extends RuntimeException {

    public AsylumCaseServiceResponseException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
