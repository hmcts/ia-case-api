package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GENERATE_LIST_CMR_TASK_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_LIST_CMR_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Slf4j
@Component
public class GenerateListCmrTaskHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public GenerateListCmrTaskHandler() {
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && GENERATE_LIST_CMR_TASK == event;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        asylumCase.write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
