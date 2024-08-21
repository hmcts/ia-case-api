package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
    void should_allow_valid_phone_number() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("+911234567890"));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
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

    @Test
    void should_not_allow_invalid_international_phone_number() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("911234567890"));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors()
            .contains("International number must begin with + followed by region code."));
    }


    @Test
    void should_not_allow_invalid_uk_phone_number() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("0758999999"));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors()
            .contains("Phone number is invalid."));
    }

    @Test
    void should_allow_valid_international_phone_number() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("+911234567890"));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
    }


    @Test
    void should_allow_valid_uk_phone_number() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("07827297000"));
        PreSubmitCallbackResponse<AsylumCase> response = appellantPhoneNumberValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertEquals(0, response.getErrors().size());
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
