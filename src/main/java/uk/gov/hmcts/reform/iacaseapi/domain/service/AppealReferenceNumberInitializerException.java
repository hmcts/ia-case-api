package uk.gov.hmcts.reform.iacaseapi.domain.service;

public class AppealReferenceNumberInitializerException extends RuntimeException {

    public AppealReferenceNumberInitializerException(String reason) {
        super(reason);
    }

    public AppealReferenceNumberInitializerException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
