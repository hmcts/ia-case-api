package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class HomeOfficeApiResponseStatusTypeTest {

    private static final String HO_REFERENCE = "ABC123456";

    @Test
    void should_return_correct_status_codes_for_all_enum_values() {

        assertEquals(-4, HomeOfficeApiResponseStatusType.BADLY_FORMATTED_DATA.getStatusCode());
        assertEquals(-3, HomeOfficeApiResponseStatusType.OTHER_APPLICATION_DATA.getStatusCode());
        assertEquals(-2, HomeOfficeApiResponseStatusType.NO_DATA.getStatusCode());
        assertEquals(-1, HomeOfficeApiResponseStatusType.DID_NOT_RESPOND.getStatusCode());
        assertEquals(200, HomeOfficeApiResponseStatusType.OK.getStatusCode());
        assertEquals(400, HomeOfficeApiResponseStatusType.BAD_REQUEST.getStatusCode());
        assertEquals(401, HomeOfficeApiResponseStatusType.NOT_AUTHENTICATED.getStatusCode());
        assertEquals(403, HomeOfficeApiResponseStatusType.NOT_AUTHORISED.getStatusCode());
        assertEquals(404, HomeOfficeApiResponseStatusType.NOT_FOUND.getStatusCode());
        assertEquals(500, HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals(501, HomeOfficeApiResponseStatusType.NOT_IMPLEMENTED.getStatusCode());
        assertEquals(502, HomeOfficeApiResponseStatusType.BAD_GATEWAY.getStatusCode());
        assertEquals(503, HomeOfficeApiResponseStatusType.SERVICE_UNAVAILABLE.getStatusCode());
        assertEquals(504, HomeOfficeApiResponseStatusType.GATEWAY_TIMEOUT.getStatusCode());
        assertEquals(0, HomeOfficeApiResponseStatusType.UNKNOWN.getStatusCode());
    }

    @Test
    void should_replace_reference_in_user_facing_error_text_when_placeholder_present() {

        String text = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(HO_REFERENCE, false);

        assertTrue(text.contains(HO_REFERENCE));
        assertFalse(text.contains("XYZYX"));
    }

    @Test
    void should_not_modify_user_facing_error_text_when_placeholder_not_present() {

        String text = HomeOfficeApiResponseStatusType.BAD_REQUEST.getUserFacingErrorText(HO_REFERENCE, false);

        assertEquals(
            "An error occurred. Please report this to HMCTS using the following contact details: Email contactia@justice.gov.uk or Telephone: 0300 123 1711.",
            text
        );
    }

    @Test
    void should_replace_reference_in_ho_integration_error_text_when_placeholder_present() {

        String text = HomeOfficeApiResponseStatusType.NOT_FOUND.getHoIntegrationErrorText(HO_REFERENCE);

        assertEquals(
            "No application matching Home Office reference number " + HO_REFERENCE + " was found.",
            text
        );
    }

    @Test
    void should_return_empty_ho_integration_error_text_for_ok_status() {

        String text = HomeOfficeApiResponseStatusType.OK.getHoIntegrationErrorText(HO_REFERENCE);

        assertEquals("", text);
    }

    @Test
    void should_return_expected_user_facing_text_for_server_errors() {

        String expected =
            "An error occurred. Please try again in 15-20 minutes. If it occurs again, please report this to HMCTS using the following contact details: Email contactia@justice.gov.uk or Telephone: 0300 123 1711.";

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR.getUserFacingErrorText(HO_REFERENCE, false));

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.SERVICE_UNAVAILABLE.getUserFacingErrorText(HO_REFERENCE, false));

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.BAD_GATEWAY.getUserFacingErrorText(HO_REFERENCE, false));
    }

    @Test
    void should_return_expected_user_facing_text_for_client_errors() {

        String expected =
            "An error occurred. Please report this to HMCTS using the following contact details: Email contactia@justice.gov.uk or Telephone: 0300 123 1711.";

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.BAD_REQUEST.getUserFacingErrorText(HO_REFERENCE, false));

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.NOT_AUTHENTICATED.getUserFacingErrorText(HO_REFERENCE, false));

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.NOT_AUTHORISED.getUserFacingErrorText(HO_REFERENCE, false));

        assertEquals(expected,
            HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(HO_REFERENCE, false));
    }

    @Test
    void should_return_correct_string_representation() {

        assertEquals("badlyFormattedData",
            HomeOfficeApiResponseStatusType.BADLY_FORMATTED_DATA.toString());

        assertEquals("otherApplicationData",
            HomeOfficeApiResponseStatusType.OTHER_APPLICATION_DATA.toString());

        assertEquals("noData",
            HomeOfficeApiResponseStatusType.NO_DATA.toString());

        assertEquals("didNotRespond",
            HomeOfficeApiResponseStatusType.DID_NOT_RESPOND.toString());

        assertEquals("ok",
            HomeOfficeApiResponseStatusType.OK.toString());

        assertEquals("badRequest",
            HomeOfficeApiResponseStatusType.BAD_REQUEST.toString());

        assertEquals("notAuthenticated",
            HomeOfficeApiResponseStatusType.NOT_AUTHENTICATED.toString());

        assertEquals("notAuthorised",
            HomeOfficeApiResponseStatusType.NOT_AUTHORISED.toString());

        assertEquals("notFound",
            HomeOfficeApiResponseStatusType.NOT_FOUND.toString());

        assertEquals("internalServerError",
            HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR.toString());

        assertEquals("notImplemented",
            HomeOfficeApiResponseStatusType.NOT_IMPLEMENTED.toString());

        assertEquals("badGateway",
            HomeOfficeApiResponseStatusType.BAD_GATEWAY.toString());

        assertEquals("serviceUnavailable",
            HomeOfficeApiResponseStatusType.SERVICE_UNAVAILABLE.toString());

        assertEquals("gatewayTimeout",
            HomeOfficeApiResponseStatusType.GATEWAY_TIMEOUT.toString());

        assertEquals("unknown",
            HomeOfficeApiResponseStatusType.UNKNOWN.toString());
    }

    @ParameterizedTest
    @EnumSource(HomeOfficeApiResponseStatusType.class)
    void should_iterate_all_enum_values_and_validate_basic_contracts(HomeOfficeApiResponseStatusType value) {
        assertNotNull(value.toString());
        assertNotNull(value.getUserFacingErrorText(HO_REFERENCE, false));
        assertNotNull(value.getUserFacingErrorText(HO_REFERENCE, true));
        assertNotNull(value.getHoIntegrationErrorText(HO_REFERENCE));
    }

    @Test
    void should_handle_different_reference_values() {

        String ref1 = "REF-111";
        String ref2 = "REF-222";

        String text1 = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(ref1, false);
        String text2 = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(ref2, false);

        assertNotEquals(text1, text2);
        assertTrue(text1.contains(ref1));
        assertTrue(text2.contains(ref2));
    }

    @Test
    void getUserFacingErrorText_should_replace_reference_and_modify_text_when_isOnSubmit_true() {
        String hoReference = "ABC123456";
        String result = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(hoReference, true);

        assertTrue(result.contains(hoReference));
        assertTrue(result.contains("You should edit the appeal and"));
        assertFalse(result.contains("XYZYX"));
    }

    @Test
    void getUserFacingErrorText_should_only_replace_reference_when_isOnSubmit_false() {
        String hoReference = "ABC123456";
        String result = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(hoReference, false);

        assertTrue(result.contains(hoReference));
        assertFalse(result.contains("You should edit the appeal and"));
        assertFalse(result.contains("XYZYX"));
    }
}