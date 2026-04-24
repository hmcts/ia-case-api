package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HomeOfficeApiResponseStatusType {

    OTHER_APPLICATION_DATA(-3, "otherApplicationData", UserFacingErrorText.SERVER, "The Home Office validation API's response contained data from a different application."),
    NO_DATA(-2, "noData", UserFacingErrorText.SERVER, "The Home Office validation API's response contained no data."),
    DID_NOT_RESPOND(-1, "didNotRespond", UserFacingErrorText.SERVER, "The Home Office validation API did not respond."),
    OK(200, "ok", "", ""),
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
    private final String userFacingErrorText;
    private final String hoIntegrationErrorText;

    private static final String REPLACEMENT_STRING = "XYZYX";

    HomeOfficeApiResponseStatusType(int statusCode, String name, String userFacingErrorText, String hoIntegrationErrorText) {
        this.statusCode = statusCode;
        this.name = name;
        this.userFacingErrorText = userFacingErrorText;
        this.hoIntegrationErrorText = hoIntegrationErrorText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getUserFacingErrorText(String hoReference) {
        return userFacingErrorText.replace(REPLACEMENT_STRING, hoReference);
    }

    public String getHoIntegrationErrorText(String hoReference) {
        return hoIntegrationErrorText.replace(REPLACEMENT_STRING, hoReference);
    }

    @Override
    public String toString() {
        return name;
    }

    private final class UserFacingErrorText {
        private static final String CLIENT = "An error occurred.  Please report this to HMCTS.";
        private static final String SERVER = "An error occurred.  Please try again in 15-20 minutes.  If it occurs again, please report this to HMCTS.";
        private static final String USER = "The reference XYZYX cannot be matched to a Home Office record.  You should enter the UAN or GWF reference exactly as it appears on the decision letter.  This can often be found in the ‘How to appeal’ section.";
    }    
}

