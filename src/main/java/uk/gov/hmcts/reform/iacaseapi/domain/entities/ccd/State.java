package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum State {

    APPEAL_STARTED("appealStarted"),
    APPEAL_SUBMITTED("appealSubmitted"),
    RESPONDENT_BUNDLE("respondentBundle"),
    PRE_APPEAL_BUILDING("preAppealBuilding"),
    APPEAL_BUILDING("appealBuilding"),
    CASE_BUILDING("caseBuilding"),
    CASE_BUILDING_CASE_OFFICER_REVIEW("caseBuildingCaseOfficerReview"),
    CASE_BUILDING_LEGAL_REP_REVIEW("caseBuildingLegalRepReview"),
    AWAITING_RESPONSE("awaitingResponse"),
    FINAL_CASE_BUILDING_CASE_OFFICER_REVIEW("finalCaseBuildingCaseOfficerReview"),
    HEARING_REQUIREMENTS("hearingRequirements"),
    HEARING_REQUIREMENTS_REVIEW("hearingRequirementsReview"),
    LISTING("listing"),
    PREPARE_FOR_HEARING("prepareForHearing"),
    HEARING_OUTCOME("hearingOutcome"),
    CASE_CLOSED("caseClosed"),
    ;

    private final String id;

    State(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
