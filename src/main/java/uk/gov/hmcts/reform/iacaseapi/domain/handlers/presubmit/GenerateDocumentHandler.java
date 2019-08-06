package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import java.util.List;
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
public class GenerateDocumentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isDocmosisEnabled;
    private final boolean isEmStitchingEnabled;
    private final DocumentGenerator<AsylumCase> documentGenerator;

    public GenerateDocumentHandler(
        @Value("${featureFlag.docmosisEnabled}") boolean isDocmosisEnabled,
        @Value("${featureFlag.isEmStitchingEnabled}") boolean isEmStitchingEnabled,
        DocumentGenerator<AsylumCase> documentGenerator
    ) {
        this.isDocmosisEnabled = isDocmosisEnabled;
        this.isEmStitchingEnabled = isEmStitchingEnabled;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> allowedEvents = Lists.newArrayList(
                Event.SUBMIT_APPEAL,
                Event.LIST_CASE,
                Event.GENERATE_DECISION_AND_REASONS,
                Event.SEND_DECISION_AND_REASONS,
                Event.EDIT_CASE_LISTING);
        if (isEmStitchingEnabled) {
            allowedEvents.add(Event.GENERATE_HEARING_BUNDLE);
            allowedEvents.add(Event.SUBMIT_CASE);
        }

        return isDocmosisEnabled
                  && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                  && allowedEvents.contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithGeneratedDocument = documentGenerator.generate(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithGeneratedDocument);
    }
}
