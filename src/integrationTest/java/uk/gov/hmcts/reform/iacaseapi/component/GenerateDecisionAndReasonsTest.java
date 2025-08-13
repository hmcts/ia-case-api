package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithDocumentApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

class GenerateDecisionAndReasonsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithDocumentApiStub {

    @MockBean
    UserDetailsProvider userDetailsProvider;

    @Mock
    private FeatureToggler featureToggler;

    @Mock
    private UserDetails userDetails;


    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void handles_generate_decision_and_reasons_event() {

        addServiceAuthStub(server);
        addDocumentApiTransformerStub(server);

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(GENERATE_DECISION_AND_REASONS)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response.getData()
            .read(DECISION_AND_REASONS_AVAILABLE, YesOrNo.class).get())
            .isEqualTo(YES);
    }
}
