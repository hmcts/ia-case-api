package uk.gov.hmcts.reform.iacaseapi.domain.service.exceptions;

public class UserLookupException extends RuntimeException {

    public UserLookupException(String message) {
        super(message);
    }
}
