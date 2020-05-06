package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public interface FeePayment<T extends CaseData> {

    T send(
            Callback<T> callback
    );
}
