package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;

public interface PreSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(
        CallbackStage callbackStage,
        Callback<T> callback
    );

    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    PreSubmitCallbackResponse<T> handle(
        CallbackStage callbackStage,
        Callback<T> callback
    );
}
