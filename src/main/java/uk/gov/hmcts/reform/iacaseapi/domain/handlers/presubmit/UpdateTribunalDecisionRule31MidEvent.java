package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.DISMISSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateTribunalDecisionRule31MidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
               && callback.getPageId().equals("tribunalDecisionType");
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean isDecisionAllowed = isDecisionAllowed(asylumCase);

        final List<Value> values = new ArrayList<>();
        if (isDecisionAllowed) {
            values.add(new Value(DISMISSED.getValue(), "Yes, change decision to Dismissed"));
            values.add(new Value(ALLOWED.getValue(), "No"));
        } else {
            values.add(new Value(ALLOWED.getValue(), "Yes, change decision to Allowed"));
            values.add(new Value(DISMISSED.getValue(), "No"));
        }

        DynamicList typesOfUpdateTribunalDecision = new DynamicList(new Value("", ""), values);

        asylumCase.write(TYPES_OF_UPDATE_TRIBUNAL_DECISION, typesOfUpdateTribunalDecision);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDecisionAllowed(AsylumCase asylumCase) {
        return asylumCase
            .read(IS_DECISION_ALLOWED, AppealDecision.class)
            .map(type -> type.equals(ALLOWED)).orElse(false);
    }
}
