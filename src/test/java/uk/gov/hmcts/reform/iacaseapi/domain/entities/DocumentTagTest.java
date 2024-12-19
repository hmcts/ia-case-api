package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DocumentTagTest {

    @Test
    void has_correct_values() {
        assertEquals("caseArgument", DocumentTag.CASE_ARGUMENT.toString());
        assertEquals("respondentEvidence", DocumentTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("appealResponse", DocumentTag.APPEAL_RESPONSE.toString());
        assertEquals("appealSubmission", DocumentTag.APPEAL_SUBMISSION.toString());
        assertEquals("additionalEvidence", DocumentTag.ADDITIONAL_EVIDENCE.toString());
        assertEquals("reheardHearingNotice", DocumentTag.REHEARD_HEARING_NOTICE.toString());
        assertEquals("hearingNotice", DocumentTag.HEARING_NOTICE.toString());
        assertEquals("reheardHearingNoticeRelisted", DocumentTag.REHEARD_HEARING_NOTICE_LISTED.toString());
        assertEquals("hearingNoticeRelisted", DocumentTag.HEARING_NOTICE_LISTED.toString());
        assertEquals("hearingRequirements", DocumentTag.HEARING_REQUIREMENTS.toString());
        assertEquals("caseSummary", DocumentTag.CASE_SUMMARY.toString());
        assertEquals("hearingBundle", DocumentTag.HEARING_BUNDLE.toString());
        assertEquals("updatedHearingBundle", DocumentTag.UPDATED_HEARING_BUNDLE.toString());
        assertEquals("addendumEvidence", DocumentTag.ADDENDUM_EVIDENCE.toString());
        assertEquals("decisionAndReasons", DocumentTag.DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("decisionAndReasonsCoverLetter", DocumentTag.DECISION_AND_REASONS_COVER_LETTER.toString());
        assertEquals("finalDecisionAndReasonsPdf", DocumentTag.FINAL_DECISION_AND_REASONS_PDF.toString());
        assertEquals("finalDecisionAndReasonsDocument", DocumentTag.FINAL_DECISION_AND_REASONS_DOCUMENT.toString());
        assertEquals("submitCaseBundle", DocumentTag.APPEAL_SKELETON_BUNDLE.toString());
        assertEquals("endAppeal", DocumentTag.END_APPEAL.toString());
        assertEquals("ftpaAppellant", DocumentTag.FTPA_APPELLANT.toString());
        assertEquals("ftpaRespondent", DocumentTag.FTPA_RESPONDENT.toString());
        assertEquals("ftpaDecisionAndReasons", DocumentTag.FTPA_DECISION_AND_REASONS.toString());
        assertEquals("homeOfficeDecisionLetter", DocumentTag.HO_DECISION_LETTER.toString());
        assertEquals("sensitiveDocument", DocumentTag.SENSITIVE_DOCUMENT.toString());
        assertEquals("recordOutOfTimeDecisionDocument", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT.toString());
        assertEquals("upperTribunalBundle", DocumentTag.UPPER_TRIBUNAL_BUNDLE.toString());
        assertEquals("appealReasons", DocumentTag.APPEAL_REASONS.toString());
        assertEquals("clarifyingQuestions", DocumentTag.CLARIFYING_QUESTIONS.toString());
        assertEquals("endAppealAutomatically", DocumentTag.END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("appealForm", DocumentTag.APPEAL_FORM.toString());
        assertEquals("noticeOfDecisionUtTransfer", DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER.toString());
        assertEquals("internalAdaSuitability", DocumentTag.INTERNAL_ADA_SUITABILITY.toString());
        assertEquals("upperTribunalTransferOrderDocument", DocumentTag.UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT.toString());
        assertEquals("iAUT2Form", DocumentTag.IAUT_2_FORM.toString());
        assertEquals("ftpaSetAside", DocumentTag.FTPA_SET_ASIDE.toString());
        assertEquals("updatedFinalDecisionAndReasonsPdf", DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF.toString());
        assertEquals("updatedDecisionAndReasonsCoverLetter", DocumentTag.UPDATED_DECISION_AND_REASONS_COVER_LETTER.toString());
        assertEquals("remittalDecision", DocumentTag.REMITTAL_DECISION.toString());
        assertEquals("noticeOfAdjournedHearing", DocumentTag.NOTICE_OF_ADJOURNED_HEARING.toString());
        assertEquals("", DocumentTag.NONE.toString());
        assertEquals("appealWasNotSubmittedSupportingDocument", DocumentTag.APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT.toString());
        assertEquals("internalOutOfTimeDecisionLetter", DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER.toString());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(45, DocumentTag.values().length);
    }
}
