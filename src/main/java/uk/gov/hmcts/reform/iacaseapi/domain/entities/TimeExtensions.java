package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class TimeExtensions {

    private Optional<List<IdValue<TimeExtension>>> timeExtensions = Optional.empty();

    public TimeExtensions() {
        // noop
    }

    public Optional<List<IdValue<TimeExtension>>> getTimeExtensions() {
        return timeExtensions;
    }

    public void setTimeExtensions(List<IdValue<TimeExtension>> timeExtensions) {
        this.timeExtensions = Optional.ofNullable(timeExtensions);
    }
}
