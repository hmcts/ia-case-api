package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@Component
public class RecordAdjustmentDetailsPreparer  implements PreSubmitCallbackHandler<AsylumCase> {

    private final String hearingsApiEndpoint;
    private final String aboutToStartPath;
    private final String midEventPath;

    AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;

    public RecordAdjustmentDetailsPreparer(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${hearingsApi.endpoint}") String hearingsApiEndpoint,
        @Value("${hearingsApi.aboutToStartPath}") String aboutToStartPath,
        @Value("${hearingsApi.midEventPath}") String midEventPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.hearingsApiEndpoint = hearingsApiEndpoint;
        this.aboutToStartPath = aboutToStartPath;
        this.midEventPath = midEventPath;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_START
               && callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = getHearings(callback);

        asylumCase.read(AsylumCaseFieldDefinition.CHANGE_HEARINGS);



        boolean isIntegrated = asylumCase.read(AsylumCaseFieldDefinition.IS_INTEGRATED)
            .map(yesOrNo -> yesOrNo.equals(YES))
            .orElse(false);

        if (!isIntegrated) {
            asylumCase.write(AsylumCaseFieldDefinition.IS_INTEGRATED, YES);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCase getHearings(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath);
    }
}