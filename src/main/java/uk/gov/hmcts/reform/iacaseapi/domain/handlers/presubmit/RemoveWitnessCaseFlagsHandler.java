package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RemoveWitnessCaseFlagsHandler extends WitnessCaseFlagsHandler
    implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && UPDATE_HEARING_REQUIREMENTS.equals(callback.getEvent());
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Map<String, WitnessDetails> witnessDetailsMap = getWitnessDetailsMap(asylumCase);
        Map<String, StrategicCaseFlag> witnessFlagsMap = getWitnessFlagsMap(asylumCase);

        boolean caseDataUpdated = false;
        List<String> idsOfDeletedWitnesses = witnessFlagsMap.keySet().stream()
            .filter(key -> !witnessDetailsMap.containsKey(key)).toList();
        for (String witnessPartyId : idsOfDeletedWitnesses) {
            witnessFlagsMap.remove(witnessPartyId);
            caseDataUpdated = true;
        }

        if (caseDataUpdated) {
            List<PartyFlagIdValue> witnessFlagsIdValues = witnessFlagsMap
                .entrySet().stream()
                .map(entry -> new PartyFlagIdValue(entry.getKey(), entry.getValue())).toList();
            asylumCase
                .write(WITNESS_LEVEL_FLAGS, witnessFlagsIdValues);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
