package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

public class CcdDataIntegrationException extends RuntimeException {

    public CcdDataIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
