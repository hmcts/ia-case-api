package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public interface PostSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(Callback<T> callback);

    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    PostSubmitCallbackResponse handle(Callback<T> callback);
}
