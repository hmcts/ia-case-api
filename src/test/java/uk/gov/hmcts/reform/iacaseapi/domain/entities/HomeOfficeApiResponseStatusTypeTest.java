package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.getUserErrorHelpText;


@ExtendWith(MockitoExtension.class)
class HomeOfficeApiResponseStatusTypeTest {

    private static final String HO_REFERENCE = "ABC123456";

    @ParameterizedTest
    @MethodSource("enumMap")
    void has_correct_values(HomeOfficeApiResponseStatusType status, int expectedCode, String expectedName, String type, String expectedHoIntegrationErrorText) {
        assertEquals(expectedCode, status.getStatusCode());
        assertEquals(expectedName, status.toString());
        assertEquals(expectedHoIntegrationErrorText, status.getHoIntegrationErrorText("XYZYX"));
        String userFacingErrorText = status.getUserFacingErrorText("XYZYX", false, false);
        switch (type) {
            case "SERVER" -> assertThat(userFacingErrorText)
                .contains("An error occurred.")
                .contains("Please try again in 15-20 minutes")
                .doesNotContain("cannot be matched to a Home Office record");
            case "CLIENT" -> assertThat(userFacingErrorText)
                .contains("An error occurred.")
                .doesNotContain("Please try again in 15-20 minutes")
                .doesNotContain("cannot be matched to a Home Office record");
            case "USER" -> assertThat(userFacingErrorText)
                .doesNotContain("An error occurred.")
                .doesNotContain("Please try again in 15-20 minutes")
                .contains("cannot be matched to a Home Office record");
            case "NONE" -> assertThat(userFacingErrorText).isEmpty();
            default -> fail("Unexpected type: " + type);
        }
    }

    @Test
    void if_this_test_fails_it_is_because_enumMap_needs_updating_with_your_changes() {
        List<String> enumMapStrings = enumMap().map(arg -> arg.get()[0].toString()).toList();
        List<HomeOfficeApiResponseStatusType> missingEnums = Arrays.stream(HomeOfficeApiResponseStatusType.values())
            .filter(status -> !enumMapStrings.contains(status.toString())).toList();
        assertTrue(missingEnums.isEmpty(), "The following events are missing from the eventMapping method: " + missingEnums);
    }

    static Stream<Arguments> enumMap() {
        return Stream.of(
            Arguments.of(BADLY_FORMATTED_DATA, -4, "badlyFormattedData", "SERVER", "The Home Office validation API's response contained data that did not match the expected format."),
            Arguments.of(OTHER_APPLICATION_DATA, -3, "otherApplicationData", "SERVER", "The Home Office validation API's response contained data from a different application."),
            Arguments.of(NO_DATA, -2, "noData", "SERVER", "The Home Office validation API's response contained no data."),
            Arguments.of(DID_NOT_RESPOND, -1, "didNotRespond", "SERVER", "The Home Office validation API did not respond."),
            Arguments.of(OK, 200, "ok", "NONE", ""),
            Arguments.of(BAD_REQUEST, 400, "badRequest", "CLIENT", "The request to the Home Office validation API was not correctly formed."),
            Arguments.of(NOT_AUTHENTICATED, 401, "notAuthenticated", "CLIENT", "The request to the Home Office validation API could not be authenticated."),
            Arguments.of(NOT_AUTHORISED, 403, "notAuthorised", "CLIENT", "The request to the Home Office validation API was authenticated but not authorised."),
            Arguments.of(NOT_FOUND, 404, "notFound", "USER", "No application matching Home Office reference number XYZYX was found."),
            Arguments.of(INTERNAL_SERVER_ERROR, 500, "internalServerError", "SERVER", "The Home Office validation API was not available."),
            Arguments.of(NOT_IMPLEMENTED, 501, "notImplemented", "SERVER", "The Home Office validation API has not been implemented yet."),
            Arguments.of(BAD_GATEWAY, 502, "badGateway", "SERVER", "The Home Office validation API was not available due to a gateway error."),
            Arguments.of(SERVICE_UNAVAILABLE, 503, "serviceUnavailable", "SERVER", "The Home Office validation API was not available because the server could not process the request."),
            Arguments.of(GATEWAY_TIMEOUT, 504, "gatewayTimeout", "SERVER", "The Home Office validation API was not available due to a gateway time-out."),
            Arguments.of(UNKNOWN, 0, "unknown", "CLIENT", "The Home Office validation API did not return the required information for an unknown reason.")
        );
    }

    @Test
    void should_replace_reference_in_user_facing_error_text_when_placeholder_present() {

        String text = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(HO_REFERENCE, false, false);

        assertTrue(text.contains(HO_REFERENCE));
        assertFalse(text.contains("XYZYX"));
    }

