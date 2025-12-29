package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterBookingStatus.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UpdateInterpreterBookingStatusPreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    private UpdateInterpreterBookingStatusPreparer updateInterpreterBookingStatusPreparer;

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        updateInterpreterBookingStatusPreparer = new UpdateInterpreterBookingStatusPreparer();
    }

    @Test
    void should_set_applicant_interpreter_spoken_and_sign_booking_fields_if_required() {
        InterpreterLanguageRefData applicantSpokenRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("fre", "French"), Collections.emptyList()),
            "",
            null);

        InterpreterLanguageRefData applicantSignRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("", ""), Collections.emptyList()),
            "",
            "Manual sign language");

        List<String> languageCategories = Arrays.asList("spokenLanguageInterpreter", "signLanguageInterpreter");

        when(bailCase.read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(languageCategories));
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(applicantSpokenRefData));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(applicantSignRefData));

        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        PreSubmitCallbackResponse<BailCase> response = updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(bailCase, times(1)).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING, "John Doe - Applicant - French");
        verify(bailCase, times(1)).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, NOT_REQUESTED);
        verify(bailCase, times(1)).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING, "John Doe - Applicant - Manual sign language");
        verify(bailCase, times(1)).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, NOT_REQUESTED);
    }

    @Test
    void should_not_set_applicant_interpreter_booking_status_fields_if_they_already_exist() {
        InterpreterLanguageRefData applicantSpokenRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("fre", "French"), Collections.emptyList()),
            "",
            null);

        InterpreterLanguageRefData applicantSignRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("", ""), Collections.emptyList()),
            "",
            "Manual sign language");

        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(applicantSpokenRefData));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(applicantSignRefData));
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS)).thenReturn(Optional.of(REQUESTED));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS)).thenReturn(Optional.of(BOOKED));
        when(bailCase.read(APPLICANT_GIVEN_NAMES)).thenReturn(Optional.of("John"));
        when(bailCase.read(APPLICANT_FAMILY_NAME)).thenReturn(Optional.of("Doe"));

        PreSubmitCallbackResponse<BailCase> response = updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(bailCase, never()).write(eq(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS), any());
        verify(bailCase, never()).write(eq(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS), any());
    }

    @Test
    void should_clear_applicant_interpreter_booking_status_fields_if_language_fields_not_set() {
        PreSubmitCallbackResponse<BailCase> response = updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(bailCase, times(1)).clear(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING);
        verify(bailCase, times(1)).clear(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        verify(bailCase, times(1)).clear(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING);
        verify(bailCase, times(1)).clear(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
    }

    @Test
    void should_set_fcs_interpreter_spoken_and_sign_booking_fields_if_required() {
        List<String> languageCategories = Arrays.asList("spokenLanguageInterpreter", "signLanguageInterpreter");

        when(bailCase.read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.empty());

        when(bailCase.read(FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(languageCategories));
        when(bailCase.read(FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(languageCategories));
        when(bailCase.read(FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(languageCategories));
        when(bailCase.read(FCS4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(languageCategories));

        InterpreterLanguageRefData fcsSignRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("", ""), Collections.emptyList()),
            "",
            "Manual sign language");

        InterpreterLanguageRefData fcsSpokenRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("fre", "French"), Collections.emptyList()),
            "",
            null);

        FCS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(
            language -> when(bailCase.read(language, InterpreterLanguageRefData.class)).thenReturn(Optional.of(fcsSpokenRefData))
        );

        FCS_N_INTERPRETER_SIGN_LANGUAGE.forEach(
            language -> when(bailCase.read(language, InterpreterLanguageRefData.class)).thenReturn(Optional.of(fcsSignRefData))
        );

        when(bailCase.read(SUPPORTER_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Supporter One"));
        when(bailCase.read(SUPPORTER_FAMILY_NAMES, String.class)).thenReturn(Optional.of("Surname"));
        when(bailCase.read(SUPPORTER_2_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Supporter Two"));
        when(bailCase.read(SUPPORTER_2_FAMILY_NAMES, String.class)).thenReturn(Optional.of("Surname"));
        when(bailCase.read(SUPPORTER_3_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Supporter Three"));
        when(bailCase.read(SUPPORTER_3_FAMILY_NAMES, String.class)).thenReturn(Optional.of("Surname"));
        when(bailCase.read(SUPPORTER_4_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Supporter Four"));
        when(bailCase.read(SUPPORTER_4_FAMILY_NAMES, String.class)).thenReturn(Optional.of("Surname"));

        PreSubmitCallbackResponse<BailCase> response = updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);

        verify(bailCase, times(1)).write(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1, "Supporter One Surname - FCS - French");
        verify(bailCase, times(1)).write(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2, "Supporter Two Surname - FCS - French");
        verify(bailCase, times(1)).write(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3, "Supporter Three Surname - FCS - French");
        verify(bailCase, times(1)).write(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4, "Supporter Four Surname - FCS - French");

        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(bailCase, times(1)).write(bookingStatus, NOT_REQUESTED)
        );

        verify(bailCase, times(1)).write(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1, "Supporter One Surname - FCS - Manual sign language");
        verify(bailCase, times(1)).write(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2, "Supporter Two Surname - FCS - Manual sign language");
        verify(bailCase, times(1)).write(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3, "Supporter Three Surname - FCS - Manual sign language");
        verify(bailCase, times(1)).write(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4, "Supporter Four Surname - FCS - Manual sign language");

        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(bailCase, times(1)).write(bookingStatus, NOT_REQUESTED)
        );
    }

    @Test
    void should_not_set_fcs_interpreter_booking_status_fields_if_they_already_exist() {
        InterpreterLanguageRefData applicantSpokenRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("fre", "French"), Collections.emptyList()),
            "",
            null);

        InterpreterLanguageRefData applicantSignRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("", ""), Collections.emptyList()),
            "",
            "Manual sign language");

        FCS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(
            language -> when(bailCase.read(language, InterpreterLanguageRefData.class))
                .thenReturn(Optional.of(applicantSpokenRefData))
        );

        FCS_N_INTERPRETER_SIGN_LANGUAGE.forEach(
            language -> when(bailCase.read(language, InterpreterLanguageRefData.class))
                .thenReturn(Optional.of(applicantSignRefData))
        );

        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            booking -> when(bailCase.read(booking)).thenReturn(Optional.of(BOOKED))
        );

        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            booking -> when(bailCase.read(booking)).thenReturn(Optional.of(REQUESTED))
        );

        updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(bailCase, never()).write(eq(bookingStatus), any())
        );

        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(bailCase, never()).write(eq(bookingStatus), any())
        );
    }

    @Test
    void should_clear_fcs_interpreter_booking_status_fields_if_language_fields_not_set() {
        updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS.forEach(
            booking -> verify(bailCase).clear(eq(booking))
        );

        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(bailCase).clear(eq(bookingStatus))
        );

        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS.forEach(
            booking -> verify(bailCase).clear(eq(booking))
        );

        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(bailCase).clear(eq(bookingStatus))
        );
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = updateInterpreterBookingStatusPreparer.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START
                    && (callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(
            () -> updateInterpreterBookingStatusPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> updateInterpreterBookingStatusPreparer
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateInterpreterBookingStatusPreparer
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateInterpreterBookingStatusPreparer
            .handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateInterpreterBookingStatusPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
