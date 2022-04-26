package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentGenerator;

@Component
public class GenerateDocumentHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentGenerator<BailCase> documentGenerator;
    @Value("${featureFlag.isDocumentGenerationEnambled}")
    private boolean isDocumentGenerationEnambled;

    public GenerateDocumentHandler(
        DocumentGenerator<BailCase> documentGenerator
    ) {
        this.documentGenerator = documentGenerator;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callbackStage, "callbackStage must not be null");
        return isDocumentGenerationEnambled ? callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_APPLICATION : false;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callbackStage, "callbackStage must not be null");

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCaseWithGeneratedDocument = documentGenerator.generate(callback);

        return new PreSubmitCallbackResponse<>(bailCaseWithGeneratedDocument);
    }
}
