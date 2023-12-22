package uk.gov.hmcts.reform.iacaseapi.domain.service.exceptions;

public class CcdDataDeserializationException extends RuntimeException {

    public CcdDataDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
