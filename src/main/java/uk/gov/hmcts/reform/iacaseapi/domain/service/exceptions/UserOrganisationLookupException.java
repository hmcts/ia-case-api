package uk.gov.hmcts.reform.iacaseapi.domain.service.exceptions;

public class UserOrganisationLookupException extends RuntimeException {
    public UserOrganisationLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
