package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;

class HystrixRefDataCaseWorkerFallbackTest {

    @Test
    void fetchUsersById() {
        HystrixRefDataCaseWorkerFallback fallback = new HystrixRefDataCaseWorkerFallback();
        CaseWorkerProfile actualCaseWorkerProfile = fallback.fetchUsersById("some user token",
            "some service token",
            new UserIds(List.of("some user id")));

        assertThat(actualCaseWorkerProfile).isEqualTo(CaseWorkerProfile.builder().build());
    }
}