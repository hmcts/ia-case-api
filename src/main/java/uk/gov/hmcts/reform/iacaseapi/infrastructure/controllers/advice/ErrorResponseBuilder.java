package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice.model.ErrorResponse;

@Slf4j
@Service
public class ErrorResponseBuilder {

    public ErrorResponse build(ErrorCode errorCode, HttpServletRequest request, String customMessage) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(customMessage != null ? customMessage : errorCode.getDefaultMessage())
            .timestamp(Instant.now())
            .requestId(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
            .path(request.getRequestURI())
            .build();
    }

    public ErrorResponse buildWithFieldErrors(ErrorCode errorCode,
                                              HttpServletRequest request,
                                              List<ErrorResponse.FieldError> fieldErrors) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(errorCode.getDefaultMessage())
            .timestamp(Instant.now())
            .requestId(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();
    }

    public void logError(Exception ex, ErrorCode errorCode, HttpServletRequest request) {
        String ccdCaseId = getCcdCaseId();
        log.error("Error [{}] for CCDCaseId: {}, path: {}, correlationId: {}",
            errorCode.getCode(),
            ccdCaseId,
            request.getRequestURI(),
            MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY),
            ex);
    }

    private String getCcdCaseId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            Object ccdCaseId = attrs.getAttribute("CCDCaseId", RequestAttributes.SCOPE_REQUEST);
            return ccdCaseId != null ? ccdCaseId.toString() : "unknown";
        }
        return "unknown";
    }
}
