package uk.gov.hmcts.reform.bailcaseapi.domain.handlers;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public interface PostSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(Callback<T> callback);

    PostSubmitCallbackResponse handle(Callback<T> callback);
}
