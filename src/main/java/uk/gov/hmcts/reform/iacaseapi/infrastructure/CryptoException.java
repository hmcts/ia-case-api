package uk.gov.hmcts.reform.iacaseapi.infrastructure;

public class CryptoException extends RuntimeException {

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}