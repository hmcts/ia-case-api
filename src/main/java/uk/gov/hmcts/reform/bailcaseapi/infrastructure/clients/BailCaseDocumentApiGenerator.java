package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentGenerator;

@Service
public class BailCaseDocumentApiGenerator implements DocumentGenerator<BailCase> {

    private final BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    private final String documentsApiEndpoint;
    private final String aboutToSubmitPath;
    private final String aboutToStartPath;

    public BailCaseDocumentApiGenerator(
        BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator,
        @Value("${documentsApi.endpoint}") String documentsApiEndpoint,
        @Value("${documentsApi.aboutToSubmitPath}")String aboutToSubmitPath,
        @Value("${documentsApi.aboutToStartPath}")String aboutToStartPath
    ) {
        this.bailCaseCallbackApiDelegator = bailCaseCallbackApiDelegator;
        this.documentsApiEndpoint = documentsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.aboutToStartPath = aboutToStartPath;
    }

    public BailCase generate(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return bailCaseCallbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToSubmitPath
        );
    }

    public BailCase aboutToStart(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return bailCaseCallbackApiDelegator.delegate(
            callback,
            documentsApiEndpoint + aboutToStartPath
        );
    }
}
