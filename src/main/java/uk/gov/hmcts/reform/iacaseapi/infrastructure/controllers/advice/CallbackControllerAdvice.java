package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.RequiredFieldMissingException;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackControllerAdvice.class);

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(HttpServletRequest request,
                                                                         RequiredFieldMissingException ex) {
        LOG.info("handling exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}