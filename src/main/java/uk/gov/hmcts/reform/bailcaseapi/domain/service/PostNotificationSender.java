package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public interface PostNotificationSender<T extends CaseData> {

    PostSubmitCallbackResponse send(
        Callback<T> callback
    );
}
