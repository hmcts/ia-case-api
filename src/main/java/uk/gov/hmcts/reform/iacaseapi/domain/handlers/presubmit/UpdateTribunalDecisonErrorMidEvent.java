package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

public class UpdateTribunalDecisonErrorMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
               && callback.getPageId().equals("tribunalDecisionAndReasonsQuestion");
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        YesOrNo isDecisionAndReasonBeingUpdated = asylumCase.read(AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS, YesOrNo.class)
            .orElse(NO);
        Optional<DynamicList> optionalTypesOfUpdateTribunalDecision = asylumCase.read(AsylumCaseFieldDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class);

        if (isDecisionAndReasonBeingUpdated == NO &&
            optionalTypesOfUpdateTribunalDecision.isPresent() &&
            "No".equals(optionalTypesOfUpdateTribunalDecision.get().getValue().getLabel())) {
            response.addError("You must update the decision or the Decision and Reasons document to continue");
        }
        return response;
            }
        }