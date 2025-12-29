package uk.gov.hmcts.reform.bailcaseapi.domain.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SIGN_LANGUAGE;

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
    private BailCase bailCase;

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
    void should_clear_duplicated_interpreter_value_when_applicant_or_fcs_require_interpreter_service() {
        InterpreterLanguageRefData spokenInterpreterLanguage = new InterpreterLanguageRefData(
            new DynamicList(new Value("scl", "Shina"), Collections.emptyList()),
            "Yes",
            "spoken manual");
        InterpreterLanguageRefData signInterpreterLanguage = new InterpreterLanguageRefData(
            new DynamicList(new Value("jpn", "Japanese"), Collections.emptyList()),
            "No",
            "sign manual");

        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenInterpreterLanguage));
        when(bailCase.read(FCS1_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signInterpreterLanguage));

        InterpreterLanguagesUtils.sanitizeInterpreterLanguageRefDataComplexType(bailCase, APPLICANT_INTERPRETER_SPOKEN_LANGUAGE);
        InterpreterLanguagesUtils.sanitizeInterpreterLanguageRefDataComplexType(bailCase, FCS1_INTERPRETER_SIGN_LANGUAGE);

        assertNull(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE,InterpreterLanguageRefData.class)
                       .get().getLanguageRefData());
        assertNull(bailCase.read(FCS1_INTERPRETER_SIGN_LANGUAGE,InterpreterLanguageRefData.class)
                       .get().getLanguageManualEntryDescription());

    }
}
