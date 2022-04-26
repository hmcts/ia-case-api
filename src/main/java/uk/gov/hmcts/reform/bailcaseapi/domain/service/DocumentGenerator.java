package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

public interface DocumentGenerator<T extends CaseData> {

    T generate(
        Callback<T> callback
    );

    T aboutToStart(
        Callback<T> callback
    );
}
