package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FtpaDisplayServiceTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseFlagAppender caseFlagAppender;

    private FtpaDisplayService ftpaDisplayService;

    @Before
    public void setUp() {

        ftpaDisplayService = new FtpaDisplayService(caseFlagAppender);
    }

    @Test
    public void should_return_the_correct_final_display_decisions_for_granted() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "refused")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "notAdmitted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "reheardRule35")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "reheardRule32")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "remadeRule32")).isEqualTo("granted");
    }

    @Test
    public void should_return_the_correct_final_display_decisions_for__partially_granted() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "refused")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "notAdmitted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "reheardRule35")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "reheardRule32")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "remadeRule32")).isEqualTo("granted");
    }

    @Test
    public void should_return_the_correct_final_display_decisions_for_refused() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "notAdmitted")).isEqualTo("notAdmitted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "remadeRule32")).isEqualTo("remadeRule32");
    }

    @Test
    public void should_return_the_correct_final_display_decisions_for_not_admitted() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "refused")).isEqualTo("refused");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "remadeRule32")).isEqualTo("remadeRule32");
    }

    @Test
    public void should_return_the_correct_final_display_decisions_for_reheard_rule35() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "refused")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "notAdmitted")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "remadeRule32")).isEqualTo("remadeRule32");

    }

    @Test
    public void should_return_the_correct_final_display_decisions_for_reheard_rule32() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "refused")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "notAdmitted")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule32", "remadeRule32")).isEqualTo("remadeRule32");

    }

    @Test
    public void should_return_the_correct_final_display_decisions_for_remade_rule32() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "refused")).isEqualTo("remadeRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "notAdmitted")).isEqualTo("remadeRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "remadeRule32")).isEqualTo("remadeRule32");
    }

    @Test
    public void should_return_correct_final_display_decision_as_allowed_for_refused_and_not_admitted() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_DECISION, "Allowed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "refused")).isEqualTo("allowed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "notAdmitted")).isEqualTo("allowed");
    }

    @Test
    public void should_return_correct_final_display_decision_as_dismissed_for_refused_and_not_admitted() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_DECISION, "Dismissed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "refused")).isEqualTo("dismissed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "notAdmitted")).isEqualTo("dismissed");
    }

    @Test
    public void should_write_correct_first_decision_to_asylum_case_when_not_present() {

        ftpaDisplayService.handleFtpaDecisions(asylumCase, "granted", "");
        verify(asylumCase, times(1)).write(FTPA_FIRST_DECISION, "granted");
    }

    @Test
    public void should_write_correct_second_decision_to_asylum_case_when_not_present() {

        ftpaDisplayService.handleFtpaDecisions(asylumCase, "refused", "granted");
        verify(asylumCase, times(1)).write(FTPA_SECOND_DECISION, "refused");
        verify(asylumCase, times(0)).write(FTPA_FIRST_DECISION, "granted");
    }

    @Test
    public void should_write_correct_final_display_decision_to_asylum_case() {

        ftpaDisplayService.setFinalDisplayDecision(asylumCase, "refused", "granted");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "granted");
    }

    @Test
    public void should_write_ftpa_case_flag_to_asylum_case_for_reheard_rule_32() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "reheardRule32");
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAGS, Collections.emptyList());
    }

    @Test
    public void should_write_ftpa_case_flag_to_asylum_case_for_reheard_rule_35() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "reheardRule35");
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAGS, Collections.emptyList());
    }

    @Test
    public void should_write_correct_flag_value_to_asylum_case_when_feature_flag_is_disabled() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, false, "reheardRule35");
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAGS, Collections.emptyList());
    }

    @Test
    public void should_not_write_ftpa_case_flag_to_asylum_case_for_a_decision_that_is_not_reheard() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "granted");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "partiallyGranted");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "refused");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "notAdmitted");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "remadeRule32");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAGS, Collections.emptyList());
    }
}
