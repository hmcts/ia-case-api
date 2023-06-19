package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@Service
@Slf4j
public class HomeOfficeApiService implements HomeOfficeApi<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String homeOfficeApiEndpoint;
    private final String aboutToStartPath;
    private final String aboutToSubmitPath;

    public HomeOfficeApiService(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${homeOfficeApi.endpoint}") String homeOfficeApiEndpoint,
        @Value("${homeOfficeApi.aboutToStartPath}") String aboutToStartPath,
        @Value("${homeOfficeApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.homeOfficeApiEndpoint = homeOfficeApiEndpoint;
        this.aboutToStartPath = aboutToStartPath;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    @Override
    public AsylumCase call(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        log.info("call: Calling delegator to endpoint: {}{}", homeOfficeApiEndpoint, aboutToSubmitPath);
        return asylumCaseCallbackApiDelegator.delegate(
                callback,
                homeOfficeApiEndpoint + aboutToSubmitPath
        );
    }

    @Override
    public AsylumCase aboutToStart(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        log.info("aboutToStart: Calling delegator to endpoint: {}{}", homeOfficeApiEndpoint, aboutToSubmitPath);
        return asylumCaseCallbackApiDelegator.delegate(
                callback,
                homeOfficeApiEndpoint + aboutToStartPath
        );
    }

    @Override
    public AsylumCase aboutToSubmit(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        log.info("aboutToSubmit: Calling delegator to endpoint: {}{}", homeOfficeApiEndpoint, aboutToSubmitPath);
        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            homeOfficeApiEndpoint + aboutToSubmitPath
        );
    }
}
