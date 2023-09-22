package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class WitnessCaseFlagsHandler{

    protected Map<String, StrategicCaseFlag> getWitnessFlags(AsylumCase asylumCase) {
        Optional<List<PartyFlagIdValue>> optionalWitnessFlags = asylumCase.read(WITNESS_LEVEL_FLAGS);

        return optionalWitnessFlags
            .map(witnessFlags -> witnessFlags
                .stream().collect(Collectors.toMap(PartyFlagIdValue::getPartyId, PartyFlagIdValue::getValue)))
            .orElse(new HashMap<>());
    }

    protected Map<String, WitnessDetails> getWitnessDetailsMap(AsylumCase asylumCase) {
        Optional<List<IdValue<WitnessDetails>>> optionalWitnessDetails = asylumCase.read(WITNESS_DETAILS);

        return optionalWitnessDetails
            .map(idValueList -> idValueList.stream()
                .map(IdValue::getValue)
                .collect(Collectors.toMap(WitnessDetails::getWitnessPartyId, witnessDetails -> witnessDetails)))
            .orElse(new HashMap<>());
    }

    protected Map<String, Language> getWitnessInterpreterLanguageMap(
        AsylumCase asylumCase, Map<String, WitnessDetails> witnessDetailsMap) {

        return getWitnessLanguageMap(asylumCase, witnessDetailsMap, WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE);
    }

    protected Map<String, Language> getWitnessSignLanguageMap(
        AsylumCase asylumCase, Map<String, WitnessDetails> witnessDetailsMap) {

        return getWitnessLanguageMap(asylumCase, witnessDetailsMap, WITNESS_N_INTERPRETER_SIGN_LANGUAGE);
    }

    private Map<String, Language> getWitnessLanguageMap(
        AsylumCase asylumCase,
        Map<String, WitnessDetails> witnessDetailsMap,
        List<AsylumCaseFieldDefinition> languageFieldDefs) {

        Map<String, Language> witnessLanguageMap = new HashMap<>();
        int size = Math.min(WITNESS_N_FIELD.size(), languageFieldDefs.size());
        for (int i = 0; i < size; i++) {
            WitnessDetails witnessDetails = asylumCase.read(WITNESS_N_FIELD.get(i), WitnessDetails.class).orElse(null);
            Language witnessInterpreterLanguage = asylumCase
                .read(languageFieldDefs.get(i), InterpreterLanguageRefData.class)
                .map(Language::of).orElse(null);

            if (witnessDetails != null
                && witnessInterpreterLanguage != null
                && witnessDetailsMap.containsKey(witnessDetails.getWitnessPartyId())) {
                witnessLanguageMap.put(witnessDetails.getWitnessPartyId(), witnessInterpreterLanguage);
            }
        }

        return witnessLanguageMap;
    }

}
