package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTE_SUBJECT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class AddCaseNoteTest extends SpringBootIntegrationTest implements WithUserDetailsStub {

    private String caseType = "Asylum";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void adds_a_case_note(
        @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        addCaseWorkerUserDetailsStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(ADD_CASE_NOTE)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseType(caseType)
                .caseData(anAsylumCase()
                    .with(ADD_CASE_NOTE_SUBJECT, "some-subject")
                    .with(ADD_CASE_NOTE_DESCRIPTION, "some-description")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<List<IdValue<CaseNote>>> caseNotes = response.getAsylumCase().read(CASE_NOTES);

        CaseNote caseNote = caseNotes.get().get(0).getValue();

        assertThat(caseNotes.get().size()).isEqualTo(1);
        assertThat(caseNote.getUser()).isEqualTo("Case Officer");
        assertThat(caseNote.getCaseNoteSubject()).isEqualTo("some-subject");
        assertThat(caseNote.getCaseNoteDescription()).isEqualTo("some-description");
    }
}
