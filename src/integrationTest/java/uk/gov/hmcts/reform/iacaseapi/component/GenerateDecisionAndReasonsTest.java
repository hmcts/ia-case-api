package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_AND_REASONS_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithDocumentApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class GenerateDecisionAndReasonsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithDocumentApiStub {

    private String caseType = "Asylum";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void handles_generate_decision_and_reasons_event(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        addServiceAuthStub(server);
        addDocumentApiTransformerStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(GENERATE_DECISION_AND_REASONS)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseType(caseType)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response.getData()
            .read(DECISION_AND_REASONS_AVAILABLE, YesOrNo.class).get())
            .isEqualTo(YES);
    }
}
