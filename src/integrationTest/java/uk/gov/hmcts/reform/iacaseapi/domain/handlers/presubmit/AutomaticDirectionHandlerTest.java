package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_RESPONSE_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.RESPONDENT_REVIEW;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.*;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;

public class AutomaticDirectionHandlerTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
    WithServiceAuthStub, WithTimedEventServiceStub, WithNotificationsApiStub {

    @MockBean
    private RequestUserAccessTokenProvider requestTokenProvider;

    private String expectedId = "someId";
    private long caseId = 54321;
    private String caseType = "Asylum";

    @BeforeEach
    public void setupTimedEventServiceStub() {
        when(requestTokenProvider.getAccessToken()).thenReturn("Bearer token");
    }


    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    void should_trigger_timed_event_service(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addTimedEventServiceStub(server);
        addNotificationsApiTransformerStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(
            callback()
                .event(REQUEST_RESPONSE_REVIEW)
                .caseDetails(
                    someCaseDetailsWith()
                        .id(caseId)
                        .state(RESPONDENT_REVIEW)
                        .caseType(caseType)
                        .caseData(
                            anAsylumCase()
                                .with(APPELLANT_GIVEN_NAMES, "some names")
                                .with(APPELLANT_FAMILY_NAME, "some family name")
                                .with(SEND_DIRECTION_EXPLANATION, "some explanation")
                                .with(SEND_DIRECTION_DATE_DUE, "2025-12-25")
                        )
                )
        );

        String id = response
            .getData()
            .read(AsylumCaseFieldDefinition.AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, String.class)
            .orElse("");

        assertThat(expectedId).isEqualTo(id);
    }
}
