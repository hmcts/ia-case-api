package uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

public interface EventValidChecker<T extends CaseData> {
    EventValid check(Callback<T> callback);
}
