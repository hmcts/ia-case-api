package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_10_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_4_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_5_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_6_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_7_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_8_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_9_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_10;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_5;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_6;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_7;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_8;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_9;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_10;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_5;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_6;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_7;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_8;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_9;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.buildWitnessFullName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateInterpreterBookingStatusPreparer implements PreSubmitCallbackHandler<AsylumCase> {
    public static final String SPOKEN_LANGUAGE_INTERPRETER = "spokenLanguageInterpreter";
    public static final String SIGN_LANGUAGE_INTERPRETER = "signLanguageInterpreter";
    public static String APPELLANT = "Appellant";
    public static String WITNESS = "Witness";
    private AsylumCase asylumCase;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        this.asylumCase = callback.getCaseDetails().getCaseData();

        populateOrClearAppellantSignAndSpokenInterpreterBookingFields();

        populateOrClearWitnessesSignAndSpokenInterpreterBookingFields();

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void populateOrClearWitnessesSignAndSpokenInterpreterBookingFields() {
        Optional<List<IdValue<WitnessDetails>>> optionalWitnesses = asylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> witnessesDetails = optionalWitnesses.orElse(Collections.emptyList());

        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(language -> {
            if (isInterpreterPresent(language)) {
                populateSpokenLanguageBookingStatusFieldsForWitness(language, witnessesDetails);
            } else {
                clearSpokenLanguageBookingStatusFieldsForWitness(language);
            }

        });

        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(language -> {
            if (isInterpreterPresent(language)) {
                populateSignLanguageBookingStatusFieldsForWitness(language, witnessesDetails);
            } else {
                clearSignLanguageBookingStatusFieldsForWitness(language);
            }

        });
    }

    /**
     * It is required to do null checks for the inner fields (languageRefData and languageManualEntryDescription) as well,
     * as there appears to be a CCD bug where it creates empty interpreter language fields in the Review Hearing Requirements
     * event which caused by COMPLEX type fields being set as READONLY.
     */
    private boolean isInterpreterPresent(AsylumCaseFieldDefinition language) {
        return asylumCase
            .read(language, InterpreterLanguageRefData.class)
            .filter(refData ->
                refData.getLanguageRefData() != null || refData.getLanguageManualEntryDescription() != null)
            .isPresent();
    }

    private void populateOrClearAppellantSignAndSpokenInterpreterBookingFields() {
        Optional<List<String>> languageCategoriesOptional = asylumCase
            .read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);

        if (languageCategoriesOptional.isPresent() && languageCategoriesOptional.get().contains(SPOKEN_LANGUAGE_INTERPRETER)) {
            populateBookingStatusFieldsForAppellant(
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE,
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING,
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        } else {
            clearBookingStatusFields(
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING,
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        }

        if (languageCategoriesOptional.isPresent() && languageCategoriesOptional.get().contains(SIGN_LANGUAGE_INTERPRETER)) {
            populateBookingStatusFieldsForAppellant(
                APPELLANT_INTERPRETER_SIGN_LANGUAGE,
                APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING,
                APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
        } else {
            clearBookingStatusFields(
                APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING,
                APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
        }
    }

    private void populateBookingStatusFieldsForAppellant(AsylumCaseFieldDefinition language,
                                                         AsylumCaseFieldDefinition booking,
                                                         AsylumCaseFieldDefinition bookingStatus) {
        String bookingDetails = formatBookingDetails(
            asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElse(""),
            APPELLANT,
            getLanguage(language));

        asylumCase.write(booking, bookingDetails);

        setBookingStatus(bookingStatus);
    }

    private void populateSpokenLanguageBookingStatusFieldsForWitness(AsylumCaseFieldDefinition witness,
                                                                   List<IdValue<WitnessDetails>> witnesses) {
        String witnessName = "";
        switch (witness) {
            case WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(0).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
            }
            case WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(1).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
            }
            case WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(2).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
            }
            case WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(3).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
            }
            case WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(4).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_5,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5);
            }
            case WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(5).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_6,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6);
            }
            case WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(6).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_7,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7);
            }
            case WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(7).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_8,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8);
            }
            case WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(8).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_9,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9);
            }
            case WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(9).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_10,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10);
            }
            default -> {
                throw new IllegalArgumentException("Unknown witness interpreter spoken language type");
            }
        }
    }

    private void clearSpokenLanguageBookingStatusFieldsForWitness(AsylumCaseFieldDefinition language) {
        switch (language) {
            case WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
            }
            case WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
            }
            case WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
            }
            case WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
            }
            case WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_5,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5);
            }
            case WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_6,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6);
            }
            case WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_7,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7);
            }
            case WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_8,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8);
            }
            case WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_9,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9);
            }
            case WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_10,
                    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10);
            }
            default -> {
                throw new IllegalArgumentException("Unknown witness interpreter spoken language type");
            }
        }
    }

    private void populateSignLanguageBookingStatusFieldsForWitness(AsylumCaseFieldDefinition language,
                                                                   List<IdValue<WitnessDetails>> witnesses) {
        String witnessName = "";
        switch (language) {
            case WITNESS_1_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(0).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_1_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
            }
            case WITNESS_2_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(1).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_2_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
            }
            case WITNESS_3_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(2).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_3_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
            }
            case WITNESS_4_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(3).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_4_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
            }
            case WITNESS_5_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(4).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_5_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_5,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5);
            }
            case WITNESS_6_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(5).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_6_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_6,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6);
            }
            case WITNESS_7_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(6).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_7_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_7,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7);
            }
            case WITNESS_8_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(7).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_8_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_8,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8);
            }
            case WITNESS_9_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(8).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_9_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_9,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9);
            }
            case WITNESS_10_INTERPRETER_SIGN_LANGUAGE -> {
                witnessName = buildWitnessFullName(witnesses.get(9).getValue());
                assignBookingStatus(
                    witnessName,
                    WITNESS_10_INTERPRETER_SIGN_LANGUAGE,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_10,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10);
            }
            default -> {
                throw new IllegalArgumentException("Unknown witness interpreter sign language type");
            }
        }
    }

    private void clearSignLanguageBookingStatusFieldsForWitness(AsylumCaseFieldDefinition language) {
        switch (language) {
            case WITNESS_1_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
            }
            case WITNESS_2_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
            }
            case WITNESS_3_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
            }
            case WITNESS_4_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
            }
            case WITNESS_5_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_5,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5);
            }
            case WITNESS_6_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_6,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6);
            }
            case WITNESS_7_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_7,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7);
            }
            case WITNESS_8_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_8,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8);
            }
            case WITNESS_9_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_9,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9);
            }
            case WITNESS_10_INTERPRETER_SIGN_LANGUAGE -> {
                clearBookingStatusFields(
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_10,
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10);
            }
            default -> {
                throw new IllegalArgumentException("Unknown witness interpreter sign language type");
            }
        }
    }
    
    private void assignBookingStatus(String witnessName,
                                     AsylumCaseFieldDefinition language,
                                     AsylumCaseFieldDefinition booking,
                                     AsylumCaseFieldDefinition bookingStatus) {
        String bookingDetails = formatBookingDetails(
            witnessName,
            WITNESS,
            getLanguage(language));

        asylumCase.write(booking, bookingDetails);

        setBookingStatus(bookingStatus);
    }

    private String getLanguage(AsylumCaseFieldDefinition fieldDefinition) {
        return asylumCase
            .read(fieldDefinition, InterpreterLanguageRefData.class)
            .map(languageRefData ->
                isEmpty(languageRefData.getLanguageManualEntryDescription())
                    ? languageRefData.getLanguageRefData().getValue().getLabel()
                    : languageRefData.getLanguageManualEntryDescription())
            .orElse("");
    }

    private String formatBookingDetails(String name, String party, String language) {
        return String.format(
            "%s - %s - %s",
            name,
            party,
            language
        );
    }

    private void setBookingStatus(AsylumCaseFieldDefinition bookingStatus) {
        if (asylumCase.read(bookingStatus).isEmpty()) {
            asylumCase.write(bookingStatus, NOT_REQUESTED);
        }
    }

    private void clearBookingStatusFields(AsylumCaseFieldDefinition booking,
                                          AsylumCaseFieldDefinition bookingStatus) {
        asylumCase.clear(booking);
        asylumCase.clear(bookingStatus);
    }
}
