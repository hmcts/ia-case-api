package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class EventTest {

    @Test
    public void has_correct_values() {
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("sendDirection", Event.SEND_DIRECTION.toString());
        assertEquals("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString());
        assertEquals("uploadRespondentEvidence", Event.UPLOAD_RESPONDENT_EVIDENCE.toString());
        assertEquals("uploadHomeOfficeBundle", Event.UPLOAD_HOME_OFFICE_BUNDLE.toString());
        assertEquals("buildCase", Event.BUILD_CASE.toString());
        assertEquals("submitCase", Event.SUBMIT_CASE.toString());
        assertEquals("requestCaseEdit", Event.REQUEST_CASE_EDIT.toString());
        assertEquals("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("addAppealResponse", Event.ADD_APPEAL_RESPONSE.toString());
        assertEquals("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("requestHearingRequirements", Event.REQUEST_HEARING_REQUIREMENTS.toString());
        assertEquals("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString());
        assertEquals("reviewHearingRequirements", Event.REVIEW_HEARING_REQUIREMENTS.toString());
        assertEquals("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS.toString());
        assertEquals("updateHearingRequirements", Event.UPDATE_HEARING_REQUIREMENTS.toString());
        assertEquals("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS.toString());
        assertEquals("listCaseWithoutHearingRequirements", Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.toString());
        assertEquals("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString());
        assertEquals("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE.toString());
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("createCaseSummary", Event.CREATE_CASE_SUMMARY.toString());
        assertEquals("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE.toString());
        assertEquals("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE.toString());
        assertEquals("customiseHearingBundle", Event.CUSTOMISE_HEARING_BUNDLE.toString());
        assertEquals("decisionAndReasonsStarted", Event.DECISION_AND_REASONS_STARTED.toString());
        assertEquals("generateDecisionAndReasons", Event.GENERATE_DECISION_AND_REASONS.toString());
        assertEquals("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString());
        assertEquals("addCaseNote", Event.ADD_CASE_NOTE.toString());
        assertEquals("uploadAddendumEvidence", Event.UPLOAD_ADDENDUM_EVIDENCE.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("recordApplication", Event.RECORD_APPLICATION.toString());
        assertEquals("recordAttendeesAndDuration", Event.RECORD_ATTENDEES_AND_DURATION.toString());
        assertEquals("uploadHearingRecording", Event.UPLOAD_HEARING_RECORDING.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("requestCaseBuilding", Event.REQUEST_CASE_BUILDING.toString());
        assertEquals("uploadAdditionalEvidenceHomeOffice", Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("requestResponseReview", Event.REQUEST_RESPONSE_REVIEW.toString());
        assertEquals("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND.toString());
        assertEquals("uploadAddendumEvidenceLegalRep", Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP.toString());
        assertEquals("uploadAddendumEvidenceHomeOffice", Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("uploadAddendumEvidenceAdminOfficer", Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER.toString());
        assertEquals("removeAppealFromOnline", Event.REMOVE_APPEAL_FROM_ONLINE.toString());
        assertEquals("shareACase", Event.SHARE_A_CASE.toString());
        assertEquals("editReasonsForAppeal", Event.EDIT_REASONS_FOR_APPEAL.toString());
        assertEquals("requestReasonsForAppeal", Event.REQUEST_REASONS_FOR_APPEAL.toString());
        assertEquals("submitReasonsForAppeal", Event.SUBMIT_REASONS_FOR_APPEAL.toString());
        assertEquals("sendToPreHearing", Event.SEND_TO_PREHEARING.toString());
        assertEquals("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString());
        assertEquals("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("leadershipJudgeFtpaDecision", Event.LEADERSHIP_JUDGE_FTPA_DECISION.toString());
        assertEquals("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("editDocuments", Event.EDIT_DOCUMENTS.toString());
        assertEquals("recordAllocatedJudge", Event.RECORD_ALLOCATED_JUDGE.toString());
        assertEquals("editTimeExtension", Event.EDIT_TIME_EXTENSION.toString());
        assertEquals("submitTimeExtension", Event.SUBMIT_TIME_EXTENSION.toString());
        assertEquals("reviewTimeExtension", Event.REVIEW_TIME_EXTENSION.toString());
        assertEquals("sendDirectionWithQuestions", Event.SEND_DIRECTION_WITH_QUESTIONS.toString());
        assertEquals("flagCase", Event.FLAG_CASE.toString());
        assertEquals("removeFlag", Event.REMOVE_FLAG.toString());
        assertEquals("requestCmaRequirements", Event.REQUEST_CMA_REQUIREMENTS.toString());
        assertEquals("reviewCmaRequirements", Event.REVIEW_CMA_REQUIREMENTS.toString());
        assertEquals("forceCaseToCaseUnderReview", Event.FORCE_CASE_TO_CASE_UNDER_REVIEW.toString());
        assertEquals("forceCaseToSubmitHearingRequirements", Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("changeHearingCentre", Event.CHANGE_HEARING_CENTRE.toString());
        assertEquals("updateLegalRepDetails", Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS.toString());
        assertEquals("linkAppeal", Event.LINK_APPEAL.toString());
        assertEquals("unlinkAppeal", Event.UNLINK_APPEAL.toString());
        assertEquals("paymentAppeal", Event.PAYMENT_APPEAL.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("restoreStateFromAdjourn", Event.RESTORE_STATE_FROM_ADJOURN.toString());
        assertEquals("decisionWithoutHearing", Event.DECISION_WITHOUT_HEARING.toString());
        assertEquals("listCma", Event.LIST_CMA.toString());
        assertEquals("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT.toString());
        assertEquals("forceRequestCaseBuilding", Event.FORCE_REQUEST_CASE_BUILDING.toString());
        assertEquals("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString());
        assertEquals("markAppealPaid", Event.MARK_APPEAL_PAID.toString());
        assertEquals("requestHomeOfficeData", Event.REQUEST_HOME_OFFICE_DATA.toString());
        assertEquals("makeAnApplication", Event.MAKE_AN_APPLICATION.toString());
        assertEquals("reinstateAppeal", Event.REINSTATE_APPEAL.toString());
        assertEquals("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString());
        assertEquals("requestNewHearingRequirements", Event.REQUEST_NEW_HEARING_REQUIREMENTS.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(93, Event.values().length);
    }
}
