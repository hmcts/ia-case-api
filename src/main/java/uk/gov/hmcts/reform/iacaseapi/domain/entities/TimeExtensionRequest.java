package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class TimeExtensionRequest {

    private Optional<String> timeRequested = Optional.empty();
    private Optional<String> reasons = Optional.empty();

    private TimeExtensionRequest() {
        // noop -- for deserializer
    }

    public Optional<String> getTimeRequested() {
        return timeRequested;
    }

    public Optional<String> getReasons() {
        return reasons;
    }

    public void setTimeRequested(String timeRequested) {
        this.timeRequested = Optional.ofNullable(timeRequested);
    }

    public void setReasons(String reasons) {
        this.reasons = Optional.ofNullable(reasons);
    }
}
