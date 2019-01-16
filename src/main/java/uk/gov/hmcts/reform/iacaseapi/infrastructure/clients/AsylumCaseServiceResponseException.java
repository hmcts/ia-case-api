package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AsylumCaseServiceResponseException extends UnknownErrorCodeException {

    public AsylumCaseServiceResponseException(
        AlertLevel alertLevel,
        String message,
        Throwable cause) {

        super(alertLevel, message, cause);

    }

}
