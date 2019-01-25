package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AsylumCaseRetrievalException extends RuntimeException {

    public AsylumCaseRetrievalException(
        String message,
        Throwable cause) {

        super(message, cause);
    }
}
