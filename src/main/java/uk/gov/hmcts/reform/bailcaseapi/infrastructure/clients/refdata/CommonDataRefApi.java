package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@FeignClient(name = "common-ref-data-api", url = "${commonData.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface CommonDataRefApi {

    @GetMapping(value = "refdata/commondata/lov/categories/{categoryId}", consumes = "application/json")
    CommonDataResponse getAllCategoryValuesByCategoryId(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @PathVariable("categoryId") String categoryId,
            @RequestParam("serviceId") String serviceId,
            @RequestParam("isChildRequired") String isChildRequired
    );
}
