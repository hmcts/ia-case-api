package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    SEND_DIRECTION("sendDirection"),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence"),
    UPLOAD_RESPONDENT_EVIDENCE("uploadRespondentEvidence"),
    UPLOAD_HOME_OFFICE_BUNDLE("uploadHomeOfficeBundle"),
    BUILD_CASE("buildCase"),
    SUBMIT_CASE("submitCase"),
    REQUEST_CASE_EDIT("requestCaseEdit"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    ADD_APPEAL_RESPONSE("addAppealResponse"),
    UPLOAD_HOME_OFFICE_APPEAL_RESPONSE("uploadHomeOfficeAppealResponse"),
    REQUEST_HEARING_REQUIREMENTS("requestHearingRequirements"),
    REQUEST_HEARING_REQUIREMENTS_FEATURE("requestHearingRequirementsFeature"),
    REVIEW_HEARING_REQUIREMENTS("reviewHearingRequirements"),
    DRAFT_HEARING_REQUIREMENTS("draftHearingRequirements"),
    UPDATE_HEARING_REQUIREMENTS("updateHearingRequirements"),
    UPDATE_HEARING_ADJUSTMENTS("updateHearingAdjustments"),
    CHANGE_DIRECTION_DUE_DATE("changeDirectionDueDate"),
    UPLOAD_ADDITIONAL_EVIDENCE("uploadAdditionalEvidence"),
    UPLOAD_ADDENDUM_EVIDENCE("uploadAddendumEvidence"),
    LIST_CASE("listCase"),
    LIST_CASE_WITHOUT_HEARING_REQUIREMENTS("listCaseWithoutHearingRequirements"),
    CREATE_CASE_SUMMARY("createCaseSummary"),
    REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE("revertStateToAwaitingRespondentEvidence"),
    GENERATE_HEARING_BUNDLE("generateHearingBundle"),
    CUSTOMISE_HEARING_BUNDLE("customiseHearingBundle"),
    ASYNC_STITCHING_COMPLETE("asyncStitchingComplete"),
    DECISION_AND_REASONS_STARTED("decisionAndReasonsStarted"),
    GENERATE_DECISION_AND_REASONS("generateDecisionAndReasons"),
    SEND_DECISION_AND_REASONS("sendDecisionAndReasons"),
    ADD_CASE_NOTE("addCaseNote"),
    EDIT_CASE_LISTING("editCaseListing"),
    RECORD_APPLICATION("recordApplication"),
    RECORD_ATTENDEES_AND_DURATION("recordAttendeesAndDuration"),
    UPLOAD_HEARING_RECORDING("uploadHearingRecording"),
    END_APPEAL("endAppeal"),
    REQUEST_CASE_BUILDING("requestCaseBuilding"),
    FORCE_REQUEST_CASE_BUILDING("forceRequestCaseBuilding"),
    UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE("uploadAdditionalEvidenceHomeOffice"),
    REQUEST_RESPONSE_REVIEW("requestResponseReview"),
    REQUEST_RESPONSE_AMEND("requestResponseAmend"),
    UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP("uploadAddendumEvidenceLegalRep"),
    UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE("uploadAddendumEvidenceHomeOffice"),
    UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER("uploadAddendumEvidenceAdminOfficer"),
    SUBMIT_HEARING_REQUIREMENTS("submitHearingRequirements"),
    REMOVE_APPEAL_FROM_ONLINE("removeAppealFromOnline"),
    SHARE_A_CASE("shareACase"),
    REQUEST_REASONS_FOR_APPEAL("requestReasonsForAppeal"),
    EDIT_REASONS_FOR_APPEAL("editReasonsForAppeal"),
    SUBMIT_REASONS_FOR_APPEAL("submitReasonsForAppeal"),
    REQUEST_CLARIFYING_ANSWERS("requestClarifyingAnswers"),
    SUBMIT_CLARIFYING_QUESTION_ANSWERS("submitClarifyingQuestionAnswers"),
    EDIT_DOCUMENTS("editDocuments"),
    SEND_TO_PREHEARING("sendToPreHearing"),
    CHANGE_HEARING_CENTRE("changeHearingCentre"),
    APPLY_FOR_FTPA_APPELLANT("applyForFTPAAppellant"),
    APPLY_FOR_FTPA_RESPONDENT("applyForFTPARespondent"),
    LEADERSHIP_JUDGE_FTPA_DECISION("leadershipJudgeFtpaDecision"),
    RESIDENT_JUDGE_FTPA_DECISION("residentJudgeFtpaDecision"),
    RECORD_ALLOCATED_JUDGE("recordAllocatedJudge"),
    EDIT_TIME_EXTENSION("editTimeExtension"),
    SUBMIT_TIME_EXTENSION("submitTimeExtension"),
    REVIEW_TIME_EXTENSION("reviewTimeExtension"),
    SEND_DIRECTION_WITH_QUESTIONS("sendDirectionWithQuestions"),
    FLAG_CASE("flagCase"),
    REMOVE_FLAG("removeFlag"),
    REQUEST_CMA_REQUIREMENTS("requestCmaRequirements"),
    EDIT_CMA_REQUIREMENTS("editCmaRequirements"),
    SUBMIT_CMA_REQUIREMENTS("submitCmaRequirements"),
    REVIEW_CMA_REQUIREMENTS("reviewCmaRequirements"),
    FORCE_CASE_TO_CASE_UNDER_REVIEW("forceCaseToCaseUnderReview"),
    UPDATE_DETAILS_AFTER_CMA("updateDetailsAfterCma"),
    FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS("forceCaseToSubmitHearingRequirements"),
    UPDATE_LEGAL_REPRESENTATIVES_DETAILS("updateLegalRepDetails"),
    ADJOURN_HEARING_WITHOUT_DATE("adjournHearingWithoutDate"),
    RESTORE_STATE_FROM_ADJOURN("restoreStateFromAdjourn"),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing"),
    LIST_CMA("listCma"),
    EDIT_APPEAL_AFTER_SUBMIT("editAppealAfterSubmit"),
    LINK_APPEAL("linkAppeal"),
    UNLINK_APPEAL("unlinkAppeal"),
    PAYMENT_APPEAL("paymentAppeal"),
    PAY_AND_SUBMIT_APPEAL("payAndSubmitAppeal"),
    UPLOAD_SENSITIVE_DOCUMENTS("uploadSensitiveDocuments"),
    MARK_APPEAL_PAID("markAppealPaid"),
    REQUEST_HOME_OFFICE_DATA("requestHomeOfficeData"),
    MAKE_AN_APPLICATION("makeAnApplication"),
    REINSTATE_APPEAL("reinstateAppeal"),
    DECIDE_AN_APPLICATION("decideAnApplication"),
    REQUEST_NEW_HEARING_REQUIREMENTS("requestNewHearingRequirements"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
