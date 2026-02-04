package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.SearchResult;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    name = "case-access-core-case-data-api",
    url = "${case_access.core_case_data_api_url}",
    configuration = FeignConfiguration.class,
    primary = false
)
public interface CcdDataCaseAccessApi extends CcdDataApi {
    String CONTENT_TYPE = "content-type";

    @PostMapping(
        value = "/searchCases?ctid={caseType}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    SearchResult searchCases(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("caseType") String caseType,
        @RequestBody String searchString
    );
}

