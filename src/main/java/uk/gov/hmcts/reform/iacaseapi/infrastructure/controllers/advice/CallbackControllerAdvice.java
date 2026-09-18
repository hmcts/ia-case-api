package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.DocumentServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataIntegrationException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchRepository;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ReferenceDataIntegrationException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice.model.ErrorResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.EmptySupplementaryInfoException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.LargeSupplementaryInfoException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.NullSupplementaryInfoException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice extends ResponseEntityExceptionHandler {

    private final ErrorResponseLogger errorResponseLogger;
    private final ErrorResponseBuilder errorResponseBuilder;

    public CallbackControllerAdvice(ErrorResponseLogger errorResponseLogger,
                                    ErrorResponseBuilder errorResponseBuilder) {
        this.errorResponseLogger = errorResponseLogger;
        this.errorResponseBuilder = errorResponseBuilder;
    }

    // --- 400 Bad Request Handlers ---

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<ErrorResponse> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.REQUIRED_FIELD_MISSING, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.REQUIRED_FIELD_MISSING, request, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.REQUIRED_FIELD_MISSING.getHttpStatus());
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalStateException(
        HttpServletRequest request,
        IllegalStateException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.INVALID_STATE, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INVALID_STATE, request, null);
        return new ResponseEntity<>(response, ErrorCode.INVALID_STATE.getHttpStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        HttpServletRequest request,
        IllegalArgumentException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.INVALID_ARGUMENT, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INVALID_ARGUMENT, request, null);
        return new ResponseEntity<>(response, ErrorCode.INVALID_ARGUMENT.getHttpStatus());
    }

    // --- 403 Forbidden Handlers ---

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(
        HttpServletRequest request,
        AccessDeniedException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.ACCESS_DENIED, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.ACCESS_DENIED, request, null);
        return new ResponseEntity<>(response, ErrorCode.ACCESS_DENIED.getHttpStatus());
    }

    @ExceptionHandler(NullSupplementaryInfoException.class)
    protected ResponseEntity<ErrorResponse> handleNullSupplementaryInfoException(
        HttpServletRequest request,
        NullSupplementaryInfoException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.SUPPLEMENTARY_INFO_ACCESS_DENIED, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.SUPPLEMENTARY_INFO_ACCESS_DENIED, request, null);
        return new ResponseEntity<>(response, ErrorCode.SUPPLEMENTARY_INFO_ACCESS_DENIED.getHttpStatus());
    }

    // --- 404 Not Found Handlers ---

    @ExceptionHandler(EmptySupplementaryInfoException.class)
    protected ResponseEntity<ErrorResponse> handleEmptySupplementaryInfoException(
        HttpServletRequest request,
        EmptySupplementaryInfoException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.SUPPLEMENTARY_INFO_NOT_FOUND, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.SUPPLEMENTARY_INFO_NOT_FOUND, request, null);
        return new ResponseEntity<>(response, ErrorCode.SUPPLEMENTARY_INFO_NOT_FOUND.getHttpStatus());
    }

    // --- 500 Internal Server Error Handlers ---

    @ExceptionHandler(ReferenceDataIntegrationException.class)
    protected ResponseEntity<ErrorResponse> handleReferenceDataIntegrationException(
        HttpServletRequest request,
        ReferenceDataIntegrationException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.REFERENCE_DATA_ERROR, request);
        errorResponseLogger.maybeLogException(ex.getCause());
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.REFERENCE_DATA_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.REFERENCE_DATA_ERROR.getHttpStatus());
    }

    @ExceptionHandler(AsylumCaseServiceResponseException.class)
    protected ResponseEntity<ErrorResponse> handleAsylumCaseServiceResponseException(
        HttpServletRequest request,
        AsylumCaseServiceResponseException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.ASYLUM_CASE_SERVICE_ERROR, request);
        errorResponseLogger.maybeLogException(ex.getCause());
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.ASYLUM_CASE_SERVICE_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.ASYLUM_CASE_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(CcdDataIntegrationException.class)
    protected ResponseEntity<ErrorResponse> handleCcdDataIntegrationException(
        HttpServletRequest request,
        CcdDataIntegrationException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.CCD_INTEGRATION_ERROR, request);
        errorResponseLogger.maybeLogException(ex.getCause());
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.CCD_INTEGRATION_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.CCD_INTEGRATION_ERROR.getHttpStatus());
    }

    @ExceptionHandler(CcdElasticSearchRepository.CcdSearchException.class)
    protected ResponseEntity<ErrorResponse> handleCcdSearchException(
        HttpServletRequest request,
        CcdElasticSearchRepository.CcdSearchException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.CCD_SEARCH_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.CCD_SEARCH_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.CCD_SEARCH_ERROR.getHttpStatus());
    }

    @ExceptionHandler(IdentityManagerResponseException.class)
    protected ResponseEntity<ErrorResponse> handleIdentityManagerResponseException(
        HttpServletRequest request,
        IdentityManagerResponseException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.IDENTITY_SERVICE_ERROR, request);
        errorResponseLogger.maybeLogException(ex.getCause());
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.IDENTITY_SERVICE_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.IDENTITY_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(DocumentServiceResponseException.class)
    protected ResponseEntity<ErrorResponse> handleDocumentServiceResponseException(
        HttpServletRequest request,
        DocumentServiceResponseException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.DOCUMENT_SERVICE_ERROR, request);
        errorResponseLogger.maybeLogException(ex.getCause());
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.DOCUMENT_SERVICE_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.DOCUMENT_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(LargeSupplementaryInfoException.class)
    protected ResponseEntity<ErrorResponse> handleLargeSupplementaryInfoException(
        HttpServletRequest request,
        LargeSupplementaryInfoException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.SUPPLEMENTARY_INFO_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.SUPPLEMENTARY_INFO_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.SUPPLEMENTARY_INFO_ERROR.getHttpStatus());
    }

    // --- Catch-all Handler ---

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleGenericException(
        HttpServletRequest request,
        Exception ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }
}
