package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    name = "extended-ccd-data-store-api",
    url = "${ccd.case-data-api.url}",
    configuration = FeignConfiguration.class
)
public interface ExtendedCcdApi {
    @GetMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}",
        produces = "application/json",
        consumes = "application/json")
    CaseDetails get(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("uid") String userId,
        @PathVariable("jid") String jurisdiction,
        @PathVariable("ctid") String caseType,
        @PathVariable("cid") String id
    );
}
