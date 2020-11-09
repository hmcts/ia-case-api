package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Component
public class AppealSubmissonPdfHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isDocmosisEnabled;
    private final DocumentGenerator<AsylumCase> documentGenerator;

    public AppealSubmissonPdfHandler(
        @Value("${featureFlag.docmosisEnabled}") boolean isDocmosisEnabled,
        DocumentGenerator<AsylumCase> documentGenerator
    ) {
        this.isDocmosisEnabled = isDocmosisEnabled;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return isDocmosisEnabled
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Event.PAY_AND_SUBMIT_APPEAL == callback.getEvent();
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithGeneratedDocument = documentGenerator.generate(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithGeneratedDocument);
    }
}
