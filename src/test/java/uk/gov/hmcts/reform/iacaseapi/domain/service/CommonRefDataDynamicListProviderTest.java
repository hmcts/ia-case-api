package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider.CASE_MANAGEMENT_CANCELLATION_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider.CHANGE_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider.HEARING_CHANNEL_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider.IS_CHILD_REQUIRED;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CommonRefDataDynamicListProviderTest {

    @Mock
    private RefDataUserService refDataUserService;
    @Mock
    private CommonDataResponse commonDataResponse;
    @Mock
    private CategoryValues categoryValues;

    private CommonRefDataDynamicListProvider provider;

    @BeforeEach
    void setup() {
        provider = new CommonRefDataDynamicListProvider(refDataUserService);
    }

    @Test
    void should_provide_hearing_channels_dynamic_list() {
        when(refDataUserService.retrieveCategoryValues(HEARING_CHANNEL_CATEGORY, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        List<CategoryValues> categoryValuesList = List.of(categoryValues);
        when(refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, HEARING_CHANNEL_CATEGORY))
            .thenReturn(categoryValuesList);
        List<Value> values = List.of(new Value("abc", "abc"));
        when(refDataUserService.mapCategoryValuesToDynamicListValues(categoryValuesList)).thenReturn(values);

        DynamicList hearingChannels = provider.provideHearingChannels();

        verify(refDataUserService, times(1))
            .retrieveCategoryValues(HEARING_CHANNEL_CATEGORY, IS_CHILD_REQUIRED);
        verify(refDataUserService, times(1))
            .filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, HEARING_CHANNEL_CATEGORY);
        verify(refDataUserService, times(1))
            .mapCategoryValuesToDynamicListValues(categoryValuesList);

        assertNotNull(hearingChannels);
        assertEquals(values, hearingChannels.getListItems());
    }

    @Test
    void should_provide_change_reasons_dynamic_list() {
        when(refDataUserService.retrieveCategoryValues(CHANGE_REASONS, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        List<CategoryValues> categoryValuesList = List.of(categoryValues);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, CHANGE_REASONS))
            .thenReturn(categoryValuesList);
        List<Value> values = List.of(new Value("abc", "abc"));
        when(refDataUserService.mapCategoryValuesToDynamicListValues(categoryValuesList)).thenReturn(values);

        DynamicList reasons = provider.provideChangeReasons();

        verify(refDataUserService, times(1))
            .retrieveCategoryValues(CHANGE_REASONS, IS_CHILD_REQUIRED);
        verify(refDataUserService, times(1))
            .filterCategoryValuesByCategoryId(commonDataResponse, CHANGE_REASONS);
        verify(refDataUserService, times(1))
            .mapCategoryValuesToDynamicListValues(categoryValuesList);

        assertNotNull(reasons);
        assertEquals(values, reasons.getListItems());
    }

    @Test
    void should_provide_case_managment_cancellation_reasons_dynamic_list() {
        when(refDataUserService.retrieveCategoryValues(CASE_MANAGEMENT_CANCELLATION_REASONS, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        List<CategoryValues> categoryValuesList = List.of(categoryValues);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, CASE_MANAGEMENT_CANCELLATION_REASONS))
            .thenReturn(categoryValuesList);
        List<Value> values = List.of(new Value("abc", "abc"));
        when(refDataUserService.mapCategoryValuesToDynamicListValues(categoryValuesList)).thenReturn(values);

        DynamicList reasons = provider.provideCaseManagementCancellationReasons();

        verify(refDataUserService, times(1))
            .retrieveCategoryValues(CASE_MANAGEMENT_CANCELLATION_REASONS, IS_CHILD_REQUIRED);
        verify(refDataUserService, times(1))
            .filterCategoryValuesByCategoryId(commonDataResponse, CASE_MANAGEMENT_CANCELLATION_REASONS);
        verify(refDataUserService, times(1))
            .mapCategoryValuesToDynamicListValues(categoryValuesList);

        assertNotNull(reasons);
        assertEquals(values, reasons.getListItems());
    }

}
