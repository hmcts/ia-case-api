package uk.gov.hmcts.reform.bailcaseapi.domain.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
}
