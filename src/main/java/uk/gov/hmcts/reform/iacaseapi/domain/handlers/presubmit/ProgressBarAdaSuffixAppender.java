package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.TRANSFER_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.getAdaSuffix;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.getAfterHearingReqSuffix;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAcceleratedDetainedAppeal;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ProgressBarAdaSuffixAppender implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Set.of(SUBMIT_APPEAL, TRANSFER_OUT_OF_ADA).contains(callback.getEvent());
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isNabaEnabled = asylumCase.read(IS_NABA_ENABLED, YesOrNo.class).orElse(NO);
        if (isNabaEnabled.equals(NO)) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        if (isAcceleratedDetainedAppeal(asylumCase)) {
            asylumCase.write(ADA_SUFFIX, getAdaSuffix());
        } else {
            asylumCase.write(ADA_SUFFIX, "");
        }

        // Set suffix to append to URLs of images when appeal transfers out of ADA after submitHearingRequirements
        if (callback.getEvent().equals(TRANSFER_OUT_OF_ADA)) {
            asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class)
                .ifPresent(yesOrNo -> {
                    if (yesOrNo.equals(YES)) {
                        asylumCase.write(HEARING_REQ_SUFFIX, getAfterHearingReqSuffix());
                    }
                });
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
