package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;

public class AutomaticDirectionHandlerTest extends SpringBootIntegrationTest {

    @MockBean
    private RequestUserAccessTokenProvider requestTokenProvider;

    @Autowired
    private AutomaticDirectionRequestingHearingRequirementsHandler handler;

    private String expectedId = "someId";
    private long caseId = 54321;

    @Before
    public void setupTimedEventServiceStub() {

        when(requestTokenProvider.getAccessToken()).thenReturn("Bearer token");

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer")
        );

        stubFor(post(urlEqualTo("/timed-event-service/timed-event"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"id\": \"" + expectedId + "\","
                          + " \"jurisdiction\": \"IA\","
                          + " \"caseType\": \"Asylum\","
                          + " \"caseId\": " + caseId + ","
                          + " \"event\": \"requestHearingRequirementsFeature\","
                          + " \"scheduledDateTime\": \"2020-05-13T10:00:00Z\" }")));
    }

    @Test
    public void should_trigger_timed_event_service() {
        AsylumCase asylumCase = new AsylumCase();
;
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

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        String id = response
            .getData()
            .read(AsylumCaseFieldDefinition.AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, String.class)
            .orElse("");

        assertThat(expectedId).isEqualTo(id);
    }
}
