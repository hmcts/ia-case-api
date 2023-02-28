package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class TransferOutOfAdaHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public TransferOutOfAdaHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == TRANSFER_OUT_OF_ADA;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        Optional<YesOrNo> isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);

        if (isAcceleratedDetainedAppeal.equals(Optional.of(YesOrNo.YES))) {

            asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, NO);
            asylumCase.write(DETENTION_STATUS, DetentionStatus.DETAINED);
            asylumCase.write(TRANSFER_OUT_OF_ADA_DATE, dateProvider.now().toString());
            asylumCase.write(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.YES);

            asylumCase.write(LISTING_AVAILABLE_FOR_ADA, NO);

            asylumCase.write(ADA_HEARING_ADJUSTMENTS_UPDATABLE, NO);
            asylumCase.write(ADA_HEARING_REQUIREMENTS_UPDATABLE, NO);
            asylumCase.write(ADA_HEARING_REQUIREMENTS_TO_REVIEW, NO);
            asylumCase.write(ADA_HEARING_REQUIREMENTS_SUBMITTABLE, NO);
            asylumCase.write(ADA_EDIT_LISTING_AVAILABLE, NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);

    }

}
