package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AppealReferenceNumberInitializerException extends UnknownErrorCodeException {
    public AppealReferenceNumberInitializerException(String reason) {
        super(AlertLevel.P2, reason);
    }

    public AppealReferenceNumberInitializerException(String reason, Throwable cause) {
        super(AlertLevel.P2, reason, cause);
    }
}
