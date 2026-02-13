package uk.gov.hmcts.reform.iacaseapi.domain;

public class HomeOfficeMissingApplicationException extends RuntimeException {

    private final int httpStatus;

    public HomeOfficeMissingApplicationException(final int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}