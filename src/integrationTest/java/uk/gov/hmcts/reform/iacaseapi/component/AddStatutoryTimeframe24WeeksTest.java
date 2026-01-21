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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_REASON_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.XUI_BANNER_TEXT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

class AddStatutoryTimeframe24WeeksTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
        WithRoleAssignmentStub, WithServiceAuthStub {

    public static final String WEEK_STF_CASE_DEADLINE_28_MAY_2026 = "24 Week STF (28 May 2026)";
    public static final String WEEK_STF_CASE_DEADLINE_27_MAY_2026 = "24 Week STF (27 May 2026)";
    public static final String SOME_REASON = "some reason";
    public static final String SOME_GIVEN_NAME = "some-given-name";
    public static final String SOME_FAMILY_NAME = "some-family-name";
    private static final String APPEAL_SUBMISSION_DATE_STR = "2025-12-10";
    private static final String TRIBUNAL_SUBMISSION_DATE_STR = "2025-12-11";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void adds_a_statutory_timeframe_24_weeks() {
        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addRoleAssignmentQueryStub(server);
        String reason = "some reason";

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
                .event(ADD_STATUTORY_TIMEFRAME_24_WEEKS)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(anAsylumCase()
                                .with(STF_24W_CURRENT_REASON_AUTO_GENERATED, reason)
                                .with(APPEAL_SUBMISSION_DATE, APPEAL_SUBMISSION_DATE_STR)
                                .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                                .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<List<IdValue<CaseNote>>> caseNotes = response.getAsylumCase().read(CASE_NOTES);

        CaseNote caseNote = caseNotes.get().get(0).getValue();

        assertThat(caseNotes.get()).hasSize(1);
        assertThat(caseNote.getUser()).isEqualTo("Case Officer");
        assertThat(caseNote.getCaseNoteSubject()).isEqualTo("Setting statutory timeframe 24 weeks to - Yes");
        assertThat(caseNote.getCaseNoteDescription()).isEqualTo(reason);

        Optional<StatutoryTimeframe24Weeks> statutoryTimeframe24Weeks = response.getAsylumCase().read(STATUTORY_TIMEFRAME_24_WEEKS);
        List<IdValue<StatutoryTimeframe24WeeksHistory>> statutoryTimeframe24WeekHistory = statutoryTimeframe24Weeks.get().getHistory();

        assertThat(statutoryTimeframe24WeekHistory).hasSize(1);
        assertThat(statutoryTimeframe24WeekHistory.get(0).getValue().getUser()).isEqualTo("Case Officer");
        assertThat(statutoryTimeframe24WeekHistory.get(0).getValue().getStatus()).isEqualTo(YesOrNo.YES);
        assertThat(statutoryTimeframe24WeekHistory.get(0).getValue().getReason()).isEqualTo(reason);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void adds_astf_24_weeks_banner_text_with_tribunal_submission_date() {
        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addRoleAssignmentQueryStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
                .event(ADD_STATUTORY_TIMEFRAME_24_WEEKS)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(anAsylumCase()
                                .with(TRIBUNAL_RECEIVED_DATE, TRIBUNAL_SUBMISSION_DATE_STR)
                                .with(APPEAL_SUBMISSION_DATE, APPEAL_SUBMISSION_DATE_STR)
                                .with(STF_24W_CURRENT_REASON_AUTO_GENERATED, SOME_REASON)
                                .with(APPELLANT_GIVEN_NAMES, SOME_GIVEN_NAME)
                                .with(APPELLANT_FAMILY_NAME, SOME_FAMILY_NAME))));
        assertThat(response.getAsylumCase().read(XUI_BANNER_TEXT).get()).isEqualTo(WEEK_STF_CASE_DEADLINE_28_MAY_2026);
        assertThat(response.getAsylumCase().read(XUI_BANNER_TEXT).get()).isEqualTo(WEEK_STF_CASE_DEADLINE_28_MAY_2026);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void adds_astf_24_weeks_banner_text_with_appeal_submission_date() {
        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);
        addRoleAssignmentQueryStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
                .event(ADD_STATUTORY_TIMEFRAME_24_WEEKS)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(anAsylumCase()
                                .with(APPEAL_SUBMISSION_DATE, APPEAL_SUBMISSION_DATE_STR)
                                .with(STF_24W_CURRENT_REASON_AUTO_GENERATED, SOME_REASON)
                                .with(APPELLANT_GIVEN_NAMES, SOME_GIVEN_NAME)
                                .with(APPELLANT_FAMILY_NAME, SOME_FAMILY_NAME))));

        assertThat(response.getAsylumCase().read(XUI_BANNER_TEXT).get()).isEqualTo(WEEK_STF_CASE_DEADLINE_27_MAY_2026);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void adds_a_stf_24_should_have_correct_case_type_and_status_at_case_level() {
        addCaseWorkerUserDetailsStub(server);
        addServiceAuthStub(server);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
                .event(ADD_STATUTORY_TIMEFRAME_24_WEEKS)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(anAsylumCase()
                                .with(APPEAL_SUBMISSION_DATE, APPEAL_SUBMISSION_DATE_STR)
                                .with(STF_24W_CURRENT_REASON_AUTO_GENERATED, SOME_REASON)
                                .with(APPELLANT_GIVEN_NAMES, SOME_GIVEN_NAME)
                                .with(APPELLANT_FAMILY_NAME, SOME_FAMILY_NAME))));

        assertThat(response.getAsylumCase().read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class).get()).isEqualTo(YesOrNo.YES);
    }

}
