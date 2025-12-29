package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public interface DocumentGenerator<T extends CaseData> {

    T generate(
        Callback<T> callback
    );

    T aboutToStart(
        Callback<T> callback
    );
}
