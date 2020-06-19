package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public interface SendToWorkAllocation <T extends CaseData> {
    public void handle(Callback<T> callback);
}
