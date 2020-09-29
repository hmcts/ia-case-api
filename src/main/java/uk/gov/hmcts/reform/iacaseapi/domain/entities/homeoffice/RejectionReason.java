package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

public class RejectionReason {
    private String reason;

    private RejectionReason() {

    }

    public RejectionReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
