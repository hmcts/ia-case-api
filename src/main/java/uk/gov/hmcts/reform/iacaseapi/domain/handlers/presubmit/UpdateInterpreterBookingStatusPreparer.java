package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.buildWitnessFullName;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory;
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
    public static String APPELLANT = "Appellant";
    public static String WITNESS = "Witness";
    private AsylumCase asylumCase;
    private String witnessName;

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

        asylumCase = callback.getCaseDetails().getCaseData();

        if (asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE).isPresent()) {
            populateBookingStatusFieldsForAppellant(
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE,
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING,
                APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        }

        if (asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE).isPresent()) {
            populateBookingStatusFieldsForAppellant(
                APPELLANT_INTERPRETER_SIGN_LANGUAGE,
                APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING,
                APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
        }

        Optional<List<IdValue<WitnessDetails>>> optionalWitnesses = asylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> witnessesDetails = optionalWitnesses.orElse(Collections.emptyList());

        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(w -> {
            if (!asylumCase.read(w).isPresent()) {
                return;
            }
            populateSpokenLanguageBookingStatusFieldsForWitness(w, witnessesDetails);
        });

        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(w -> {
            if (!asylumCase.read(w).isPresent()) {
                return;
            }
            populateSignLanguageBookingStatusFieldsForWitness(w, witnessesDetails);
        });

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void populateBookingStatusFieldsForAppellant(AsylumCaseFieldDefinition language,
                                                         AsylumCaseFieldDefinition booking,
                                                         AsylumCaseFieldDefinition bookingStatus) {

        // TODO - change this field to a String type to only hold the formatBookingDetails
        InterpreterBookingDetails bookingDetails = new InterpreterBookingDetails(
            NOT_REQUESTED,
            formatBookingDetails(
                (String) asylumCase.read(APPELLANT_NAME_FOR_DISPLAY).get(),
                APPELLANT,
                getLanguage(language)));

        asylumCase.write(booking, bookingDetails);

        // TODO - if bookingStatus is not set then set as not requested
        asylumCase.write(bookingStatus, NOT_REQUESTED);
    }

    private void populateSpokenLanguageBookingStatusFieldsForWitness(AsylumCaseFieldDefinition witness,
                                                                   List<IdValue<WitnessDetails>> witnesses) {
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
                    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7);
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
        }
    }

    private void populateSignLanguageBookingStatusFieldsForWitness(AsylumCaseFieldDefinition witness,
                                                                   List<IdValue<WitnessDetails>> witnesses) {
        switch (witness) {
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
        }
    }

    private void assignBookingStatus(String witnessName,
                                     AsylumCaseFieldDefinition language,
                                     AsylumCaseFieldDefinition booking,
                                     AsylumCaseFieldDefinition bookingStatus) {
        InterpreterBookingDetails bookingDetails = new InterpreterBookingDetails(
            NOT_REQUESTED,
            formatBookingDetails(
                witnessName,
                WITNESS,
                getLanguage(language)));

        asylumCase.write(booking, bookingDetails);

        asylumCase.write(bookingStatus, NOT_REQUESTED);
    }

    private String getLanguage(AsylumCaseFieldDefinition fieldDefinition) {
        Optional<InterpreterLanguageRefData> languageRefData = asylumCase.read(
            fieldDefinition, InterpreterLanguageRefData.class);

        String language = isEmpty(languageRefData.get().getLanguageManualEntryDescription()) ?
            languageRefData.get().getLanguageRefData().getValue().getLabel() :
            languageRefData.get().getLanguageManualEntryDescription();

        return language;
    }

    private String formatBookingDetails(String name, String party, String language) {
        return String.format(
            "%s - %s - %s",
            name,
            party,
            language
        );
    }
}