    @ParameterizedTest
    @EnumSource(value = HomeOfficeApiResponseStatusType.class, names = {"NOT_FOUND"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_modify_user_facing_error_text_when_placeholder_not_present(HomeOfficeApiResponseStatusType status) {

        String text = status.getUserFacingErrorText(HO_REFERENCE, false, false);

        assertFalse(text.contains(HO_REFERENCE));
        assertFalse(text.contains("XYZYX"));
    }

    @Test
    void should_replace_reference_in_ho_integration_error_text_when_placeholder_present() {

        String text = HomeOfficeApiResponseStatusType.NOT_FOUND.getHoIntegrationErrorText(HO_REFERENCE);

        assertTrue(text.contains(HO_REFERENCE));
        assertFalse(text.contains("XYZYX"));
    }


    @ParameterizedTest
    @EnumSource(value = HomeOfficeApiResponseStatusType.class, names = {"NOT_FOUND"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_replace_reference_in_ho_integration_error_text_when_placeholder_not_present(HomeOfficeApiResponseStatusType status) {

        String text = status.getHoIntegrationErrorText(HO_REFERENCE);

        assertFalse(text.contains(HO_REFERENCE));
        assertFalse(text.contains("XYZYX"));
    }

    @ParameterizedTest
    @EnumSource(HomeOfficeApiResponseStatusType.class)
    void should_iterate_all_enum_values_and_validate_basic_contracts(HomeOfficeApiResponseStatusType value) {
        assertNotNull(value.toString());
        assertNotNull(value.getUserFacingErrorText(HO_REFERENCE, false, false));
        assertNotNull(value.getUserFacingErrorText(HO_REFERENCE, true, false));
        assertNotNull(value.getHoIntegrationErrorText(HO_REFERENCE));
    }

    @Test
    void should_handle_different_reference_values() {

        String ref1 = "REF-111";
        String ref2 = "REF-222";

        String text1 = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(ref1, false, false);
        String text2 = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(ref2, false, false);

        assertNotEquals(text1, text2);
        assertTrue(text1.contains(ref1));
        assertFalse(text1.contains(ref2));
        assertTrue(text2.contains(ref2));
        assertFalse(text2.contains(ref1));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getUserText_should_return_expected_text_for_onSubmit_true(boolean isAdmin) {
        String actualText = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(HO_REFERENCE, true, isAdmin);
        assertThat(actualText)
            .contains("The reference " + HO_REFERENCE + " cannot be matched to a Home Office record.")
            .doesNotContain("You should enter the UAN")
            .contains("You should edit the appeal and enter the UAN")
            .doesNotContain(getUserErrorHelpText(isAdmin));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getUserText_should_return_expected_text_for_onSubmit_false(boolean isAdmin) {
        String actualText = HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(HO_REFERENCE, false, isAdmin);
        assertThat(actualText)
            .contains("The reference " + HO_REFERENCE + " cannot be matched to a Home Office record.")
            .contains("You should enter the UAN")
            .doesNotContain("You should edit the appeal and enter the UAN")
            .contains(getUserErrorHelpText(isAdmin));
    }

    @Test
    void getClientText_should_return_expected_text_for_isAdmin_true() {
        String actualText = HomeOfficeApiResponseStatusType.BAD_REQUEST.getUserFacingErrorText(HO_REFERENCE, false, true);
        assertThat(actualText)
            .contains("An error occurred.")
            .contains("Please report this by raising a Halo ticket.")
            .doesNotContain("Please report this to HMCTS using the following contact details");
    }

    @Test
    void getClientText_should_return_expected_text_for_isAdmin_false() {
        String actualText = HomeOfficeApiResponseStatusType.BAD_REQUEST.getUserFacingErrorText(HO_REFERENCE, false, false);
        assertThat(actualText)
            .contains("An error occurred.")
            .doesNotContain("Please report this by raising a Halo ticket.")
            .contains("Please report this to HMCTS using the following contact details");
    }

    @Test
    void getServerText_should_return_expected_text_for_isAdmin_true() {
        String actualText = HomeOfficeApiResponseStatusType.GATEWAY_TIMEOUT.getUserFacingErrorText(HO_REFERENCE, false, true);
        assertThat(actualText)
            .contains("An error occurred.")
            .contains("please report this by raising a Halo ticket.")
            .doesNotContain("please report this to HMCTS using the following contact details");
    }

    @Test
    void getServerText_should_return_expected_text_for_isAdmin_false() {
        String actualText = HomeOfficeApiResponseStatusType.GATEWAY_TIMEOUT.getUserFacingErrorText(HO_REFERENCE, false, false);
        assertThat(actualText)
            .contains("An error occurred.")
            .doesNotContain("please report this by raising a Halo ticket.")
            .contains("please report this to HMCTS using the following contact details");
    }
}