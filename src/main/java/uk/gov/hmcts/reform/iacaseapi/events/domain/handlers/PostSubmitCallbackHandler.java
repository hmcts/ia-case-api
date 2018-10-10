package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;

public interface PostSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(
        CallbackStage callbackStage,
        Callback<T> callback
    );

    PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<T> callback
    );
}
