package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public interface EventValidChecker<T extends CaseData> {
    EventValid check(Callback<T> callback);
}
