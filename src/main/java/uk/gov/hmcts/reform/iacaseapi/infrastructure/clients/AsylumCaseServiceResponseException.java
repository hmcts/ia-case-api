package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class AsylumCaseServiceResponseException extends RuntimeException {

    public AsylumCaseServiceResponseException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
