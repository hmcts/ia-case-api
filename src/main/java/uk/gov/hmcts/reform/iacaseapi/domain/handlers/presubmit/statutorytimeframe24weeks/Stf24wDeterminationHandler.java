package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.STF_24W_DETERMINATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@Component
public class Stf24wDeterminationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Autowired
    private UpdateStatutoryTimeframe24WeeksService updateStatutoryTimeframe24WeeksService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage.equals(ABOUT_TO_SUBMIT)
            && callback.getEvent().equals(STF_24W_DETERMINATION)
            && callback.getCaseDetails().getCaseData()
            .read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class).isPresent();
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        YesOrNo status = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class).orElse(YesOrNo.NO);
        AsylumCase updatedAsylum = updateStatutoryTimeframe24WeeksService.updateAsylumCaseFromDetermination(asylumCase, status);
        return new PreSubmitCallbackResponse<>(updatedAsylum);
    }
}
