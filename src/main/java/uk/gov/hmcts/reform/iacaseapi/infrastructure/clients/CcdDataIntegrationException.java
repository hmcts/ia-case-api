package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class CcdDataIntegrationException extends RuntimeException {

    public CcdDataIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
