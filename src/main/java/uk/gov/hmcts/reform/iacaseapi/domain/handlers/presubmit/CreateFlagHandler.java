package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Map;


@Component
class CreateFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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

        final String appellantNameForDisplay =
                asylumCase
                        .read(APPELLANT_NAME_FOR_DISPLAY, String.class)
                        .orElseThrow(() -> new IllegalStateException("appellantNameForDisplay is not present"));

        asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(appellantNameForDisplay));
        asylumCase.write(CASE_LEVEL_FLAGS, new StrategicCaseFlag());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
