package uk.gov.hmcts.reform.iacaseapi.domain.exceptions;

public class AsylumCaseRetrievalException extends RuntimeException {
    public AsylumCaseRetrievalException(String reason, Throwable exp) {
        super(reason, exp);
    }
}
