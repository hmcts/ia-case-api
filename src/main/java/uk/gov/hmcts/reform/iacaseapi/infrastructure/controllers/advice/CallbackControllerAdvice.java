package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ReferenceDataIntegrationException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice {

    private ErrorResponseLogger errorResponseLogger;

    public CallbackControllerAdvice(ErrorResponseLogger errorResponseLogger) {
        this.errorResponseLogger = errorResponseLogger;
    }

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        log.info("handling exception: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReferenceDataIntegrationException.class)
    protected ResponseEntity<String> handleReferenceDataIntegrationException(
        HttpServletRequest request,
        ReferenceDataIntegrationException e
    ) {
        errorResponseLogger.maybeLogException(e.getCause());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
