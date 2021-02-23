package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;

@FeignClient(
    name = "ref-data-client",
    url = "${ref-data-case-worker-api.url}",
    fallback = HystrixRefDataCaseWorkerFallback.class
)
public interface RefDataCaseWorkerApi {

    @PostMapping(
        value = "/refdata/case-worker/users/fetchUsersById",
        produces = "application/json",
        consumes = "application/json"
    )
    List<CaseWorkerProfile> fetchUsersById(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @RequestBody UserIds userIds
    );

}