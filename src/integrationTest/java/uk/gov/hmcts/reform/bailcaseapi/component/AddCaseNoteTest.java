package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.ADD_CASE_NOTE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.ADD_CASE_NOTE_SUBJECT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPLICATION_SUBMITTED;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.BailCaseForTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class AddCaseNoteTest extends SpringBootIntegrationTest implements WithUserDetailsStub {

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void adds_a_case_note() {

        addAdminOfficerUserDetailsStub(server);

        PreSubmitCallbackResponseForTest response = iaBailCaseApiClient.aboutToSubmit(callback()
            .event(ADD_CASE_NOTE)
            .caseDetails(someCaseDetailsWith()
                .state(APPLICATION_SUBMITTED)
                .caseData(BailCaseForTest.anBailCase()
                    .with(ADD_CASE_NOTE_SUBJECT, "some-subject")
                    .with(ADD_CASE_NOTE_DESCRIPTION, "some-description")
                    .with(APPLICANT_GIVEN_NAMES, "some-given-name")
                    .with(APPLICANT_FAMILY_NAME, "some-family-name"))));

        Optional<List<IdValue<CaseNote>>> caseNotes = response.getBailCase().read(CASE_NOTES);

        CaseNote caseNote = caseNotes.get().get(0).getValue();

        assertThat(caseNotes.get().size()).isEqualTo(1);
        assertThat(caseNote.getUser()).isEqualTo("Admin Officer");
        assertThat(caseNote.getCaseNoteSubject()).isEqualTo("some-subject");
        assertThat(caseNote.getCaseNoteDescription()).isEqualTo("some-description");
    }
}
