package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class AddCaseNoteTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
    WithRoleAssignmentStub, WithServiceAuthStub {

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void adds_a_case_note() {
        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addRoleAssignmentActorStub(server);
        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(ADD_CASE_NOTE)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(ADD_CASE_NOTE_SUBJECT, "some-subject")
                    .with(ADD_CASE_NOTE_DESCRIPTION, "some-description")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<List<IdValue<CaseNote>>> caseNotes = response.getAsylumCase().read(CASE_NOTES);

        CaseNote caseNote = caseNotes.get().get(0).getValue();

        assertThat(caseNotes.get()).hasSize(1);
        assertThat(caseNote.getUser()).isEqualTo("Case Officer");
        assertThat(caseNote.getCaseNoteSubject()).isEqualTo("some-subject");
        assertThat(caseNote.getCaseNoteDescription()).isEqualTo("some-description");
    }
}
