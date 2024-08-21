package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppellantPhoneNumberValidatorTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final AppellantPhoneNumberValidator appellantPhoneNumberValidator = new AppellantPhoneNumberValidator();

    @BeforeEach
    void setup() {
        when(callback.getPageId()).thenReturn("appellantContactPreference");
        when(callback.getEvent()).thenReturn(START_APPEAL);
    }

    @Test
    void should_do_nothing_for_empty_phone_number() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.empty());
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
        assertThat(response).usingRecursiveComparison().isEqualTo(new PreSubmitCallbackResponse<>(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025550125", "9876543210", "412345678", "612345678", "1712345678"})
    void should_not_allow_valid_international_phone_number_without_plus_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors()
            .contains("Phone number is invalid. International numbers must begin with + followed by region code."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025559999","9876500000","412345000","612340000","1712349999"})
    void should_not_allow_invalid_international_phone_number_without_plus_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors()
            .contains("Phone number is invalid. International numbers must begin with + followed by region code."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+12345","+9991234567","+44 0000000000","+1 1234567","+91 000000000"})
    void should_not_allow_invalid_international_phone_number_with_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains("Phone number is invalid."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+1 2025550125","+91 9876543210","+61 412345678","+33 612345678","+49 1712345678"})
    void should_allow_valid_international_phone_number_with_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+44 7827292000","+44 7712345678","+44 7901122334","+44 7471234567","+44 7523456789"})
    void should_allow_valid_uk_phone_number_with_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
    }


    @ParameterizedTest
    @ValueSource(strings = {"+44 0000000000","+44 1234567","+44 78000000000","+44 555123456","+44 4000000001"})
    void should_not_allow_invalid_uk_phone_number_with_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains("Phone number is invalid."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"07827292000","07123456789","07827297000","07911223344","07523456789"})
    void should_allow_valid_uk_phone_number_without_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"078272","071","075123","079000000000","07654321"})
    void should_not_allow_invalid_uk_phone_number_without_region_code(String phoneNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(phoneNumber));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors()
            .contains("Phone number is invalid. International numbers must begin with + followed by region code."));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appellantPhoneNumberValidator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> appellantPhoneNumberValidator.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantPhoneNumberValidator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantPhoneNumberValidator.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void throws_if_cannot_candle_callback() {
        assertThatThrownBy(() -> appellantPhoneNumberValidator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        List<Event> validEvents = List.of(START_APPEAL, EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT);
        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            boolean canHandle = appellantPhoneNumberValidator.canHandle(callbackStage, callback);
            if (validEvents.contains(event)
                && callbackStage == MID_EVENT) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }
}
