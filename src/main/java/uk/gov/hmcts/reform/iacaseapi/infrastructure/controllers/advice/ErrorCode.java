package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", HttpStatus.BAD_REQUEST, "A required field is missing"),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_ARGUMENT("INVALID_ARGUMENT", HttpStatus.BAD_REQUEST, "Invalid argument provided"),
    INVALID_STATE("INVALID_STATE", HttpStatus.BAD_REQUEST, "Invalid application state"),

    // 403 Forbidden
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access to this resource is denied"),
    SUPPLEMENTARY_INFO_ACCESS_DENIED("SUPPLEMENTARY_INFO_ACCESS_DENIED", HttpStatus.FORBIDDEN,
        "Access to supplementary information is denied"),

    // 404 Not Found
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "The requested resource was not found"),
    SUPPLEMENTARY_INFO_NOT_FOUND("SUPPLEMENTARY_INFO_NOT_FOUND", HttpStatus.NOT_FOUND,
        "Supplementary information not found"),

    // 500 Internal Server Error
    CCD_INTEGRATION_ERROR("CCD_INTEGRATION_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Error communicating with CCD"),
    REFERENCE_DATA_ERROR("REFERENCE_DATA_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Error communicating with reference data service"),
    DOCUMENT_SERVICE_ERROR("DOCUMENT_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Error communicating with document service"),
    IDENTITY_SERVICE_ERROR("IDENTITY_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Error communicating with identity service"),
    ASYLUM_CASE_SERVICE_ERROR("ASYLUM_CASE_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Error processing asylum case"),
    CCD_SEARCH_ERROR("CCD_SEARCH_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Error searching CCD"),
    SUPPLEMENTARY_INFO_ERROR("SUPPLEMENTARY_INFO_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Error processing supplementary information"),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
