package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

import static java.util.Objects.requireNonNull;


@Component
public class GenerateAmendedHearingBundlePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentGenerator<AsylumCase> documentGenerator;

    public GenerateAmendedHearingBundlePreparer(
        DocumentGenerator<AsylumCase> documentGenerator
    ) {
        this.documentGenerator = documentGenerator;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
               && callback.getEvent() == Event.GENERATE_AMENDED_HEARING_BUNDLE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        return new PreSubmitCallbackResponse<>(documentGenerator.aboutToStart(callback));
    }

}
