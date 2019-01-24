package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AppealReferenceNumberInitializerException extends UnknownErrorCodeException {

    public AppealReferenceNumberInitializerException(Throwable cause) {
        super(AlertLevel.P2, "Appeal reference number could not be initialised", cause);
    }
}
