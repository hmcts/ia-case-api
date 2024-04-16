package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DocumentTagTest {

    @Test
    void has_correct_values() {

        assertEquals("uploadTheBailEvidenceDocs", DocumentTag.BAIL_EVIDENCE.toString());
        assertEquals("applicationSubmission", DocumentTag.APPLICATION_SUBMISSION.toString());
        assertEquals("uploadBailSummary", DocumentTag.BAIL_SUMMARY.toString());
        assertEquals("signedDecisionNotice", DocumentTag.SIGNED_DECISION_NOTICE.toString());
        assertEquals("bailDecisionUnsigned", DocumentTag.BAIL_DECISION_UNSIGNED.toString());
        assertEquals("uploadDocument", DocumentTag.UPLOAD_DOCUMENT.toString());
        assertEquals("bailSubmission", DocumentTag.BAIL_SUBMISSION.toString());
        assertEquals("b1Document", DocumentTag.B1_DOCUMENT.toString());
        assertEquals("bailEndApplication", DocumentTag.BAIL_END_APPLICATION.toString());
        assertEquals("bailNoticeOfHearing", DocumentTag.BAIL_NOTICE_OF_HEARING.toString());
        assertEquals("", DocumentTag.NONE.toString());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(11, DocumentTag.values().length);
    }

}
