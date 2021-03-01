package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatethecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase.CaseWorkerName;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseWorkerService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@SpringBootTest
@ActiveProfiles("integration")
public class CaseWorkerServiceTest {

    @Autowired
    private WireMockServer caseWorkerRefDataServiceMock;

    @MockBean
    private IdamService idamService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private CaseWorkerService caseWorkerService;

    public static final String ACTOR_ID = "some actor id";

    @Test
    void give_case_worker_ref_data_responds_with_200_then_return_case_worker_name() throws IOException {
        CaseWorkerRefDataMock.setup200MockResponse(caseWorkerRefDataServiceMock);

        CaseWorkerName actualCaseWorkerName = caseWorkerService.getCaseWorkerNameForActorId(ACTOR_ID);

        assertThat(actualCaseWorkerName).isEqualTo(new CaseWorkerName(ACTOR_ID, "firstName lastName"));
    }

    @Test
    void give_case_worker_ref_data_responds_with_404_then_fallback_returns_empty_case_worker_name() {
        CaseWorkerRefDataMock.setup404MockResponse(caseWorkerRefDataServiceMock);

        CaseWorkerName actualCaseWorkerName = caseWorkerService.getCaseWorkerNameForActorId(ACTOR_ID);

        assertThat(actualCaseWorkerName).isEqualTo(new CaseWorkerName(ACTOR_ID, ""));
    }

}
