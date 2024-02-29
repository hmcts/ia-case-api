package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateTribunalAppealDecisionRule31 implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isDecisionRule31(asylumCase)) {
            DynamicList updateTribunalDecisionValue = asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)
                    .orElseThrow(() -> new IllegalStateException("typesOfUpdateTribunalDecision is not present"));

            asylumCase.write(UPDATED_APPEAL_DECISION, StringUtils.capitalize(updateTribunalDecisionValue.getValue().getCode()));
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDecisionRule31(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)
                .map(type -> type.equals(UNDER_RULE_31)).orElse(false);
    }
}