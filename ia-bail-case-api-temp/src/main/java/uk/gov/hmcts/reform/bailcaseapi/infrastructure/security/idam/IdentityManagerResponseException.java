package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

public class IdentityManagerResponseException extends RuntimeException {

    public IdentityManagerResponseException(
        String message,
        Throwable cause) {
        super(message, cause);
    }
}
