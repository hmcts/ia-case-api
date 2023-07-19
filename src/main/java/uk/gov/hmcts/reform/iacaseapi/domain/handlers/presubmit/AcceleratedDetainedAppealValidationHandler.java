package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class AcceleratedDetainedAppealValidationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.MID_EVENT)
                && Arrays.asList(
                Event.START_APPEAL,
                Event.EDIT_APPEAL
        ).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        YesOrNo adaEnabled = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElse("")
                .equals(DetentionFacility.IRC.toString()) ? YesOrNo.YES : YesOrNo.NO;

        YesOrNo isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO);

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        YesOrNo appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Unable to determine if in country or out of country appeal"));
        boolean isUkAppeal = appellantInUk.equals(YES);

        if (isAda.equals(YES) && adaEnabled.equals(NO) && isUkAppeal) {
            response.addError("You can only select yes if the appellant is detained in an immigration removal centre");
        }

        return response;
    }
}
