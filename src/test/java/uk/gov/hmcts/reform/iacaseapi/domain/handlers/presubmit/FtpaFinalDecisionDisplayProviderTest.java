package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaFinalDecisionDisplayProviderTest {

    @Mock private AsylumCase asylumCase;

    FtpaFinalDecisionDisplayProvider ftpaFinalDecisionDisplayProvider;

    @BeforeEach
    void setUp() {

        ftpaFinalDecisionDisplayProvider = new FtpaFinalDecisionDisplayProvider();
    }

    @Test
    void should_return_the_correct_final_display_decisions() {

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "refused")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "notAdmitted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "reheardRule35")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "reheardRule32")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "granted", "remadeRule32")).isEqualTo("granted");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "refused")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "notAdmitted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "reheardRule35")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "reheardRule32")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "partiallyGranted", "remadeRule32")).isEqualTo("granted");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "notAdmitted")).isEqualTo("notAdmitted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "remadeRule32")).isEqualTo("remadeRule32");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "refused")).isEqualTo("refused");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "remadeRule32")).isEqualTo("remadeRule32");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "refused")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "notAdmitted")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule35", "remadeRule32")).isEqualTo("remadeRule32");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "refused")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "notAdmitted")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "reheardRule32", "remadeRule32")).isEqualTo("remadeRule32");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "granted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "partiallyGranted")).isEqualTo("granted");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "refused")).isEqualTo("remadeRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "notAdmitted")).isEqualTo("remadeRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "reheardRule35")).isEqualTo("reheardRule35");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "reheardRule32")).isEqualTo("reheardRule32");
        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "remadeRule32", "remadeRule32")).isEqualTo("remadeRule32");
    }

    @Test
    void should_return_correct_final_display_decision_as_allowed_for_refused_and_not_admitted() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_DECISION, "Allowed");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "refused")).isEqualTo("allowed");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "notAdmitted")).isEqualTo("allowed");
    }

    @Test
    void should_return_correct_final_display_decision_as_dismissed_for_refused_and_not_admitted() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_DECISION, "Dismissed");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "refused", "refused")).isEqualTo("dismissed");

        assertThat(ftpaFinalDecisionDisplayProvider.getFinalDisplayDecision(asylumCase, "notAdmitted", "notAdmitted")).isEqualTo("dismissed");
    }

    @Test
    void should_write_correct_first_decision_to_asylum_case_when_not_present() {

        ftpaFinalDecisionDisplayProvider.handleFtpaDecisions(asylumCase, "granted", "");

        verify(asylumCase, times(1)).write(FTPA_FIRST_DECISION, "granted");
    }

    @Test
    void should_write_correct_second_decision_to_asylum_case_when_not_present() {

        ftpaFinalDecisionDisplayProvider.handleFtpaDecisions(asylumCase, "refused", "granted");

        verify(asylumCase, times(1)).write(FTPA_SECOND_DECISION, "refused");

        verify(asylumCase, times(0)).write(FTPA_FIRST_DECISION, "granted");
    }

    @Test
    void should_write_correct_final_display_decision_to_asylum_case() {

        ftpaFinalDecisionDisplayProvider.setFinalDisplayDecision(asylumCase, "refused", "granted");

        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "granted");
    }
}
