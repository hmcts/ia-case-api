package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;

@Data
@Builder
public class AdjournmentDetail {

    private String adjournmentDetailsHearing;
    private String hearingAdjournmentWhen;
    private String hearingAdjournmentDecisionParty;
    private String hearingAdjournmentDecisionPartyName;
    private String hearingAdjournmentRequestingParty;
    private String anyAdditionalAdjournmentInfo;
    private String additionalAdjournmentInfo;
    private String relistCaseImmediately;
    private String nextHearingFormat;
    private String nextHearingVenue;
    private HoursMinutes nextHearingDuration;
    private String nextHearingDate;
    private String nextHearingDateFixed;
    private String nextHearingDateRangeEarliest;
    private String nextHearingDateRangeLatest;
    private String shouldReserveOrExcludeJudge;
    private String reserveOrExcludeJudge;
}
