package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class AsylumCaseRetrievalException extends RuntimeException {

    public AsylumCaseRetrievalException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
