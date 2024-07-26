package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@Component
@RequiredArgsConstructor
public class CommonRefDataDynamicListProvider {

    private final RefDataUserService refDataUserService;

    public static final String HEARING_CHANNEL_CATEGORY = "HearingChannel";
    public static final String CASE_MANAGEMENT_CANCELLATION_REASONS = "CaseManagementCancellationReasons";
    public static final String CHANGE_REASONS = "ChangeReasons";
    public static final String LANGUAGE_CATEGORY = "languageCategory";
    public static final String IS_CHILD_REQUIRED = "N";

    public DynamicList provideHearingChannels() {

        return provide(HEARING_CHANNEL_CATEGORY, true);
    }

    public DynamicList provideChangeReasons() {

        return provide(CHANGE_REASONS, false);
    }

    public DynamicList provideCaseManagementCancellationReasons() {

        return provide(CASE_MANAGEMENT_CANCELLATION_REASONS, false);
    }

    private DynamicList provide(String categoryId, boolean withActiveFlag) {

        CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
            categoryId,
            IS_CHILD_REQUIRED
        );

        List<CategoryValues> reasons = withActiveFlag
            ? refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, categoryId)
            : refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, categoryId);

        return new DynamicList(new Value("", ""),
            refDataUserService.mapCategoryValuesToDynamicListValues(reasons));
    }
}
