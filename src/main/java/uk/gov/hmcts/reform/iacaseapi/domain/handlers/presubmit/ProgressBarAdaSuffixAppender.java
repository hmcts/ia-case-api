package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_SUFFIX;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REQ_SUFFIX;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

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

    private static final String ADA_SUFFIX_STRING = "_ada";
    private static final String AFTER_HEARING_REQ = "_afterHearingReq";

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

        asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .ifPresent(isAda -> {
                if (isAda.equals(YES)) {
                    asylumCase.write(ADA_SUFFIX, ADA_SUFFIX_STRING);
                } else {
                    asylumCase.write(ADA_SUFFIX, "");
                }
            });

        // Set suffix to append to URLs of images when appeal transfers out of ADA after submitHearingRequirements
        if (callback.getEvent().equals(TRANSFER_OUT_OF_ADA)) {
            asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class)
                .ifPresent(yesOrNo -> {
                    if (yesOrNo.equals(YES)) {
                        asylumCase.write(HEARING_REQ_SUFFIX, AFTER_HEARING_REQ);
                    }
                });
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
