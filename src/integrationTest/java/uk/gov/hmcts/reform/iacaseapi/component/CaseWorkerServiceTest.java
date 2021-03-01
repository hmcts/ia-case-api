package uk.gov.hmcts.reform.iacaseapi.component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase.CaseWorkerName;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseWorkerService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@SpringBootTest(properties = {
    "ref-data-case-worker-api.url=http://localhost:8080"
})
@ActiveProfiles("integration")
public class CaseWorkerServiceTest {

    public static final String ACTOR_ID = "some actor id";

    WireMockServer wireMockServer = new WireMockServer();

    @MockBean
    private IdamService idamService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CaseWorkerService caseWorkerService;

    @Test
    void give_case_worker_ref_data_responds_with_404_then_fallback_returns_empty_case_worker_name() {
        wireMockServer.start();

        Mockito.when(idamService.getUserToken()).thenReturn("some user token");
        Mockito.when(authTokenGenerator.generate()).thenReturn("some service token");

        String url = "/refdata/case-worker/users/fetchUsersById";
        givenThat(post(urlEqualTo(url)).willReturn(aResponse().withStatus(404)));

        CaseWorkerName actualCaseWorkerName = caseWorkerService.getCaseWorkerNameForActorId(ACTOR_ID);

        assertThat(actualCaseWorkerName).isEqualTo(new CaseWorkerName(ACTOR_ID, ""));
        verify(postRequestedFor(urlEqualTo(url)));

        wireMockServer.stop();
    }
}
