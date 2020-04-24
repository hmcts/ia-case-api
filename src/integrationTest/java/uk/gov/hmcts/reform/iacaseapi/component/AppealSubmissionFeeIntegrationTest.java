package uk.gov.hmcts.reform.iacaseapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.*;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.*;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

@Slf4j
public class AppealSubmissionFeeIntegrationTest extends SpringBootIntegrationTest {

    @Test
    public void should_retrieve_the_fee_amount_for_the_appeal() {

        given.someLoggedIn(userWith()
                .roles(newHashSet("caseworker-ia", "caseworker-ia-legalrep-solicitor")));

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToStart(callback()
            .event(Event.SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "HO12345")
                    .with(HOME_OFFICE_DECISION_DATE, "2020-04-20")
                    .with(APPEAL_TYPE, "refusalOfEu"))));

        assertThat(response.getData()
            .read(APPEAL_FEE_DESC, String.class).get())
            .isEqualTo("The fee for this type of appeal with a hearing is £140.00");
        assertThat(response.getData()
            .read(FEE_AMOUNT_FOR_DISPLAY, String.class).get())
            .isEqualTo("£140.00");
    }
}
