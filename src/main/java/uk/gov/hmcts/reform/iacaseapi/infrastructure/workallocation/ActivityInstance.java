package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

public class ActivityInstance {
    private Long durationInMillis;

    public Long getDurationInMillis() {
        return durationInMillis;
    }

    @Override
    public String toString() {
        return "ActivityInstance{" +
                "durationInMillis=" + durationInMillis +
                '}';
    }
}
