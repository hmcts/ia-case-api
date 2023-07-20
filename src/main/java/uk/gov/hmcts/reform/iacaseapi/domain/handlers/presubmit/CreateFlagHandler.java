package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
class CreateFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.CREATE_FLAG;
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

        Optional<StrategicCaseFlag> existingCaseLevelFlags = asylumCase.read(CASE_LEVEL_FLAGS);

        Optional<StrategicCaseFlag> existingAppellantLevelFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS);

        if (existingAppellantLevelFlags.isEmpty()
            || existingAppellantLevelFlags.get().getPartyName() == null
            || existingAppellantLevelFlags.get().getPartyName().isBlank()) {

            final String appellantNameForDisplay =
                asylumCase
                    .read(APPELLANT_NAME_FOR_DISPLAY, String.class)
                    .orElseThrow(() -> new IllegalStateException("appellantNameForDisplay is not present"));

            asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(appellantNameForDisplay));
        }

        if (existingCaseLevelFlags.isEmpty()) {
            asylumCase.write(CASE_LEVEL_FLAGS, new StrategicCaseFlag());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
