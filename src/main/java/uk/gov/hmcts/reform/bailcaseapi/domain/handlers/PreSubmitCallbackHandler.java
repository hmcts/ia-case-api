package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

public interface PreSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    );

    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    PreSubmitCallbackResponse<T> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    );
}
