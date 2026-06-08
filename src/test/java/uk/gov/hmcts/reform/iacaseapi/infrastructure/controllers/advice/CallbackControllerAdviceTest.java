package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CallbackControllerAdviceTest {

    @Mock
    HttpServletRequest request;
    @Mock
    private ErrorResponseLogger errorResponseLogger;
    private CallbackControllerAdvice callbackControllerAdvice;

    @BeforeEach
    public void setUp() {
        callbackControllerAdvice = new CallbackControllerAdvice(errorResponseLogger);

        when(request.getAttribute("CCDCaseId")).thenReturn("Case12345");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    }

    @Test
    void should_handle_required_missing_field_exception() {

        ResponseEntity<String> responseEntity = callbackControllerAdvice
            .handleExceptions(request, new RequiredFieldMissingException("submission out of time is a required field"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(responseEntity.getBody(), "submission out of time is a required field");
    }

    @Test
    void should_handle_illegal_state_exception() {

        ResponseEntity<String> responseEntity = callbackControllerAdvice
            .handleExceptions(request, new IllegalStateException("addCaseNoteSubject is not present"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(responseEntity.getBody(), "addCaseNoteSubject is not present");
    }

    @Test
    void should_handle_illegal_argument_exception() {

        ResponseEntity<String> responseEntity = callbackControllerAdvice
            .handleExceptions(request, new IllegalArgumentException("Hearing centre not found"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(responseEntity.getBody(), "Hearing centre not found");
    }
}
