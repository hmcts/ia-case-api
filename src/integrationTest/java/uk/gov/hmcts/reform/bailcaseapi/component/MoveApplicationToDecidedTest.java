package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_STATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.MOVE_APPLICATION_TO_DECIDED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State.DECISION_CONDITIONAL_BAIL;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.BailCaseForTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

class MoveApplicationToDecidedTest extends SpringBootIntegrationTest implements WithUserDetailsStub {

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void move_application_to_decided() {

        addAdminOfficerUserDetailsStub(server);

        PreSubmitCallbackResponseForTest response = iaBailCaseApiClient.aboutToStart(callback()
            .event(MOVE_APPLICATION_TO_DECIDED)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION_CONDITIONAL_BAIL)
                .caseData(BailCaseForTest.anBailCase()
                    .with(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, mock(Document.class))
                    .with(APPLICANT_GIVEN_NAMES, "some-given-name")
                    .with(APPLICANT_FAMILY_NAME, "some-family-name"))));

        String resultOutcome = response.getBailCase().read(OUTCOME_STATE, String.class).orElse("");

        assertThat(resultOutcome).isEqualTo(State.DECISION_DECIDED.toString());
    }
}
