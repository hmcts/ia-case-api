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
        assertEquals("", DocumentTag.NONE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, DocumentTag.values().length);
    }
}
