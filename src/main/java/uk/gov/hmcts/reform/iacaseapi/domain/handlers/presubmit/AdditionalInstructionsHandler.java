package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADDITIONAL_INSTRUCTION_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AdditionalInstructionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = List.of(
            REVIEW_HEARING_REQUIREMENTS,
            UPDATE_HEARING_REQUIREMENTS,
            UPDATE_HEARING_ADJUSTMENTS,
            LIST_CASE_WITHOUT_HEARING_REQUIREMENTS
        );
        Event event = callback.getEvent();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && targetEvents.contains(event);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (callback.getEvent() == LIST_CASE_WITHOUT_HEARING_REQUIREMENTS) {
            tryClearAdditionalInstructions(
                asylumCase, ADDITIONAL_INSTRUCTIONS, ADDITIONAL_INSTRUCTIONS_DESCRIPTION);
        } else {
            tryClearAdditionalInstructions(
                asylumCase, IS_ADDITIONAL_INSTRUCTION_ALLOWED, ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void tryClearAdditionalInstructions(
        AsylumCase asylumCase, AsylumCaseFieldDefinition flag, AsylumCaseFieldDefinition fieldToClear) {

        boolean additionalInstructionAllowed = asylumCase
            .read(flag, YesOrNo.class)
            .map(yesOrNo -> yesOrNo == YES).orElse(false);

        if (!additionalInstructionAllowed) {
            asylumCase.clear(fieldToClear);
        }
    }
}
