package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REVIEW_OUTCOME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_RESPONSE_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.RESPONDENT_REVIEW;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithDocumentApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithNotificationsApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithTimedEventServiceStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;

class AutomaticDirectionHandlerTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
    WithServiceAuthStub, WithTimedEventServiceStub, WithNotificationsApiStub, WithRoleAssignmentStub,
    WithDocumentApiStub {

    @MockitoBean
    private RequestUserAccessTokenProvider requestTokenProvider;

    private String expectedId = "someId";
    private long caseId = 54321;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void should_trigger_timed_event_service() {
        when(requestTokenProvider.getAccessToken()).thenReturn("Bearer token");
        addServiceAuthStub(server);
        addCaseWorkerUserDetailsStub(server);
        addTimedEventServiceStub(server);
        addNotificationsApiTransformerStub(server);
        addRoleAssignmentQueryStub(server);
        addDocumentApiTransformerStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(
            callback()
                .event(REQUEST_RESPONSE_REVIEW)
                .caseDetails(
                    someCaseDetailsWith()
                        .id(caseId)
                        .state(RESPONDENT_REVIEW)
                        .caseData(
                            anAsylumCase()
                                .with(APPEAL_TYPE, AppealType.PA)
                                .with(APPELLANT_GIVEN_NAMES, "some names")
                                .with(APPELLANT_FAMILY_NAME, "some family name")
                                .with(SEND_DIRECTION_EXPLANATION, "some explanation")
                                .with(SEND_DIRECTION_DATE_DUE, "2025-12-25")
                                .with(APPEAL_REVIEW_OUTCOME, "decisionMaintained")
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
