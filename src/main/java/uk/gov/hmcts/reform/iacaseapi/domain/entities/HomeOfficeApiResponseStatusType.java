package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.getUserErrorHelpText;

public enum HomeOfficeApiResponseStatusType {

    BADLY_FORMATTED_DATA(-4, "badlyFormattedData", UserFacingErrorText.SERVER, "The Home Office validation API's response contained data that did not match the expected format."),
    OTHER_APPLICATION_DATA(-3, "otherApplicationData", UserFacingErrorText.SERVER, "The Home Office validation API's response contained data from a different application."),
    NO_DATA(-2, "noData", UserFacingErrorText.SERVER, "The Home Office validation API's response contained no data."),
    DID_NOT_RESPOND(-1, "didNotRespond", UserFacingErrorText.SERVER, "The Home Office validation API did not respond."),
    OK(200, "ok", UserFacingErrorText.NONE, ""),
    BAD_REQUEST(400, "badRequest", UserFacingErrorText.CLIENT, "The request to the Home Office validation API was not correctly formed."),
    NOT_AUTHENTICATED(401, "notAuthenticated", UserFacingErrorText.CLIENT, "The request to the Home Office validation API could not be authenticated."),
    NOT_AUTHORISED(403, "notAuthorised", UserFacingErrorText.CLIENT, "The request to the Home Office validation API was authenticated but not authorised."),
    NOT_FOUND(404, "notFound", UserFacingErrorText.USER, "No application matching Home Office reference number XYZYX was found."),
    INTERNAL_SERVER_ERROR(500, "internalServerError", UserFacingErrorText.SERVER, "The Home Office validation API was not available."),
    NOT_IMPLEMENTED(501, "notImplemented", UserFacingErrorText.SERVER, "The Home Office validation API has not been implemented yet."),
    BAD_GATEWAY(502, "badGateway", UserFacingErrorText.SERVER, "The Home Office validation API was not available due to a gateway error."),
    SERVICE_UNAVAILABLE(503, "serviceUnavailable", UserFacingErrorText.SERVER, "The Home Office validation API was not available because the server could not process the request."),
    GATEWAY_TIMEOUT(504, "gatewayTimeout", UserFacingErrorText.SERVER, "The Home Office validation API was not available due to a gateway time-out."),

    @JsonEnumDefaultValue
    UNKNOWN(0, "unknown", UserFacingErrorText.CLIENT, "The Home Office validation API did not return the required information for an unknown reason.");

    @JsonValue
    private final int statusCode;
    private final String name;
    private final UserFacingErrorText userFacingErrorText;
    private final String hoIntegrationErrorText;

    private static final String REPLACEMENT_STRING = "XYZYX";

    HomeOfficeApiResponseStatusType(int statusCode, String name, UserFacingErrorText userFacingErrorText, String hoIntegrationErrorText) {
        this.statusCode = statusCode;
        this.name = name;
        this.userFacingErrorText = userFacingErrorText;
        this.hoIntegrationErrorText = hoIntegrationErrorText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getUserFacingErrorText(String hoReference, boolean isOnSubmit, boolean isAdmin) {
        return switch (this.userFacingErrorText) {
            case CLIENT -> getClientText(isAdmin);
            case SERVER -> getServerText(isAdmin);
            case USER -> getUserText(hoReference, isOnSubmit, isAdmin);
            case NONE -> "";
        };
    }

    public String getHoIntegrationErrorText(String hoReference) {
        return hoIntegrationErrorText.replace(REPLACEMENT_STRING, hoReference);
    }

    @Override
    public String toString() {
        return name;
    }

    private enum UserFacingErrorText {
        CLIENT, SERVER, USER, NONE
    }

    private static String getUserText(String hoReference, boolean isOnSubmit, boolean isAdmin) {
        String text;
        if (isOnSubmit) {
            text = "The reference XYZYX cannot be matched to a Home Office record. " +
                "You should edit the appeal and enter the UAN or GWF reference exactly as it appears on the decision letter. This can often be found in the 'How to appeal' section.";
        } else {
            text = "The reference XYZYX cannot be matched to a Home Office record. " +
                "You should enter the UAN or GWF reference exactly as it appears on the decision letter. This can often be found in the 'How to appeal' section. " +
                getUserErrorHelpText(isAdmin);
        }
        return text.replace(REPLACEMENT_STRING, hoReference);
    }

    private static String getClientText(boolean isAdmin) {
        if (isAdmin) {
            return "An error occurred. Please report this by raising a Halo ticket.";
        }
        return "An error occurred. Please report this to HMCTS using the following contact details: Email contactia@justice.gov.uk or Telephone: 0300 123 1711.";
    }

    private static String getServerText(boolean isAdmin) {
        if (isAdmin) {
            return "An error occurred. Please try again in 15-20 minutes. If it occurs again, please report this by raising a Halo ticket.";
        }
        return "An error occurred. Please try again in 15-20 minutes. If it occurs again, please report this to HMCTS using the following contact details: Email contactia@justice.gov.uk or Telephone: 0300 123 1711.";
    }
}

