package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CoreCaseDataAccessException extends UnknownErrorCodeException {

    public CoreCaseDataAccessException(String reason) {
        super(AlertLevel.P2, reason);
    }
}
