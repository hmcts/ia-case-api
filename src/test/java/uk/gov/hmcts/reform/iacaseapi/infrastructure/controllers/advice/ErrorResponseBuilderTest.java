package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice.model.ErrorResponse;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id-123";
    private static final String TEST_REQUEST_URI = "/asylum/ccdAboutToSubmit";

    @Mock
    private HttpServletRequest request;

    private ErrorResponseBuilder errorResponseBuilder;

    @BeforeEach
    void setUp() {
        errorResponseBuilder = new ErrorResponseBuilder();
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
        when(request.getRequestURI()).thenReturn(TEST_REQUEST_URI);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_build_error_response_with_custom_message() {
        String customMessage = "Custom error message";

        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.REQUIRED_FIELD_MISSING, request, customMessage);

        assertEquals(ErrorCode.REQUIRED_FIELD_MISSING.getCode(), response.getErrorCode());
        assertEquals(customMessage, response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_REQUEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
        assertNull(response.getFieldErrors());
    }

    @Test
    void should_build_error_response_with_default_message_when_custom_message_is_null() {
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.INVALID_STATE, request, null);

        assertEquals(ErrorCode.INVALID_STATE.getCode(), response.getErrorCode());
        assertEquals(ErrorCode.INVALID_STATE.getDefaultMessage(), response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_REQUEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void should_build_error_response_with_field_errors() {
        List<ErrorResponse.FieldError> fieldErrors = List.of(
            ErrorResponse.FieldError.builder()
                .field("appellantName")
                .message("Appellant name is required")
                .build(),
            ErrorResponse.FieldError.builder()
                .field("caseReference")
                .message("Case reference must be numeric")
                .build()
        );

        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);

        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR.getDefaultMessage(), response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_REQUEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("appellantName", response.getFieldErrors().get(0).getField());
        assertEquals("Appellant name is required", response.getFieldErrors().get(0).getMessage());
    }

    @Test
    void should_log_error_with_ccd_case_id_from_request_attributes() {
        String ccdCaseId = "1234567890123456";
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        attrs.setAttribute("CCDCaseId", ccdCaseId, RequestAttributes.SCOPE_REQUEST);
        RequestContextHolder.setRequestAttributes(attrs);

        // This test verifies the method runs without exceptions
        errorResponseBuilder.logError(
            new RuntimeException("Test exception"),
            ErrorCode.INTERNAL_ERROR,
            request
        );

    }

    @Test
    void should_log_error_with_unknown_ccd_case_id_when_not_available() {
        RequestContextHolder.resetRequestAttributes();

        // verifies the method handles missing request attributes gracefully
        errorResponseBuilder.logError(
            new RuntimeException("Test exception"),
            ErrorCode.INTERNAL_ERROR,
            request
        );
    }

    @Test
    void should_handle_null_correlation_id_in_mdc() {
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.INTERNAL_ERROR, request, null);

        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getErrorCode());
        assertNull(response.getRequestId());
    }
}
