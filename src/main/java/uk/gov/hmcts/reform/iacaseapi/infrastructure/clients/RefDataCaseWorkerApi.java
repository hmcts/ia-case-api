package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;

@FeignClient(
    name = "ref-data-client",
    url = "${ref-data-case-worker-api.url}"
)
public interface RefDataCaseWorkerApi {

    @PostMapping(
        value = "/refdata/case-worker/users/fetchUsersById",
        produces = "application/json",
        consumes = "application/json"
    )
    CaseWorkerProfile fetchUsersById(@RequestBody UserIds userIds);

}