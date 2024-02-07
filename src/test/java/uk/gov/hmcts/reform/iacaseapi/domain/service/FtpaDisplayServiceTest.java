package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.FtpaDecisionCheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaDisplayServiceTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagAppender caseFlagAppender;
    @Mock
    List<IdValue<DocumentWithDescription>> maybeFtpaDecisionAndReasonsDocument;
    @Mock
    Document maybeFtpaApplicationDecisionAndReasonsDocument;
    @Mock
    List<IdValue<DocumentWithDescription>> maybeFtpaDecisionNoticeDocument;

    private FtpaDisplayService ftpaDisplayService;
    private final LocalDate now = LocalDate.now();
    private final FtpaDecisionCheckValues ftpaCheckValues =
            new FtpaDecisionCheckValues(List.of("specialReasons"),
                    List.of("countryGuidance"),
                    List.of("specialDifficulty"));

    @BeforeEach
    public void setUp() {

        ftpaDisplayService = new FtpaDisplayService(caseFlagAppender);
    }

    @Test
    void should_return_the_correct_final_display_decisions_for_granted() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "refused")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "notAdmitted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "reheardRule35"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "reheardRule32"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "remadeRule31"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "granted", "remadeRule32"))
            .isEqualTo("granted");
    }

    @Test
    void should_return_the_correct_final_display_decisions_for__partially_granted() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "granted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "refused"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "notAdmitted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "reheardRule35"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "reheardRule32"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "remadeRule31"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "partiallyGranted", "remadeRule32"))
            .isEqualTo("granted");
    }

    @Test
    void should_return_the_correct_final_display_decisions_for_refused() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "granted")).isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "notAdmitted"))
            .isEqualTo("notAdmitted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "reheardRule35"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "reheardRule32"))
            .isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "remadeRule31"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "remadeRule32"))
            .isEqualTo("remadeRule32");
    }

    @Test
    void should_return_the_correct_final_display_decisions_for_not_admitted() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "granted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "refused"))
            .isEqualTo("refused");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "reheardRule35"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "reheardRule32"))
            .isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "remadeRule31"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "remadeRule32"))
            .isEqualTo("remadeRule32");
    }

    @Test
    void should_return_the_correct_final_display_decisions_for_reheard_rule35() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "granted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "refused"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "notAdmitted"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "reheardRule35"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "reheardRule32"))
            .isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "remadeRule31"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "reheardRule35", "remadeRule32"))
            .isEqualTo("remadeRule32");

    }

    @Test
    void should_return_the_correct_final_display_decisions_for_reheard_rule32() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "granted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "refused"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "notAdmitted"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "reheardRule35"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "reheardRule32"))
            .isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "remadeRule31"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule31", "remadeRule32"))
            .isEqualTo("remadeRule32");

    }

    @Test
    void should_return_the_correct_final_display_decisions_for_remade_rule32() {

        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "granted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "partiallyGranted"))
            .isEqualTo("granted");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "refused"))
            .isEqualTo("remadeRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "notAdmitted"))
            .isEqualTo("remadeRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "reheardRule35"))
            .isEqualTo("reheardRule35");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "reheardRule32"))
            .isEqualTo("reheardRule32");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "remadeRule31"))
            .isEqualTo("remadeRule31");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "remadeRule32", "remadeRule32"))
            .isEqualTo("remadeRule32");
    }

    @Test
    void should_return_correct_final_display_decision_as_allowed_for_refused_and_not_admitted() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_DECISION, "Allowed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "refused")).isEqualTo("allowed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "notAdmitted"))
            .isEqualTo("allowed");
    }

    @Test
    void should_return_correct_final_display_decision_as_dismissed_for_refused_and_not_admitted() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_DECISION, "Dismissed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "refused", "refused")).isEqualTo("dismissed");
        assertThat(ftpaDisplayService.getFinalDisplayDecision(asylumCase, "notAdmitted", "notAdmitted"))
            .isEqualTo("dismissed");
    }

    @Test
    void should_write_correct_first_decision_to_asylum_case_when_not_present() {

        ftpaDisplayService.handleFtpaDecisions(asylumCase, "granted", "");
        verify(asylumCase, times(1)).write(FTPA_FIRST_DECISION, "granted");
        verify(asylumCase, times(1)).write(SECOND_FTPA_DECISION_EXISTS, YesOrNo.NO);
    }

    @Test
    void should_write_correct_second_decision_to_asylum_case_when_not_present() {

        ftpaDisplayService.handleFtpaDecisions(asylumCase, "refused", "granted");
        verify(asylumCase, times(1)).write(FTPA_SECOND_DECISION, "refused");
        verify(asylumCase, times(0)).write(FTPA_FIRST_DECISION, "granted");
        verify(asylumCase, times(1)).write(SECOND_FTPA_DECISION_EXISTS, YesOrNo.YES);
    }

    @Test
    void should_write_correct_final_display_decision_to_asylum_case() {

        ftpaDisplayService.setFinalDisplayDecision(asylumCase, "refused", "granted");
        verify(asylumCase, times(1)).write(FTPA_FINAL_DECISION_FOR_DISPLAY, "granted");
    }

    @Test
    void should_write_ftpa_case_flag_to_asylum_case_for_reheard_rule_32() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "reheardRule32");
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "");
        verify(asylumCase, times(1)).write(LEGACY_CASE_FLAGS, Collections.emptyList());
    }

    @Test
    void should_write_ftpa_case_flag_to_asylum_case_for_reheard_rule_35() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "reheardRule35");
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "");
        verify(asylumCase, times(1)).write(LEGACY_CASE_FLAGS, Collections.emptyList());
    }

    @Test
    void should_write_correct_flag_value_to_asylum_case_when_feature_flag_is_disabled() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, false, "reheardRule35");
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LEGACY_CASE_FLAGS, Collections.emptyList());
    }

    @Test
    void should_not_write_ftpa_case_flag_to_asylum_case_for_a_decision_that_is_not_reheard() {

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "granted");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LEGACY_CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "partiallyGranted");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LEGACY_CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "refused");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LEGACY_CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "notAdmitted");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LEGACY_CASE_FLAGS, Collections.emptyList());

        ftpaDisplayService.setFtpaCaseFlag(asylumCase, true, "remadeRule32");
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LEGACY_CASE_FLAGS, Collections.emptyList());
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void should_map_ftpa_decision_respondent_maximum_data(boolean isMigration) {
        final FtpaApplications ftpaApplication = FtpaApplications.builder().ftpaApplicant("respondent").build();

        when(asylumCase.read(IS_FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE, YesOrNo.class))
                .thenReturn(Optional.of(YES));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
                .thenReturn(Optional.of("granted"));
        if (isMigration) {
            when(asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT))
                    .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
        } else {
            when(asylumCase.read(FTPA_APPLICATION_RESPONDENT_DOCUMENT, Document.class))
                    .thenReturn(Optional.of(maybeFtpaApplicationDecisionAndReasonsDocument));
        }

        when(asylumCase.read(FTPA_RESPONDENT_NOTICE_DOCUMENT))
                .thenReturn(Optional.of(maybeFtpaDecisionNoticeDocument));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OBJECTIONS, String.class))
                .thenReturn(Optional.of("Objection description example"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_LST_INS, String.class))
                .thenReturn(Optional.of("Listing instructions example"));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_NOTES_DESCRIPTION, String.class))
                .thenReturn(Optional.of("Information for UT example"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_DATE, String.class))
                .thenReturn(Optional.of(now.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_NOTES_POINTS))
                .thenReturn(Optional.of(ftpaCheckValues));

        ftpaDisplayService.mapFtpaDecision(isMigration, asylumCase, "RESPONDENT", ftpaApplication);

        assertEquals(YES, ftpaApplication.getIsFtpaNoticeOfDecisionSetAside());
        assertEquals("granted", ftpaApplication.getFtpaDecisionOutcomeType());
        if (isMigration) {
            assertEquals(maybeFtpaDecisionAndReasonsDocument, ftpaApplication.getFtpaLegacyDecisionDocument());
        } else {
            assertEquals(maybeFtpaApplicationDecisionAndReasonsDocument, ftpaApplication.getFtpaNewDecisionDocument());
        }
        assertEquals(maybeFtpaDecisionNoticeDocument, ftpaApplication.getFtpaNoticeDocument());
        assertEquals("Objection description example", ftpaApplication.getFtpaDecisionObjections());
        assertEquals("Listing instructions example", ftpaApplication.getFtpaDecisionLstIns());
        assertEquals("Information for UT example", ftpaApplication.getFtpaDecisionNotesDescription());
        assertEquals(now.toString(), ftpaApplication.getFtpaDecisionDate());
        assertEquals(ftpaCheckValues, ftpaApplication.getFtpaDecisionNotesPoints());
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void should_map_ftpa_decision_appellant_minimum_data(boolean isMigration) {
        final FtpaApplications ftpaApplication = FtpaApplications.builder().ftpaApplicant("appellant").build();

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
                .thenReturn(Optional.of("remadeRule32"));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, String.class)).thenReturn(Optional.of("Allowed"));
        if (isMigration) {
            when(asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT))
                    .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
        } else {
            when(asylumCase.read(FTPA_APPLICATION_APPELLANT_DOCUMENT, Document.class))
                    .thenReturn(Optional.of(maybeFtpaApplicationDecisionAndReasonsDocument));
        }

        when(asylumCase.read(FTPA_APPELLANT_NOTICE_DOCUMENT))
                .thenReturn(Optional.of(maybeFtpaDecisionNoticeDocument));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OBJECTIONS, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_DECISION_LST_INS, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_NOTES_DESCRIPTION, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_DECISION_DATE, String.class))
                .thenReturn(Optional.of(now.toString()));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_NOTES_POINTS))
                .thenReturn(Optional.empty());

        ftpaDisplayService.mapFtpaDecision(isMigration, asylumCase, "APPELLANT", ftpaApplication);

        assertEquals(NO, ftpaApplication.getIsFtpaNoticeOfDecisionSetAside());
        assertEquals("Allowed", ftpaApplication.getFtpaDecisionRemadeRule32());
        assertEquals("remadeRule32", ftpaApplication.getFtpaDecisionOutcomeType());

        if (isMigration) {
            assertEquals(maybeFtpaDecisionAndReasonsDocument, ftpaApplication.getFtpaLegacyDecisionDocument());
        } else {
            assertEquals(maybeFtpaApplicationDecisionAndReasonsDocument, ftpaApplication.getFtpaNewDecisionDocument());
        }
        assertEquals(maybeFtpaDecisionNoticeDocument, ftpaApplication.getFtpaNoticeDocument());
        assertNull(ftpaApplication.getFtpaDecisionObjections());
        assertNull(ftpaApplication.getFtpaDecisionLstIns());
        assertNull(ftpaApplication.getFtpaDecisionNotesDescription());
        assertEquals(now.toString(), ftpaApplication.getFtpaDecisionDate());
        assertNull(ftpaApplication.getFtpaDecisionNotesPoints());
    }
}
