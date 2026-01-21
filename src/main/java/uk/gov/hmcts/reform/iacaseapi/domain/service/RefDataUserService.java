package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.CommonDataRefApi;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefDataUserService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CommonDataRefApi commonDataRefApi;
    private final UserDetails userDetails;

    public static final String SERVICE_ID = "BFA1";
    public static final String IS_ACTIVE_FLAG = "Y";

    public CommonDataResponse retrieveCategoryValues(String categoryId, String isChildRequired) {
        log.info("retrieveCategoryValues {}", categoryId);
        try {
            return commonDataRefApi.getAllCategoryValuesByCategoryId(
                userDetails.getAccessToken(),
                authTokenGenerator.generate(),
                categoryId,
                SERVICE_ID,
                isChildRequired
            );

        } catch (Exception e) {
            log.error("Category Values look up failed {} ", e.getMessage());
            return null;
        }
    }

    public List<CategoryValues> filterCategoryValuesByCategoryId(
        CommonDataResponse commonDataResponse, String categoryId) {

        return commonDataResponse == null
            ? new ArrayList<>()
            : commonDataResponse.getCategoryValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                .collect(Collectors.toList());

    }

    public List<CategoryValues> filterCategoryValuesByCategoryId(CommonDataResponse commonDataResponse, String categoryId) {
        List<CategoryValues> filteredCategoryValues = new ArrayList<>();

        if (null != commonDataResponse) {
            filteredCategoryValues = commonDataResponse.getCategoryValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .collect(Collectors.toList());

            filteredCategoryValues.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
        }

        return filteredCategoryValues;
    }
    public List<Value> mapCategoryValuesToDynamicListValues(List<CategoryValues> categoryValues) {

        return categoryValues
            .stream()
            .map(categoryValue -> new Value(categoryValue.getKey(), categoryValue.getValueEn()))
            .collect(Collectors.toList());
    }

    public List<CategoryValues> filterCategoryValuesByCategoryIdWithActiveFlag(
        CommonDataResponse commonDataResponse, String categoryId) {

        return commonDataResponse == null
            ? new ArrayList<>()
            : commonDataResponse.getCategoryValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .filter(response -> IS_ACTIVE_FLAG.equals(response.getActiveFlag()))
                .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                .collect(Collectors.toList());
    }

    public List<CategoryValues> filterCategoryValuesByCategoryIdWithActiveFlag(CommonDataResponse commonDataResponse, String categoryId) {
        List<CategoryValues> filteredCategoryValues = new ArrayList<>();

        if (null != commonDataResponse) {
            filteredCategoryValues = commonDataResponse.getCategoryValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .filter(response -> IS_ACTIVE_FLAG.equals(response.getActiveFlag()))
                .collect(Collectors.toList());

            filteredCategoryValues.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
        }

        return filteredCategoryValues;
    }
}



