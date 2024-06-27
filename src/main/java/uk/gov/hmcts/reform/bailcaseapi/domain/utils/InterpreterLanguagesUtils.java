package uk.gov.hmcts.reform.bailcaseapi.domain.utils;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseServiceResponseException;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

import java.util.List;
import java.util.Optional;

public class InterpreterLanguagesUtils {
    public static final String IS_CHILD_REQUIRED = "Y";

    private InterpreterLanguagesUtils() {
        // Utils classes should not have public or default constructors
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
            throw new BailCaseServiceResponseException(String.format("Could not read response by RefData service for %s(s)", languageCategory), e);
        }

        return new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            "",
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

    private static InterpreterLanguageRefData clearComplexTypeField(InterpreterLanguageRefData languageComplexType) {
        if (languageComplexType.getLanguageManualEntry().contains("No")
            && languageComplexType.getLanguageManualEntryDescription() != null) {

            languageComplexType.setLanguageManualEntryDescription(null);

        } else if (languageComplexType.getLanguageManualEntry().contains("Yes")
            && languageComplexType.getLanguageRefData() != null) {

            languageComplexType.setLanguageRefData(null);
        }
        return languageComplexType;
    }
}
