package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Service
public class BailCaseDocumentApiGenerator implements DocumentGenerator<BailCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String documentsApiEndpoint;
    private final String aboutToSubmitPath;
    private final String aboutToStartPath;

    public BailCaseDocumentApiGenerator(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${documentsApi.endpoint}") String documentsApiEndpoint,
        @Value("${documentsApi.aboutToSubmitPath}")String aboutToSubmitPath,
        @Value("${documentsApi.aboutToStartPath}")String aboutToStartPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.documentsApiEndpoint = documentsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.aboutToStartPath = aboutToStartPath;
    }

    public BailCase generate(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToSubmitPath
        );
    }

    public BailCase aboutToStart(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToStartPath
        );
    }
}
