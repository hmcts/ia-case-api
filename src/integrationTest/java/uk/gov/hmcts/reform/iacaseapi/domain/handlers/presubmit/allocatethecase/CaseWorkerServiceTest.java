package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatethecase;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase.CaseWorkerName;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseWorkerService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@SpringBootTest
@ActiveProfiles("integration")
@ExtendWith({
    WiremockResolver.class
})
@TestPropertySource(properties = {
    "REF_DATA_CASE_WORKER_URL=http://127.0.0.1:8990"
})
public class CaseWorkerServiceTest {

    @MockBean
    private IdamService idamService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private CaseWorkerService caseWorkerService;

    public static final String ACTOR_ID = "e7013580-ac60-40fd-9cb5-8cd968db9201";

    @Test
    void given_case_worker_ref_data_responds_with_200_then_return_case_worker_name(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server)
        throws IOException {

        CaseWorkerRefDataMock.setup200MockResponse(server);

        List<CaseWorkerName> actualCaseWorkerNames = caseWorkerService.getCaseWorkerNameForActorIds(newArrayList(ACTOR_ID));

        assertThat(actualCaseWorkerNames.get(0)).isEqualTo(new CaseWorkerName(ACTOR_ID, "Case Officer"));
    }
}
