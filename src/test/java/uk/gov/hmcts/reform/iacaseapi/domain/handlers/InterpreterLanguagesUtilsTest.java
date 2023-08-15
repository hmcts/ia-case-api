package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
