package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;

@FeignClient(name = "location-ref-data-api", url = "${location.ref.data.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface LocationRefDataApi {

    @GetMapping(value = "refdata/location/court-venues/services", produces = "application/json", consumes = "application/json")
    CourtLocationCategory getCourtVenues(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("service_code") String serviceCode
    );
}
