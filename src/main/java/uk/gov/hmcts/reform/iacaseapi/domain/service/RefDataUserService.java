package uk.gov.hmcts.reform.iacaseapi.domain.service;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicListElement;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;


@Slf4j
@Service
public class RefDataUserService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    CommonDataRefApi commonDataRefApi;

    public static final String SERVICE_ID = "ABA5";

    private  List<DynamicListElement> listOfCategoryValues;

    private CommonDataResponse commonDataResponse;

    public CommonDataResponse retrieveCategoryValues(String authorization, String categoryId,String isHearingChildRequired) {
        log.info("retrieveCategoryValues {}", categoryId);
        try {
            commonDataResponse = commonDataRefApi.getAllCategoryValuesByCategoryId(
                authorization,
                authTokenGenerator.generate(),
                categoryId,
                SERVICE_ID,
                isHearingChildRequired
            );

        } catch (Exception e) {
            log.error("Category Values look up failed {} ", e.getMessage());
        }
        return commonDataResponse;
    }

    public List<DynamicListElement> filterCategoryValuesByCategoryId(CommonDataResponse commonDataResponse, String categoryId) {
        if (null != commonDataResponse) {
            listOfCategoryValues = commonDataResponse.getCategoryValues().stream()
                    .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                    .map(this::getDisplayCategoryEntry).collect(Collectors.toList());
            Collections.sort(listOfCategoryValues, (a, b) -> a.getCode().compareToIgnoreCase(b.getCode()));
            return listOfCategoryValues;
        }

        return List.of(DynamicListElement.builder().build());
    }

    private DynamicListElement getDisplayCategoryEntry(CategoryValues categoryValues) {
        String value = categoryValues.getValueEn();
        String key = categoryValues.getKey();
        return DynamicListElement.builder().code(key).label(value).build();
    }

}



