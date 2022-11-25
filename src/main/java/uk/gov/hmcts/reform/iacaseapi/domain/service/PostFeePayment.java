package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public interface PostFeePayment<T extends CaseData> {

    PostSubmitCallbackResponse ccdSubmitted(
        Callback<T> callback
    );

}
