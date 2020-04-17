package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class FeesRegisterException extends RuntimeException {

    public FeesRegisterException(String message, Throwable cause) {
        super(message, cause);
    }
}
