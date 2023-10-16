package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@ExtendWith(MockitoExtension.class)
public class InterpreterLanguagesUtilsTest {

    private static final String LANGUAGE_CATEGORY = "languageCategory";

    @Mock
    CommonDataResponse commonDataResponse;
    @Mock
    CategoryValues categoryValues;
    @Mock
    RefDataUserService refDataUserService;
    @Mock
    Value value;
    @Mock
    InterpreterLanguageRefData interpreterLanguageRefDataSpoken1;
    @Mock
    DynamicList dynamicListSpoken1;
    @Mock
    Value valueSpoken1;
    @Mock
    InterpreterLanguageRefData interpreterLanguageRefDataSign1;
    @Mock
    DynamicList dynamicListSign1;
    @Mock
    Value valueSign1;
    @Mock
    InterpreterLanguageRefData interpreterLanguageRefDataSpoken3;
    @Mock
    AsylumCase asylumCase;

    @Test
    void should_generate_dynamic_list() {
        when(refDataUserService.retrieveCategoryValues(LANGUAGE_CATEGORY, "Y"))
            .thenReturn(commonDataResponse);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, LANGUAGE_CATEGORY))
            .thenReturn(List.of(categoryValues));
        when(refDataUserService.mapCategoryValuesToDynamicListValues(List.of(categoryValues)))
            .thenReturn(List.of(value));

        InterpreterLanguageRefData result = InterpreterLanguagesUtils
            .generateDynamicList(refDataUserService, LANGUAGE_CATEGORY);

        assertEquals("", result.getLanguageRefData().getValue().getCode());
        assertEquals("", result.getLanguageRefData().getValue().getLabel());
        assertEquals(List.of(value), result.getLanguageRefData().getListItems());
        assertTrue(result.getLanguageManualEntry().isEmpty());
        assertEquals("", result.getLanguageManualEntryDescription());
    }

    @Test
    void should_populate_witness_n_interpreter_language_category() {

        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataSpoken1));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataSign1));

        when(asylumCase.read(WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(WITNESS_2_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataSpoken3));
        when(asylumCase.read(WITNESS_3_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        int i = 3;
        while (i < 10) {
            when(asylumCase.read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i), InterpreterLanguageRefData.class))
                .thenReturn(Optional.empty());
            when(asylumCase.read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(i), InterpreterLanguageRefData.class))
                .thenReturn(Optional.empty());
            i++;
        }

        when(interpreterLanguageRefDataSpoken1.getLanguageRefData()).thenReturn(dynamicListSpoken1);
        when(interpreterLanguageRefDataSign1.getLanguageRefData()).thenReturn(dynamicListSign1);
        when(dynamicListSpoken1.getValue()).thenReturn(valueSpoken1);
        when(dynamicListSign1.getValue()).thenReturn(valueSign1);
        when(valueSpoken1.getLabel()).thenReturn("lang 1");
        when(valueSign1.getLabel()).thenReturn("lang sign 1");
        when(interpreterLanguageRefDataSpoken3.getLanguageManualEntry()).thenReturn(List.of("Yes"));
        when(interpreterLanguageRefDataSpoken3.getLanguageManualEntryDescription()).thenReturn("manually entered lang");

        InterpreterLanguagesUtils.persistWitnessInterpreterCategoryField(asylumCase);

        verify(asylumCase, times(1))
            .write(WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY, List.of(
                SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue()));
        verify(asylumCase, times(1))
            .write(WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY, List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue()));
    }
}
