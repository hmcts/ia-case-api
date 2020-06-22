package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReasonForLinkAppealOptions {
    FAMILIAL("familial"),
    SHARED_EVIDENCE("sharedEvidence"),
    GUARDIAN("guardian"),
    BAIL("bail"),
    HOME_OFFICE_REQUEST("homeOfficeRequest"),
    OTHER_APPEAL_PENDING("otherAppealPending"),
    OTHER_APPEAL_DECIDED("otherAppealDecided");

    @JsonValue
    private final String id;

    ReasonForLinkAppealOptions(String id) {
        this.id = id;
    }
}
