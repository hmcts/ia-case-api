package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class DocumentTagTest {

    @Test
    public void has_correct_values() {
        assertEquals("caseArgument", DocumentTag.CASE_ARGUMENT.toString());
        assertEquals("respondentEvidence", DocumentTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("appealResponse", DocumentTag.APPEAL_RESPONSE.toString());
        assertEquals("appealSubmission", DocumentTag.APPEAL_SUBMISSION.toString());
        assertEquals("additionalEvidence", DocumentTag.ADDITIONAL_EVIDENCE.toString());
        assertEquals("hearingNotice", DocumentTag.HEARING_NOTICE.toString());
        assertEquals("caseSummary", DocumentTag.CASE_SUMMARY.toString());
        assertEquals("hearingBundle", DocumentTag.HEARING_BUNDLE.toString());
        assertEquals("addendumEvidence", DocumentTag.ADDENDUM_EVIDENCE.toString());
        assertEquals("decisionAndReasons", DocumentTag.DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("decisionAndReasonsCoverLetter", DocumentTag.DECISION_AND_REASONS_COVER_LETTER.toString());
        assertEquals("finalDecisionAndReasonsPdf", DocumentTag.FINAL_DECISION_AND_REASONS_PDF.toString());
        assertEquals("submitCaseBundle", DocumentTag.APPEAL_SKELETON_BUNDLE.toString());
        assertEquals("endAppeal", DocumentTag.END_APPEAL.toString());
        assertEquals("", DocumentTag.NONE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(15, DocumentTag.values().length);
    }
}
