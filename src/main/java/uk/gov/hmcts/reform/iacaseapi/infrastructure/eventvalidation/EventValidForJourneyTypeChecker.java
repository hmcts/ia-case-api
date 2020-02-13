package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public interface EventValidForJourneyTypeChecker<T extends CaseData> {
    EventValidForJourneyType check(Callback<T> callback);
}
