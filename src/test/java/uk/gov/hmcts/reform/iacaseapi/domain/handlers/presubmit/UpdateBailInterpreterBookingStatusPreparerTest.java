package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus.BOOKED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus.REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UpdateBailInterpreterBookingStatusPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateBailInterpreterBookingStatusPreparer updateBailInterpreterBookingStatusPreparer;

    @BeforeEach
    public void setUp() {
        updateBailInterpreterBookingStatusPreparer = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateBailInterpreterBookingStatusPreparer();
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_appellant_interpreter_booking_fields_if_appellant_requires_interpreters() {
        InterpreterLanguageRefData appellantSpokenRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("english", "english"), Collections.emptyList()),
            Collections.emptyList(),
            null);

        InterpreterLanguageRefData appellantSignRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("", ""), Collections.emptyList()),
            Collections.emptyList(),
            "test manual language");

        List<String> languageCategories = Arrays.asList("spokenLanguageInterpreter", "signLanguageInterpreter");

        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(languageCategories));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(appellantSpokenRefData));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(appellantSignRefData));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of("Test Appellant"));

        updateBailInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING), eq("Test Appellant - Appellant - english"));
        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS), eq(NOT_REQUESTED));

        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING), eq("Test Appellant - Appellant - test manual language"));
        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS), eq(NOT_REQUESTED));
    }

    @Test
    void should_not_set_appellant_interpreter_booking_status_fields_if_they_already_exist() {
        InterpreterLanguageRefData appellantRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("english", "english"), Collections.emptyList()),
            Collections.emptyList(),
            null);

        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(appellantRefData));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(appellantRefData));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS)).thenReturn(Optional.of(REQUESTED));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS)).thenReturn(Optional.of(BOOKED));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of("Test Appellant"));

        updateBailInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS), any());
        verify(asylumCase, never()).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS), any());
    }

    @Test
    void should_clear_appellant_interpreter_booking_status_fields_if_language_fields_not_set() {
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(new ArrayList<>()));

        updateBailInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        verify(asylumCase).clear(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING));
        verify(asylumCase).clear(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS));
        verify(asylumCase).clear(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING));
        verify(asylumCase).clear(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS));
    }

    @Test
    void should_set_witness_interpreter_booking_fields_if_witnesses_require_interpreters() {
        InterpreterLanguageRefData witnessSpokenRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("english", "english"), Collections.emptyList()),
            Collections.emptyList(),
            null);

        InterpreterLanguageRefData witnessSignRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("", ""), Collections.emptyList()),
            Collections.emptyList(),
            "test manual language");

        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(
            language -> when(asylumCase.read(language, InterpreterLanguageRefData.class))
                .thenReturn(Optional.of(witnessSpokenRefData))
        );

        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(
            language -> when(asylumCase.read(language, InterpreterLanguageRefData.class))
                .thenReturn(Optional.of(witnessSignRefData))
        );

        List<IdValue<WitnessDetails>> witnessDetailsList = getWitnessDetailsList();

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetailsList));

        updateBailInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        AtomicInteger index = new AtomicInteger();
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS.forEach(
            booking -> {
                WitnessDetails witnessDetails = witnessDetailsList.get(index.getAndIncrement()).getValue();
                String witnessName = witnessDetails.getWitnessName() + " " + witnessDetails.getWitnessFamilyName();
                verify(asylumCase).write(eq(booking), eq(witnessName + " - Witness - english"));
            }
        );

        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(asylumCase).write(eq(bookingStatus), eq(NOT_REQUESTED))
        );

        AtomicInteger index2 = new AtomicInteger();
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS.forEach(
            booking -> {
                WitnessDetails witnessDetails = witnessDetailsList.get(index2.getAndIncrement()).getValue();
                String witnessName = witnessDetails.getWitnessName() + " " + witnessDetails.getWitnessFamilyName();
                verify(asylumCase).write(eq(booking), eq(witnessName + " - Witness - test manual language"));
            }
        );

        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(asylumCase).write(eq(bookingStatus), eq(NOT_REQUESTED))
        );
    }

    @Test
    void should_not_set_witness_interpreter_booking_status_fields_if_they_already_exist() {
        InterpreterLanguageRefData witnessRefData = new InterpreterLanguageRefData(
            new DynamicList(new Value("english", "english"), Collections.emptyList()),
            Collections.emptyList(),
            null);

        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(
            language -> when(asylumCase.read(language, InterpreterLanguageRefData.class))
                .thenReturn(Optional.of(witnessRefData))
        );

        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(
            language -> when(asylumCase.read(language, InterpreterLanguageRefData.class))
                .thenReturn(Optional.of(witnessRefData))
        );

        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            booking -> when(asylumCase.read(booking)).thenReturn(Optional.of(BOOKED))
        );

        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            booking -> when(asylumCase.read(booking)).thenReturn(Optional.of(REQUESTED))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(getWitnessDetailsList()));

        updateBailInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(asylumCase, never()).write(eq(bookingStatus), any())
        );

        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(asylumCase, never()).write(eq(bookingStatus), any())
        );
    }

    @Test
    void should_clear_witnesses_interpreter_booking_status_fields_if_language_fields_not_set() {
        updateBailInterpreterBookingStatusPreparer.handle(ABOUT_TO_START, callback);

        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS.forEach(
            booking -> verify(asylumCase).clear(eq(booking))
        );

        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(asylumCase).clear(eq(bookingStatus))
        );

        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS.forEach(
            booking -> verify(asylumCase).clear(eq(booking))
        );

        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.forEach(
            bookingStatus -> verify(asylumCase).clear(eq(bookingStatus))
        );
    }

    @NotNull
    private static List<IdValue<WitnessDetails>> getWitnessDetailsList() {
        return List.of(
            new IdValue<>("1", new WitnessDetails("witness", "one")),
            new IdValue<>("2", new WitnessDetails("witness", "two")),
            new IdValue<>("3", new WitnessDetails("witness", "three")),
            new IdValue<>("4", new WitnessDetails("witness", "four")),
            new IdValue<>("5", new WitnessDetails("witness", "five")),
            new IdValue<>("6", new WitnessDetails("witness", "six")),
            new IdValue<>("7", new WitnessDetails("witness", "seven")),
            new IdValue<>("8", new WitnessDetails("witness", "eight")),
            new IdValue<>("9", new WitnessDetails("witness", "nine")),
            new IdValue<>("10", new WitnessDetails("witness", "ten"))
        );
    }


}
