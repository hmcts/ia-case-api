package uk.gov.hmcts.reform.iacaseapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.PreSubmitCallbackResponseForTest;

public class RecordAttendeesAndDurationTest extends SpringBootIntegrationTest {

    @Test
    public void sets_flag_to_indicate_the_hearing_details_have_been_recorded() {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-admofficer"))
            .forename("Admin")
            .surname("Officer"));

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(Event.RECORD_ATTENDEES_AND_DURATION)
            .caseDetails(someCaseDetailsWith()
                .state(State.DECISION)
                .caseData(anAsylumCase()
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<YesOrNo> hearingDetailsRecordedFlag =
            response.getAsylumCase().read(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);

        assertThat(hearingDetailsRecordedFlag.get()).isEqualTo(YES);
    }

    @Test
    public void returns_confirmation_page_content() {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-admofficer"))
            .forename("Admin")
            .surname("Officer"));

        PostSubmitCallbackResponseForTest response = iaCaseApiClient.ccdSubmitted(callback()
            .event(Event.RECORD_ATTENDEES_AND_DURATION)
            .caseDetails(someCaseDetailsWith()
                .state(State.DECISION)
                .caseData(anAsylumCase())));

        assertThat(response.getConfirmationHeader().get()).isEqualTo("# You have recorded the attendees and duration of the hearing");
        assertThat(response.getConfirmationBody().get()).contains("The judge will record the decision and reasons.");
        assertThat(response.getConfirmationBody().get()).contains("You don't need to do anything more with this case.");
    }
}
