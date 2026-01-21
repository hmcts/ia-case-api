package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Service
public class AsylumCaseDocumentApiGenerator implements DocumentGenerator<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String documentsApiEndpoint;
    private final String aboutToSubmitPath;
    private final String aboutToStartPath;

    public AsylumCaseDocumentApiGenerator(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${documentsApi.endpoint}") String documentsApiEndpoint,
        @Value("${documentsApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${documentsApi.aboutToStartPath}") String aboutToStartPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.documentsApiEndpoint = documentsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.aboutToStartPath = aboutToStartPath;
    }

    public AsylumCase generate(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToSubmitPath
        );
    }

    public AsylumCase aboutToStart(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToStartPath
        );
    }

    public AsylumCase generateOnMidEvent(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + "/asylum/ccdMidEvent"
        );
    }
}
