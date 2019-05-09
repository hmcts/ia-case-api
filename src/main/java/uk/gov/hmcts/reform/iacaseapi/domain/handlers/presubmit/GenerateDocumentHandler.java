package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Component
public class GenerateDocumentHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final boolean isDocmosisEnabled;
    private final DocumentGenerator<CaseDataMap> documentGenerator;

    public GenerateDocumentHandler(
        @Value("${featureFlag.docmosisEnabled}") boolean isDocmosisEnabled,
        DocumentGenerator<CaseDataMap> documentGenerator
    ) {
        this.isDocmosisEnabled = isDocmosisEnabled;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return isDocmosisEnabled
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               &&
               Arrays.asList(
                   Event.SUBMIT_APPEAL,
                   Event.LIST_CASE
               ).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap CaseDataMapWithGeneratedDocument = documentGenerator.generate(callback);

        return new PreSubmitCallbackResponse<>(CaseDataMapWithGeneratedDocument);
    }
}
