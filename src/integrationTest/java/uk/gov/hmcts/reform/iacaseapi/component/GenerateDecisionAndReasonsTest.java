package uk.gov.hmcts.reform.iacaseapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import java.time.LocalDateTime;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.PreSubmitCallbackResponseForTest;

public class GenerateDecisionAndReasonsTest extends SpringBootIntegrationTest {

    @Test
    public void handles_generate_decision_and_reasons_event() {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .id("1")
            .email("some-email@email.com")
            .forename("Case")
            .surname("Officer"));

        given.theDocumentsApiWillRespondWithoutAdditionalCaseData();

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(GENERATE_DECISION_AND_REASONS)
            .caseDetails(someCaseDetailsWith()
                .id(1)
                .state(DECISION)
                .createdDate(LocalDateTime.now())
                .jurisdiction("IA")
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        then.theDocumentsApiReceivesACcdAboutToSubmitCallback();

        assertThat(response.getData()
            .read(DECISION_AND_REASONS_AVAILABLE, YesOrNo.class).get())
            .isEqualTo(YES);
    }
}
