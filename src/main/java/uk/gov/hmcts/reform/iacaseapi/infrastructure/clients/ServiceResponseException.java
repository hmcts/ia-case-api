package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class ServiceResponseException extends RuntimeException {

    public ServiceResponseException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
