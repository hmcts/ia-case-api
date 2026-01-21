package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Component
public class EventValidCheckers<T extends CaseData> {
    private final List<EventValidChecker<T>> checkers;

    public EventValidCheckers(List<EventValidChecker<T>> checkers) {
        this.checkers = checkers;
    }

    public EventValid check(Callback<T> callback) {
        return checkers.stream()
            .map(checker -> checker.check(callback))
            .filter(result -> !result.isValid())
            .findFirst()
            .orElse(EventValid.VALID_EVENT);
    }
}
