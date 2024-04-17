package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@Service
public class IaHearingsApiService {

    private final String hearingsApiEndpoint;
    private final String aboutToStartPath;
    private final String midEventPath;
    private final String aboutToSubmitPath;

    AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;

    public IaHearingsApiService(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${hearingsApi.endpoint}") String hearingsApiEndpoint,
        @Value("${hearingsApi.aboutToStartPath}") String aboutToStartPath,
        @Value("${hearingsApi.midEventPath}") String midEventPath,
        @Value("${hearingsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.hearingsApiEndpoint = hearingsApiEndpoint;
        this.aboutToStartPath = aboutToStartPath;
        this.midEventPath = midEventPath;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public AsylumCase aboutToStart(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath);
    }

    public AsylumCase midEvent(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + midEventPath);
    }

    public AsylumCase aboutToSubmit(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToSubmitPath);
    }
}
