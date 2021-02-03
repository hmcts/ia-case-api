package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class RecordAttendeesAndDurationTest extends SpringBootIntegrationTest implements WithServiceAuthStub {

    private String caseType = "Asylum";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    public void sets_flag_to_indicate_the_hearing_details_have_been_recorded(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        addServiceAuthStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(Event.RECORD_ATTENDEES_AND_DURATION)
            .caseDetails(someCaseDetailsWith()
                .state(State.DECISION)
                .caseType(caseType)
                .caseData(anAsylumCase()
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<YesOrNo> hearingDetailsRecordedFlag =
            response.getAsylumCase().read(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);

        assertThat(hearingDetailsRecordedFlag.get()).isEqualTo(YES);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    public void returns_confirmation_page_content(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        addServiceAuthStub(server);
        PostSubmitCallbackResponseForTest response = iaCaseApiClient.ccdSubmitted(callback()
            .event(Event.RECORD_ATTENDEES_AND_DURATION)
            .caseDetails(someCaseDetailsWith()
                .state(State.DECISION)
                .caseType(caseType)
                .caseData(anAsylumCase())));

        assertThat(response.getConfirmationHeader().get())
            .isEqualTo("# You have recorded the attendees and duration of the hearing");
        assertThat(response.getConfirmationBody().get()).contains("You don't need to do anything more with this case.");
    }
}
