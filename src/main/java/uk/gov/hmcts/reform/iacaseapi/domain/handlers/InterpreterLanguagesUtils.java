package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
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

    public static final List<AsylumCaseFieldDefinition> WITNESS_LIST_ELEMENT_N_FIELD = List.of(
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
        WITNESS_LIST_ELEMENT_N_FIELD.forEach(field -> asylumCase.write(field, new DynamicMultiSelectList()));
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

}
