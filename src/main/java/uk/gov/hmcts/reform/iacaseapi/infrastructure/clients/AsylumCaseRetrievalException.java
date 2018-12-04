package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class AsylumCaseRetrievalException extends RuntimeException {

    public AsylumCaseRetrievalException(String reason) {
        super(reason);
    }

    public AsylumCaseRetrievalException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
