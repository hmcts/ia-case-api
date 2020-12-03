package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithTimedEventServiceStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;

public class AutomaticDirectionHandlerTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
    WithServiceAuthStub, WithTimedEventServiceStub {

    @MockBean
    private RequestUserAccessTokenProvider requestTokenProvider;

    @Autowired
    private AutomaticDirectionRequestingHearingRequirementsHandler handler;

    @BeforeEach
    public void setupTimedEventServiceStub() {
        when(requestTokenProvider.getAccessToken()).thenReturn("Bearer token");
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_trigger_timed_event_service(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addTimedEventServiceStub(server);

        AsylumCase asylumCase = new AsylumCase();

        Callback<AsylumCase> callback = new Callback<>(
            new CaseDetails<>(
                caseId,
                "IA",
                State.RESPONDENT_REVIEW,
                asylumCase,
                LocalDateTime.now()
            ),
            Optional.empty(),
            Event.REQUEST_RESPONSE_REVIEW
        );

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        String id = response
            .getData()
            .read(AsylumCaseFieldDefinition.AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, String.class)
            .orElse("");

        assertThat(expectedId).isEqualTo(id);
    }
}
