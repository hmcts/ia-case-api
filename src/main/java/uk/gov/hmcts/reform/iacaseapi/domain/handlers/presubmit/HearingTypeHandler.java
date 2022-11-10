package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@Component
public class HearingTypeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.START_APPEAL;
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

        final String isAcceleratedDetainedAppeal =
                asylumCase
                        .read(IS_ACCELERATED_DETAINED_IN_APPEAL, String.class)
                        .orElse("");

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElse(null);

        //AppealType.class

        //Yes to show the screen

        if(appealType!=null) {
            if ((appealType.equals(Optional.of("Deprivation of citizenship"))
                || appealType.getValue().equals(Optional.of("Revocation of a protection status")))
                || isAcceleratedDetainedAppeal.equals("Yes")) {

                asylumCase.write(HEARING_TYPE_RESULT, YesOrNo.YES);

            } else {
                asylumCase.write(HEARING_TYPE_RESULT, YesOrNo.NO);
            }
        }




        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
