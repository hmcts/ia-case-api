package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentTag {

    CASE_ARGUMENT("caseArgument"),
    RESPONDENT_EVIDENCE("respondentEvidence"),
    APPEAL_RESPONSE("appealResponse"),
    APPEAL_SUBMISSION("appealSubmission"),
    ADDITIONAL_EVIDENCE("additionalEvidence"),
    REHEARD_HEARING_NOTICE("reheardHearingNotice"),
    HEARING_NOTICE("hearingNotice"),
    REHEARD_HEARING_NOTICE_LISTED("reheardHearingNoticeRelisted"),
    HEARING_NOTICE_LISTED("hearingNoticeRelisted"),
    HEARING_REQUIREMENTS("hearingRequirements"),
    CASE_SUMMARY("caseSummary"),
    HEARING_BUNDLE("hearingBundle"),
    UPDATED_HEARING_BUNDLE("updatedHearingBundle"),
    ADDENDUM_EVIDENCE("addendumEvidence"),
    DECISION_AND_REASONS_DRAFT("decisionAndReasons"),
    DECISION_AND_REASONS_COVER_LETTER("decisionAndReasonsCoverLetter"),
    FINAL_DECISION_AND_REASONS_PDF("finalDecisionAndReasonsPdf"),
    FINAL_DECISION_AND_REASONS_DOCUMENT("finalDecisionAndReasonsDocument"),
    APPEAL_SKELETON_BUNDLE("submitCaseBundle"),
    END_APPEAL("endAppeal"),
    FTPA_APPELLANT("ftpaAppellant"),
    FTPA_RESPONDENT("ftpaRespondent"),
    FTPA_DECISION_AND_REASONS("ftpaDecisionAndReasons"),
    FTPA_SET_ASIDE("ftpaSetAside"),
    HO_DECISION_LETTER("homeOfficeDecisionLetter"),
    APPEAL_FORM("appealForm"),
    SENSITIVE_DOCUMENT("sensitiveDocument"),
    RECORD_OUT_OF_TIME_DECISION_DOCUMENT("recordOutOfTimeDecisionDocument"),
    UPPER_TRIBUNAL_BUNDLE("upperTribunalBundle"),
    APPEAL_REASONS("appealReasons"),
    CLARIFYING_QUESTIONS("clarifyingQuestions"),
    END_APPEAL_AUTOMATICALLY("endAppealAutomatically"),
    UPDATED_FINAL_DECISION_AND_REASONS_PDF("updatedFinalDecisionAndReasonsPdf"),
    UPDATED_DECISION_AND_REASONS_COVER_LETTER("updatedDecisionAndReasonsCoverLetter"),
    ADA_SUITABILITY("adaSuitability"),
    INTERNAL_ADA_SUITABILITY("internalAdaSuitability"),
    NOTICE_OF_DECISION_UT_TRANSFER("noticeOfDecisionUtTransfer"),
    INTERNAL_EDIT_APPEAL_LETTER("internalEditAppealLetter"),
    UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT("upperTribunalTransferOrderDocument"),
    IAUT_2_FORM("iAUT2Form"),
    REMITTAL_DECISION("remittalDecision"),
    NOTICE_OF_ADJOURNED_HEARING("noticeOfAdjournedHearing"),
    APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT("appealWasNotSubmittedSupportingDocument"),
    INTERNAL_OUT_OF_TIME_DECISION_LETTER("internalOutOfTimeDecisionLetter"),

    @JsonEnumDefaultValue
    NONE("");

    @JsonValue
    private final String id;

    DocumentTag(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
