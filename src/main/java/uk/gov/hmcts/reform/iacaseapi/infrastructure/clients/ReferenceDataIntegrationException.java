package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class ReferenceDataIntegrationException extends RuntimeException {

    public ReferenceDataIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
