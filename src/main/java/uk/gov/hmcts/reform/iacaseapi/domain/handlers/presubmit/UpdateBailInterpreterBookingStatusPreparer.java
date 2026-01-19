package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.utils.InterpreterLanguagesUtils.*;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateBailInterpreterBookingStatusPreparer implements PreSubmitCallbackHandler<BailCase> {
    public static String APPLICANT = "Applicant";
    public static String FCS = "FCS";
    private BailCase bailCase;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        this.bailCase = callback.getCaseDetails().getCaseData();

        populateOrClearApplicantSignAndSpokenInterpreterBookingFields();

        populateOrClearFcsSignAndSpokenInterpreterBookingFields();

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void populateOrClearApplicantSignAndSpokenInterpreterBookingFields() {
        Optional<List<String>> languageCategoriesOptional = bailCase
            .read(APPLICANT_INTERPRETER_LANGUAGE_CATEGORY);

        if (languageCategoriesOptional.isPresent()
            && languageCategoriesOptional.get().contains(SPOKEN_LANGUAGE_INTERPRETER.getValue())) {
            populateBookingStatusFieldsForApplicant(
                APPLICANT_INTERPRETER_SPOKEN_LANGUAGE,
                APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING,
                APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        } else {
            clearBookingStatusFields(
                APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING,
                APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        }

        if (languageCategoriesOptional.isPresent()
            && languageCategoriesOptional.get().contains(SIGN_LANGUAGE_INTERPRETER.getValue())) {
            populateBookingStatusFieldsForApplicant(
                APPLICANT_INTERPRETER_SIGN_LANGUAGE,
                APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING,
                APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
        } else {
            clearBookingStatusFields(
                APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING,
                APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
        }
    }

    private void populateBookingStatusFieldsForApplicant(BailCaseFieldDefinition language,
                                                         BailCaseFieldDefinition booking,
                                                         BailCaseFieldDefinition bookingStatus) {

        String applicantFullName = getFullName(APPLICANT_GIVEN_NAMES, APPLICANT_FAMILY_NAME);

        String bookingDetails = formatBookingDetails(
            applicantFullName,
            APPLICANT,
            getLanguage(language));

        bailCase.write(booking, bookingDetails);

        setBookingStatus(bookingStatus);
    }

    private void populateOrClearFcsSignAndSpokenInterpreterBookingFields() {

        FCS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(language -> {
            if (bailCase.read(language, InterpreterLanguageRefData.class).isPresent()) {
                populateSpokenLanguageBookingStatusFieldsForFcs(language);
            } else {
                clearSpokenLanguageBookingStatusFieldsForFcs(language);
            }
        });

        FCS_N_INTERPRETER_SIGN_LANGUAGE.forEach(language -> {
            if (bailCase.read(language, InterpreterLanguageRefData.class).isPresent()) {
                populateSignLanguageBookingStatusFieldsForFcs(language);
            } else {
                clearSignLanguageBookingStatusFieldsForFcs(language);
            }
        });
    }

    private void populateSpokenLanguageBookingStatusFieldsForFcs(BailCaseFieldDefinition fcs) {
        String fcsFullName;
        switch (fcs) {
            case FCS1_INTERPRETER_SPOKEN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_GIVEN_NAMES, SUPPORTER_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS1_INTERPRETER_SPOKEN_LANGUAGE,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
            }
            case FCS2_INTERPRETER_SPOKEN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_2_GIVEN_NAMES, SUPPORTER_2_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS2_INTERPRETER_SPOKEN_LANGUAGE,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
            }
            case FCS3_INTERPRETER_SPOKEN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_3_GIVEN_NAMES, SUPPORTER_3_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS3_INTERPRETER_SPOKEN_LANGUAGE,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
            }
            case FCS4_INTERPRETER_SPOKEN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_4_GIVEN_NAMES, SUPPORTER_4_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS4_INTERPRETER_SPOKEN_LANGUAGE,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4,
                    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
            }
            default -> throw new IllegalArgumentException("Unknown FCS interpreter spoken language type");
        }
    }

    private void clearSpokenLanguageBookingStatusFieldsForFcs(BailCaseFieldDefinition language) {
        switch (language) {
            case FCS1_INTERPRETER_SPOKEN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1,
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
            case FCS2_INTERPRETER_SPOKEN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2,
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
            case FCS3_INTERPRETER_SPOKEN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3,
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
            case FCS4_INTERPRETER_SPOKEN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4,
                FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
            default -> throw new IllegalArgumentException("Unknown FCS interpreter sign language type");
        }
    }

    private void populateSignLanguageBookingStatusFieldsForFcs(BailCaseFieldDefinition fcs) {
        String fcsFullName;
        switch (fcs) {
            case FCS1_INTERPRETER_SIGN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_GIVEN_NAMES, SUPPORTER_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS1_INTERPRETER_SIGN_LANGUAGE,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
            }
            case FCS2_INTERPRETER_SIGN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_2_GIVEN_NAMES, SUPPORTER_2_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS2_INTERPRETER_SIGN_LANGUAGE,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
            }
            case FCS3_INTERPRETER_SIGN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_3_GIVEN_NAMES, SUPPORTER_3_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS3_INTERPRETER_SIGN_LANGUAGE,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
            }
            case FCS4_INTERPRETER_SIGN_LANGUAGE -> {
                fcsFullName = getFullName(SUPPORTER_4_GIVEN_NAMES, SUPPORTER_4_FAMILY_NAMES);
                assignBookingStatus(
                    fcsFullName,
                    FCS4_INTERPRETER_SIGN_LANGUAGE,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4,
                    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
            }
            default -> throw new IllegalArgumentException("Unknown FCS interpreter spoken language type");
        }
    }

    private void clearSignLanguageBookingStatusFieldsForFcs(BailCaseFieldDefinition language) {
        switch (language) {
            case FCS1_INTERPRETER_SIGN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1,
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
            case FCS2_INTERPRETER_SIGN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2,
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
            case FCS3_INTERPRETER_SIGN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3,
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
            case FCS4_INTERPRETER_SIGN_LANGUAGE -> clearBookingStatusFields(
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4,
                FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
            default -> throw new IllegalArgumentException("Unknown FCS interpreter spoken language type");
        }
    }

    private void assignBookingStatus(String fcsName,
                                     BailCaseFieldDefinition language,
                                     BailCaseFieldDefinition booking,
                                     BailCaseFieldDefinition bookingStatus) {
        String bookingDetails = formatBookingDetails(
            fcsName,
            FCS,
            getLanguage(language));

        bailCase.write(booking, bookingDetails);

        setBookingStatus(bookingStatus);
    }

    private String getLanguage(BailCaseFieldDefinition fieldDefinition) {
        return bailCase
            .read(fieldDefinition, InterpreterLanguageRefData.class)
            .map(languageRefData ->
                isEmpty(languageRefData.getLanguageManualEntryDescription())
                    ? languageRefData.getLanguageRefData().getValue().getLabel()
                    : languageRefData.getLanguageManualEntryDescription())
            .orElse("");
    }

    private String formatBookingDetails(String fullName, String party, String language) {
        return String.format(
            "%s - %s - %s",
            fullName,
            party,
            language
        );
    }

    private void setBookingStatus(BailCaseFieldDefinition bookingStatus) {
        if (bailCase.read(bookingStatus).isEmpty()) {
            bailCase.write(bookingStatus, NOT_REQUESTED);
        }
    }

    private void clearBookingStatusFields(BailCaseFieldDefinition booking,
                                          BailCaseFieldDefinition bookingStatus) {
        bailCase.clear(booking);
        bailCase.clear(bookingStatus);
    }

    private String getFullName(BailCaseFieldDefinition givenNames, BailCaseFieldDefinition familyName) {

        final String givenNamesValue = bailCase.read(givenNames, String.class).orElse("");
        final String familyNameValue = bailCase.read(familyName, String.class).orElse("");
        return givenNamesValue + " " + familyNameValue;
    }
}
