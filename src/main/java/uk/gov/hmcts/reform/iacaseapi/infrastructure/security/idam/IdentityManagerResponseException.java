package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IdentityManagerResponseException extends UnknownErrorCodeException {

    public IdentityManagerResponseException(
        AlertLevel alertLevel,
        String message,
        Throwable cause) {

        super(alertLevel, message, cause);

    }

}
