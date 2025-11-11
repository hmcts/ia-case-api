package uk.gov.hmcts.reform.iacaseapi.component;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

class RemoveStatutoryTimeframe24WeeksTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
    WithRoleAssignmentStub, WithServiceAuthStub {

    @Test
    @WithMockUser(authorities = {"caseworker-ia-iacjudge"})
    void removes_a_statutory_timeframe_24_weeks() {
        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addRoleAssignmentActorStub(server);
        String reason = "some reason";
        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(STATUTORY_TIMEFRAME_24_WEEKS_REASON, reason)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<List<IdValue<CaseNote>>> caseNotes = response.getAsylumCase().read(CASE_NOTES);

        CaseNote caseNote = caseNotes.get().get(0).getValue();

        assertThat(caseNotes.get()).hasSize(1);
        assertThat(caseNote.getUser()).isEqualTo("Case Officer");
        assertThat(caseNote.getCaseNoteSubject()).isEqualTo("Setting statutory timeframe 24 weeks to No");
        assertThat(caseNote.getCaseNoteDescription()).isEqualTo(reason);

        Optional<List<IdValue<StatutoryTimeframe24Weeks>>> statutoryTimeframe24Weeks = response.getAsylumCase().read(STATUTORY_TIMEFRAME_24_WEEKS);

        StatutoryTimeframe24Weeks statutoryTimeframe24Week = statutoryTimeframe24Weeks.get().get(0).getValue();

        assertThat(statutoryTimeframe24Weeks.get()).hasSize(1);
        assertThat(statutoryTimeframe24Week.getUser()).isEqualTo("Case Officer");
        assertThat(statutoryTimeframe24Week.getStatus()).isEqualTo(YesOrNo.NO);
        assertThat(statutoryTimeframe24Week.getReason()).isEqualTo(reason);
    }
}
