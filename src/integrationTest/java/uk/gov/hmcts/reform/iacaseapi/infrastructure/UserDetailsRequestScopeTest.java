package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_TYPES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MAKE_AN_APPLICATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithNotificationsApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;

class UserDetailsRequestScopeTest extends SpringBootIntegrationTest implements WithNotificationsApiStub,
    WithServiceAuthStub, WithRoleAssignmentStub {

    @MockBean
    private RequestUserAccessTokenProvider requestTokenProvider;

    @MockBean
    private IdamApi idamApi;

    private final String token = "Bearer token";
    private final UserInfo userInfo = new UserInfo(
        "case-officer@gmail.com",
        "id",
        newArrayList("caseworker-ia", "caseworker-ia-legalrep-solicitor"),
        "Case Officer",
        "Case",
        "Officer"
    );

    @BeforeEach
    void setupStubs() {

        when(requestTokenProvider.getAccessToken()).thenReturn(token);
        when(idamApi.userInfo(token)).thenReturn(userInfo);

    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_trigger_make_an_application() {

        addServiceAuthStub(server);
        addNotificationsApiTransformerStub(server);
        addRoleAssignmentActorStub(server);

        iaCaseApiClient.aboutToSubmit(
            callback()
                .event(MAKE_AN_APPLICATION)
                .caseDetails(
                    someCaseDetailsWith()
                        .id(4321)
                        .state(APPEAL_SUBMITTED)
                        .supplementaryData(Map.of("HMCTSServiceId","BFA1"))
                        .caseData(
                            anAsylumCase()
                                .with(APPEAL_TYPE, AppealType.PA)
                                .with(APPELLANT_GIVEN_NAMES, "some names")
                                .with(APPELLANT_FAMILY_NAME, "some family name")
                                .with(MAKE_AN_APPLICATION_TYPES, "some names")
                                .with(MAKE_AN_APPLICATION_DETAILS, MakeAnApplicationTypes.OTHER.toString())
                        )
                )
        );

        // assert that only two requests in total are sent to Idam API for user info data (one for the event,
        // one to determine if supplementary data need handling based on user role + journey type
        Mockito.verify(idamApi, times(1)).userInfo(token);
    }
}
