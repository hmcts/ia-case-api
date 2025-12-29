package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Component
public class GenerateDocumentHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentGenerator<BailCase> documentGenerator;
    @Value("${featureFlag.isDocumentGenerationEnabled}")
    private boolean isDocumentGenerationEnabled;

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
        return isDocumentGenerationEnabled && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && getEventsToHandle().contains(callback.getEvent());
    }

    private List<Event> getEventsToHandle() {
        return Lists.newArrayList(
            Event.SUBMIT_APPLICATION,
            Event.RECORD_THE_DECISION,
            Event.END_APPLICATION,
            Event.MAKE_NEW_APPLICATION,
            Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
            Event.UPLOAD_SIGNED_DECISION_NOTICE,
            Event.CASE_LISTING
        );
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
