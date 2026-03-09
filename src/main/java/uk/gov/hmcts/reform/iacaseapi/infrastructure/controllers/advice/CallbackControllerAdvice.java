package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ReferenceDataIntegrationException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice extends ResponseEntityExceptionHandler {

    private ErrorResponseLogger errorResponseLogger;

    public CallbackControllerAdvice(ErrorResponseLogger errorResponseLogger) {
        this.errorResponseLogger = errorResponseLogger;
    }

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        log.error("RequiredFieldMissingException for CCDCaseId: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
        ExceptionUtils.printRootCauseStackTrace(e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReferenceDataIntegrationException.class)
    protected ResponseEntity<String> handleReferenceDataIntegrationException(
        HttpServletRequest request,
        ReferenceDataIntegrationException e
    ) {
        log.error("ReferenceDataIntegrationException for CCDCaseId: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST));
        errorResponseLogger.maybeLogException(e.getCause());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
        AsylumCaseServiceResponseException.class,
        IllegalStateException.class,
        IllegalArgumentException.class
    })
    protected ResponseEntity<String> handleExceptions(
        HttpServletRequest request,
        Exception ex
    ) {
        log.error("Exception for CCDCaseId: {}; request URI: {}",
            RequestContextHolder.currentRequestAttributes().getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST), request.getRequestURI() + "?" + request.getQueryString());
        // Print elements of the stack trace that come from our code (otherwise it's enormous and unreadable)
        log.error(getAbbreviatedStackTrace(ex, 5));
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private String getAbbreviatedStackTrace(Exception ex, int numInitialLines) {
        String[] trace = ExceptionUtils.getRootCauseStackTrace(ex);
        StringBuilder sb = new StringBuilder();
        String lastLine = "";
        String continuationLine = "        ...";
        for (int i = 0; i < trace.length; i++) {
            if (i < numInitialLines || trace[i].contains("uk.gov.hmcts.reform")) {
                lastLine = trace[i];
                sb.append(lastLine + "\r\n");
            } else if (!lastLine.equals(continuationLine)) {
                lastLine = continuationLine;
                sb.append(lastLine + "\r\n");
            }
        }
        return sb.toString();
    }

}
