package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE_SELECTED_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DESIRED_STATE_CORRECT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PROGRESS_MIGRATED_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class ProgressMigratedCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_START && callback.getEvent() == PROGRESS_MIGRATED_CASE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isTheDesiredStateCorrect = asylumCase.read(DESIRED_STATE_CORRECT, YesOrNo.class)
            .orElseThrow(() -> new IllegalStateException("desiredStateCorrect is not present"));

        if (isTheDesiredStateCorrect.equals(YesOrNo.NO)) {
            State newDesiredState = asylumCase.read(ARIA_DESIRED_STATE, State.class)
                .orElseThrow(() -> new IllegalStateException("ariaDesiredState is not present"));

            asylumCase.write(ARIA_DESIRED_STATE_SELECTED_VALUE, newDesiredState.getDescription());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
