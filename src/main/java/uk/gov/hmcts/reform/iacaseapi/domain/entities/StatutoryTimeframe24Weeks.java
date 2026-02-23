package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class StatutoryTimeframe24Weeks {

    private List<IdValue<StatutoryTimeframe24WeeksHistory>> history;

    private StatutoryTimeframe24Weeks() {
    }

    public StatutoryTimeframe24Weeks(
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history
    ) {
        this.history = requireNonNull(history);
    }

    public List<IdValue<StatutoryTimeframe24WeeksHistory>> getHistory() {
        return requireNonNull(history);
    }

}
