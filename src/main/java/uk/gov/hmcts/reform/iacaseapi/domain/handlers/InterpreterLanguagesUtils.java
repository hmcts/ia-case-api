package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS2_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS2_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS3_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS3_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS3_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS4_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS4_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS4_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

public final class InterpreterLanguagesUtils {

    public static final String IS_CHILD_REQUIRED = "Y";

    private InterpreterLanguagesUtils() {
        // Utils classes should not have public or default constructors
    }

    public static final List<AsylumCaseFieldDefinition> WITNESS_N_FIELD = List.of(
        WITNESS_1,
        WITNESS_2,
        WITNESS_3,
        WITNESS_4,
        WITNESS_5,
        WITNESS_6,
        WITNESS_7,
        WITNESS_8,
        WITNESS_9,
        WITNESS_10);

    public static final List<AsylumCaseFieldDefinition> WITNESS_N_INTERPRETER_CATEGORY_FIELD = List.of(
        WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_4_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_5_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_6_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_7_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_8_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_9_INTERPRETER_LANGUAGE_CATEGORY,
        WITNESS_10_INTERPRETER_LANGUAGE_CATEGORY
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_LIST_ELEMENT_N = List.of(
        WITNESS_LIST_ELEMENT_1,
        WITNESS_LIST_ELEMENT_2,
        WITNESS_LIST_ELEMENT_3,
        WITNESS_LIST_ELEMENT_4,
        WITNESS_LIST_ELEMENT_5,
        WITNESS_LIST_ELEMENT_6,
        WITNESS_LIST_ELEMENT_7,
        WITNESS_LIST_ELEMENT_8,
        WITNESS_LIST_ELEMENT_9,
        WITNESS_LIST_ELEMENT_10
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE = List.of(
        WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE,
        WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_N_INTERPRETER_SIGN_LANGUAGE = List.of(
        WITNESS_1_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_2_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_3_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_4_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_5_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_6_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_7_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_8_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_9_INTERPRETER_SIGN_LANGUAGE,
        WITNESS_10_INTERPRETER_SIGN_LANGUAGE
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS = List.of(
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_5,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_6,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_7,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_8,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_9,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_10
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES = List.of(
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS = List.of(
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_5,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_6,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_7,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_8,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_9,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_10
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES = List.of(
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10
    );

    public static String buildWitnessFullName(WitnessDetails witnessDetails) {
        return (witnessDetails.getWitnessName() + " " + witnessDetails.getWitnessFamilyName()).trim();
    }

    public static void clearWitnessIndividualFields(AsylumCase asylumCase) {
        WITNESS_N_FIELD.forEach(field -> asylumCase.write(field, new WitnessDetails("", "")));
        WITNESS_N_INTERPRETER_CATEGORY_FIELD.forEach(field -> asylumCase.write(field, Collections.emptyList()));
    }

    public static void clearWitnessInterpreterLanguageFields(AsylumCase asylumCase) {
        DynamicList dummyDynamicList = new DynamicList("");
        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(field ->
            asylumCase.write(field, new InterpreterLanguageRefData(dummyDynamicList, Collections.emptyList(), "")));
        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(field ->
            asylumCase.write(field, new InterpreterLanguageRefData(dummyDynamicList, Collections.emptyList(), "")));
    }

    public static InterpreterLanguageRefData generateDynamicList(RefDataUserService refDataUserService, String languageCategory) {
        List<CategoryValues> languages;
        DynamicList dynamicListOfLanguages;

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                languageCategory,
                IS_CHILD_REQUIRED
            );

            languages = refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, languageCategory);

            dynamicListOfLanguages = new DynamicList(new Value("", ""),
                refDataUserService.mapCategoryValuesToDynamicListValues(languages));

        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not read response by RefData service for %s(s)", languageCategory), e);
        }

        return new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            Collections.emptyList(),
            "");
    }

    // witness1interpreterCategoryField, witness2interpreterCategoryField ... witness10interpreterCategoryField
    // these fields are dynamically populated through a midEvent, and therefore become fleeting after the page where
    // they're set. To circumvent this shortcoming of ccd it's necessary to deduce their value and write them in this
    // aboutToSubmit handler

    public static void persistWitnessInterpreterCategoryField(AsylumCase asylumCase) {

        int i = 0;
        while (i < 10) {
            Optional<InterpreterLanguageRefData> optionalSpoken = asylumCase
                .read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i), InterpreterLanguageRefData.class);

            Optional<InterpreterLanguageRefData> optionalSign = asylumCase
                .read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i), InterpreterLanguageRefData.class);

            boolean spokenIsChosen = optionalSpoken.isPresent()
                                     && isInterpreterLanguagePopulated(optionalSpoken.get());

            boolean signIsChosen = optionalSign.isPresent()
                                   && isInterpreterLanguagePopulated(optionalSign.get());

            List<String> chosenLanguageType = new ArrayList<>();
            if (spokenIsChosen) {
                chosenLanguageType.add(SPOKEN_LANGUAGE_INTERPRETER.getValue());
            }
            if (signIsChosen) {
                chosenLanguageType.add(SIGN_LANGUAGE_INTERPRETER.getValue());
            }

            if (!chosenLanguageType.isEmpty()) {
                asylumCase.write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i), chosenLanguageType);
            } else {
                asylumCase.clear(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(i));
            }

            i++;
        }
    }

    private static boolean isInterpreterLanguagePopulated(InterpreterLanguageRefData interpreterLanguageRefData) {
        boolean dynamicListValueSelected = interpreterLanguageRefData.getLanguageRefData() != null
                                           && interpreterLanguageRefData.getLanguageRefData().getValue() != null
                                           && interpreterLanguageRefData.getLanguageRefData().getValue().getLabel() != null
                                           && !interpreterLanguageRefData.getLanguageRefData().getValue().getLabel().isEmpty();

        boolean manualLanguageSelected = interpreterLanguageRefData.getLanguageManualEntry() != null &&
                interpreterLanguageRefData.getLanguageManualEntryDescription() != null &&
                !interpreterLanguageRefData.getLanguageManualEntry().isEmpty() &&
                !interpreterLanguageRefData.getLanguageManualEntryDescription().isEmpty();

        return dynamicListValueSelected || manualLanguageSelected;
    }

    private static void sanitizeInterpreterLanguageRefDataComplexType(AsylumCase asylumCase, AsylumCaseFieldDefinition asylumCaseFieldDefinition) {
        InterpreterLanguageRefData sanitizedComplexType;
        Optional<InterpreterLanguageRefData> spoken =
            asylumCase.read(asylumCaseFieldDefinition, InterpreterLanguageRefData.class);

        if (spoken.isPresent()) {
            InterpreterLanguageRefData spokenComplexType = spoken.get();

            if (spokenComplexType.getLanguageManualEntry() != null) {
                sanitizedComplexType = clearComplexTypeField(spokenComplexType);
                asylumCase.write(asylumCaseFieldDefinition, sanitizedComplexType);
            }
        }
    }

    private static InterpreterLanguageRefData clearComplexTypeField(InterpreterLanguageRefData languageComplexType) {
        if (languageComplexType.getLanguageManualEntry().isEmpty()
            && languageComplexType.getLanguageManualEntryDescription() != null) {

            languageComplexType.setLanguageManualEntryDescription(null);

        } else if (languageComplexType.getLanguageManualEntry().contains("Yes")
            && languageComplexType.getLanguageRefData() != null) {

            languageComplexType.setLanguageRefData(null);
        }
        return languageComplexType;
    }

    /*
    If the user entered the language manually, the complex type will look like this:
    DYNAMIC LIST: null
    MANUAL LANGUAGE CHECKBOX: Yes
    MANUAL LANGUAGE DESC: An arbitrary language

    When the user changes the language to non-manual, the complex type ends up looking like this:
    DYNAMIC LIST: Selected Ref Data Language
    MANUAL LANGUAGE CHECKBOX: Yes
    MANUAL LANGUAGE DESC: An arbitrary language

    Before saving submitting the event, the field gets sorted by this method so it'll look like this:
    DYNAMIC LIST: Selected Ref Data Language
    MANUAL LANGUAGE CHECKBOX: null
    MANUAL LANGUAGE DESC: null
     */
    public static void sanitizeWitnessLanguageComplexType(AsylumCase asylumCase) {
        int i = 0;
        while (i < 10) {
            sanitizeInterpreterLanguageRefDataComplexType(asylumCase, WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i));
            sanitizeInterpreterLanguageRefDataComplexType(asylumCase, WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i));
            i++;
        }
    }

    public static void sanitizeAppellantLanguageComplexType(AsylumCase asylumCase) {
        sanitizeInterpreterLanguageRefDataComplexType(asylumCase, APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        sanitizeInterpreterLanguageRefDataComplexType(asylumCase, APPELLANT_INTERPRETER_SIGN_LANGUAGE);
    }


    public static final List<BailCaseFieldDefinition> FCS_N_INTERPRETER_LANGUAGE_CATEGORY_FIELD = List.of(
        FCS1_INTERPRETER_LANGUAGE_CATEGORY,
        FCS2_INTERPRETER_LANGUAGE_CATEGORY,
        FCS3_INTERPRETER_LANGUAGE_CATEGORY,
        FCS4_INTERPRETER_LANGUAGE_CATEGORY
    );

    public static final List<BailCaseFieldDefinition> FCS_N_INTERPRETER_SPOKEN_LANGUAGE = List.of(
        FCS1_INTERPRETER_SPOKEN_LANGUAGE,
        FCS2_INTERPRETER_SPOKEN_LANGUAGE,
        FCS3_INTERPRETER_SPOKEN_LANGUAGE,
        FCS4_INTERPRETER_SPOKEN_LANGUAGE
    );

    public static final List<BailCaseFieldDefinition> FCS_N_INTERPRETER_SIGN_LANGUAGE = List.of(
        FCS1_INTERPRETER_SIGN_LANGUAGE,
        FCS2_INTERPRETER_SIGN_LANGUAGE,
        FCS3_INTERPRETER_SIGN_LANGUAGE,
        FCS4_INTERPRETER_SIGN_LANGUAGE
    );

    public static final List<BailCaseFieldDefinition> FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS = List.of(
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4
    );

    public static final List<BailCaseFieldDefinition> FCS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS = List.of(
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4
    );

    public static final List<BailCaseFieldDefinition> FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES = List.of(
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4
    );

    public static final List<BailCaseFieldDefinition> FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES = List.of(
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4
    );

    public static InterpreterLanguageRefData generateDynamicList(uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService refDataUserService, String languageCategory) {
        List<CategoryValues> languages;
        DynamicList dynamicListOfLanguages;

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                languageCategory,
                IS_CHILD_REQUIRED
            );

            languages = refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, languageCategory);

            dynamicListOfLanguages = new DynamicList(new Value("", ""),
                                                                                                     refDataUserService.mapCategoryValuesToDynamicListValues(languages));

        } catch (Exception e) {
            throw new ServiceResponseException(String.format("Could not read response by RefData service for %s(s)", languageCategory), e);
        }

        return new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            List.of(""),
            "");
    }

    public static void sanitizeInterpreterLanguageRefDataComplexType(BailCase bailCase, BailCaseFieldDefinition bailCaseFieldDefinition) {
        InterpreterLanguageRefData sanitizedComplexType;
        Optional<InterpreterLanguageRefData> language =
            bailCase.read(bailCaseFieldDefinition, InterpreterLanguageRefData.class);

        if (language.isPresent()) {
            InterpreterLanguageRefData spokenComplexType = language.get();

            sanitizedComplexType = clearComplexTypeField(spokenComplexType);
            bailCase.write(bailCaseFieldDefinition, sanitizedComplexType);
        }
    }
}
