package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class HomeOfficeApiResponseStatusTypeTest {

    private static final String HO_REFERENCE = "ABC123456";

    @Test
    void should_return_correct_status_codes_for_all_enum_values() {

        Assertions.assertEquals(-3, HomeOfficeApiResponseStatusType.OTHER_APPLICATION_DATA.getStatusCode());
        Assertions.assertEquals(-2, HomeOfficeApiResponseStatusType.NO_DATA.getStatusCode());
        Assertions.assertEquals(-1, HomeOfficeApiResponseStatusType.DID_NOT_RESPOND.getStatusCode());
        Assertions.assertEquals(200, HomeOfficeApiResponseStatusType.OK.getStatusCode());
        Assertions.assertEquals(400, HomeOfficeApiResponseStatusType.BAD_REQUEST.getStatusCode());
        Assertions.assertEquals(401, HomeOfficeApiResponseStatusType.NOT_AUTHENTICATED.getStatusCode());
        Assertions.assertEquals(403, HomeOfficeApiResponseStatusType.NOT_AUTHORISED.getStatusCode());
        Assertions.assertEquals(404, HomeOfficeApiResponseStatusType.NOT_FOUND.getStatusCode());
        Assertions.assertEquals(500, HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR.getStatusCode());
        Assertions.assertEquals(501, HomeOfficeApiResponseStatusType.NOT_IMPLEMENTED.getStatusCode());
        Assertions.assertEquals(502, HomeOfficeApiResponseStatusType.BAD_GATEWAY.getStatusCode());
        Assertions.assertEquals(503, HomeOfficeApiResponseStatusType.SERVICE_UNAVAILABLE.getStatusCode());
        Assertions.assertEquals(504, HomeOfficeApiResponseStatusType.GATEWAY_TIMEOUT.getStatusCode());
        Assertions.assertEquals(0, HomeOfficeApiResponseStatusType.UNKNOWN.getStatusCode());
    }

    @Test
    void should_replace_reference_in_user_facing_error_text_when_placeholder_present() {

        String text = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(HO_REFERENCE);

        Assertions.assertTrue(text.contains(HO_REFERENCE));
        Assertions.assertFalse(text.contains("XYZYX"));
    }

    @Test
    void should_not_modify_user_facing_error_text_when_placeholder_not_present() {

        String text = HomeOfficeApiResponseStatusType.BAD_REQUEST.getUserFacingErrorText(HO_REFERENCE);

        Assertions.assertEquals(
            "An error occurred.  Please report this to HMCTS.",
            text
        );
    }

    @Test
    void should_replace_reference_in_ho_integration_error_text_when_placeholder_present() {

        String text = HomeOfficeApiResponseStatusType.NOT_FOUND.getHoIntegrationErrorText(HO_REFERENCE);

        Assertions.assertEquals(
            "No application matching Home Office reference number " + HO_REFERENCE + " was found.",
            text
        );
    }

    @Test
    void should_return_empty_ho_integration_error_text_for_ok_status() {

        String text = HomeOfficeApiResponseStatusType.OK.getHoIntegrationErrorText(HO_REFERENCE);

        Assertions.assertEquals("", text);
    }

    @Test
    void should_return_expected_user_facing_text_for_server_errors() {

        String expected =
            "An error occurred.  Please try again in 15-20 minutes.  If it occurs again, please report this to HMCTS.";

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR.getUserFacingErrorText(HO_REFERENCE));

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.SERVICE_UNAVAILABLE.getUserFacingErrorText(HO_REFERENCE));

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.BAD_GATEWAY.getUserFacingErrorText(HO_REFERENCE));
    }

    @Test
    void should_return_expected_user_facing_text_for_client_errors() {

        String expected =
            "An error occurred.  Please report this to HMCTS.";

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.BAD_REQUEST.getUserFacingErrorText(HO_REFERENCE));

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.NOT_AUTHENTICATED.getUserFacingErrorText(HO_REFERENCE));

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.NOT_AUTHORISED.getUserFacingErrorText(HO_REFERENCE));

        Assertions.assertEquals(expected,
            HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(HO_REFERENCE));
    }

    @Test
    void should_return_correct_string_representation() {

        Assertions.assertEquals("otherApplicationData",
            HomeOfficeApiResponseStatusType.OTHER_APPLICATION_DATA.toString());

        Assertions.assertEquals("noData",
            HomeOfficeApiResponseStatusType.NO_DATA.toString());

        Assertions.assertEquals("didNotRespond",
            HomeOfficeApiResponseStatusType.DID_NOT_RESPOND.toString());

        Assertions.assertEquals("ok",
            HomeOfficeApiResponseStatusType.OK.toString());

        Assertions.assertEquals("badRequest",
            HomeOfficeApiResponseStatusType.BAD_REQUEST.toString());

        Assertions.assertEquals("notAuthenticated",
            HomeOfficeApiResponseStatusType.NOT_AUTHENTICATED.toString());

        Assertions.assertEquals("notAuthorised",
            HomeOfficeApiResponseStatusType.NOT_AUTHORISED.toString());

        Assertions.assertEquals("notFound",
            HomeOfficeApiResponseStatusType.NOT_FOUND.toString());

        Assertions.assertEquals("internalServerError",
            HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR.toString());

        Assertions.assertEquals("notImplemented",
            HomeOfficeApiResponseStatusType.NOT_IMPLEMENTED.toString());

        Assertions.assertEquals("badGateway",
            HomeOfficeApiResponseStatusType.BAD_GATEWAY.toString());

        Assertions.assertEquals("serviceUnavailable",
            HomeOfficeApiResponseStatusType.SERVICE_UNAVAILABLE.toString());

        Assertions.assertEquals("gatewayTimeout",
            HomeOfficeApiResponseStatusType.GATEWAY_TIMEOUT.toString());

        Assertions.assertEquals("unknown",
            HomeOfficeApiResponseStatusType.UNKNOWN.toString());
    }

    @Test
    void should_iterate_all_enum_values_and_validate_basic_contracts() {

        Set<Integer> codes = new HashSet<>();

        Arrays.stream(HomeOfficeApiResponseStatusType.values())
            .forEach(value -> {

                Assertions.assertNotNull(value.toString());
                Assertions.assertNotNull(value.getUserFacingErrorText(HO_REFERENCE));
                Assertions.assertNotNull(value.getHoIntegrationErrorText(HO_REFERENCE));

                codes.add(value.getStatusCode());
            });

        Assertions.assertEquals(HomeOfficeApiResponseStatusType.values().length, codes.size());
    }

    @Test
    void should_handle_different_reference_values() {

        String ref1 = "REF-111";
        String ref2 = "REF-222";

        String text1 = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(ref1);
        String text2 = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(ref2);

        Assertions.assertNotEquals(text1, text2);
        Assertions.assertTrue(text1.contains(ref1));
        Assertions.assertTrue(text2.contains(ref2));
    }
}