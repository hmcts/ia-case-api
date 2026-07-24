package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
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
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice.model.ErrorResponse;

@ExtendWith(MockitoExtension.class)
class CallbackControllerAdviceTest {

    @Mock
    HttpServletRequest request;
    @Mock
    private ErrorResponseLogger errorResponseLogger;
    @Mock
    private ErrorResponseBuilder errorResponseBuilder;
    private CallbackControllerAdvice callbackControllerAdvice;

    @BeforeEach
    public void setUp() {
        callbackControllerAdvice = new CallbackControllerAdvice(errorResponseLogger, errorResponseBuilder);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void should_handle_required_missing_field_exception() {
        String errorMessage = "submission out of time is a required field";
        ErrorResponse expectedResponse = buildErrorResponse(
            ErrorCode.REQUIRED_FIELD_MISSING, errorMessage);

        when(errorResponseBuilder.build(
            eq(ErrorCode.REQUIRED_FIELD_MISSING), eq(request), eq(errorMessage)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity = callbackControllerAdvice
            .handleRequiredFieldMissingException(request, new RequiredFieldMissingException(errorMessage));

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorCode.REQUIRED_FIELD_MISSING.getCode(), responseEntity.getBody().getErrorCode());
        assertEquals(errorMessage, responseEntity.getBody().getMessage());

        verify(errorResponseBuilder).logError(any(RequiredFieldMissingException.class),
            eq(ErrorCode.REQUIRED_FIELD_MISSING), eq(request));
    }

    @Test
    void should_handle_illegal_state_exception() {
        ErrorResponse expectedResponse = buildErrorResponse(
            ErrorCode.INVALID_STATE, ErrorCode.INVALID_STATE.getDefaultMessage());

        when(errorResponseBuilder.build(
            eq(ErrorCode.INVALID_STATE), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity = callbackControllerAdvice
            .handleIllegalStateException(request, new IllegalStateException("addCaseNoteSubject is not present"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorCode.INVALID_STATE.getCode(), responseEntity.getBody().getErrorCode());

        verify(errorResponseBuilder).logError(any(IllegalStateException.class),
            eq(ErrorCode.INVALID_STATE), eq(request));
    }

    @Test
    void should_handle_illegal_argument_exception() {
        ErrorResponse expectedResponse = buildErrorResponse(
            ErrorCode.INVALID_ARGUMENT, ErrorCode.INVALID_ARGUMENT.getDefaultMessage());

        when(errorResponseBuilder.build(
            eq(ErrorCode.INVALID_ARGUMENT), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity = callbackControllerAdvice
            .handleIllegalArgumentException(request, new IllegalArgumentException("Hearing centre not found"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorCode.INVALID_ARGUMENT.getCode(), responseEntity.getBody().getErrorCode());

        verify(errorResponseBuilder).logError(any(IllegalArgumentException.class),
            eq(ErrorCode.INVALID_ARGUMENT), eq(request));
    }

    @Test
    void should_handle_generic_exception() {
        ErrorResponse expectedResponse = buildErrorResponse(
            ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());

        when(errorResponseBuilder.build(
            eq(ErrorCode.INTERNAL_ERROR), eq(request), eq(null)))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity = callbackControllerAdvice
            .handleGenericException(request, new RuntimeException("Unexpected error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), responseEntity.getBody().getErrorCode());

        verify(errorResponseBuilder).logError(any(RuntimeException.class),
            eq(ErrorCode.INTERNAL_ERROR), eq(request));
    }

    private ErrorResponse buildErrorResponse(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(message)
            .timestamp(Instant.now())
            .requestId("test-correlation-id")
            .path("/asylum/ccdAboutToSubmit")
            .build();
    }
}
