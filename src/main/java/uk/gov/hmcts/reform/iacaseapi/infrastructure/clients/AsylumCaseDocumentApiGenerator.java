package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Service
public class AsylumCaseDocumentApiGenerator implements DocumentGenerator<AsylumCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String documentsApiEndpoint;
    private final String aboutToSubmitPath;
    private final String aboutToStartPath;

    public AsylumCaseDocumentApiGenerator(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${documentsApi.endpoint}") String documentsApiEndpoint,
        @Value("${documentsApi.asylum.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${documentsApi.asylum.aboutToStartPath}") String aboutToStartPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.documentsApiEndpoint = documentsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.aboutToStartPath = aboutToStartPath;
    }

    public AsylumCase generate(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToSubmitPath
        );
    }

    public AsylumCase aboutToStart(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToStartPath
        );
    }
}
