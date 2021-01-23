package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;

@Component
@Slf4j
public class HystrixRefDataCaseWorkerFallback implements RefDataCaseWorkerApi {

    @Override
    public CaseWorkerProfile fetchUsersById(String userToken, String s2sToken, UserIds userIds) {
        log.info("Caseworker's name can not be found. Fallback to empty name");
        return CaseWorkerProfile.builder().build();
    }
}
