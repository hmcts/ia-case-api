package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Component
public class SpokenLanguageForWitnessCaseFlagsHandler extends WitnessCaseFlagsHandler
        implements PreSubmitCallbackHandler<AsylumCase> {

    public SpokenLanguageForWitnessCaseFlagsHandler(DateProvider systemDateProvider) {
        super.systemDateProvider = systemDateProvider;
    }

    private boolean caseDataUpdated = false;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = List.of(REVIEW_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS);
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && targetEvents.contains(callback.getEvent());
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        String currentDateTime = systemDateProvider.nowWithTime().toString();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<List<PartyFlagIdValue>> existingCaseFlags = asylumCase.read(WITNESS_LEVEL_FLAGS);
        boolean isWitnessInterpreterRequired = asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)
                .map(yesOrNo -> yesOrNo.equals(YES))
                .orElse(false);
        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        if (witnessDetailsOptional.isPresent()) {
            Map<WitnessDetails, Optional<InterpreterLanguageRefData>> witnessRefMap = mapWitness(witnessDetailsOptional.get(), asylumCase);

            for (Map.Entry<WitnessDetails, Optional<InterpreterLanguageRefData>> entry : witnessRefMap.entrySet()) {
                WitnessDetails witness = entry.getKey();
                Optional<InterpreterLanguageRefData> refData = entry.getValue();
                Optional<PartyFlagIdValue> existingPartyFlagId = Optional.empty();
                List<CaseFlagDetail> existingFlags = getWitnessCaseFlags(existingCaseFlags, witness.getWitnessPartyId());
                Optional<CaseFlagDetail> activeFlag = getActiveTargetCaseFlag(existingFlags, INTERPRETER_LANGUAGE_FLAG);

                if (activeFlag.isPresent()) {
                    existingFlags = tryDeactivateFlags(existingFlags, refData, activeFlag.get(), isWitnessInterpreterRequired, currentDateTime);
                }

                if (refData.isPresent() && isWitnessInterpreterRequired) {
                    String flagName = getChosenSpokenLanguage(refData.get());
                    if (activeFlag.isPresent() && caseDataUpdated) {
                        existingFlags = activateCaseFlag(asylumCase, existingFlags, INTERPRETER_LANGUAGE_FLAG, flagName, currentDateTime);
                        caseDataUpdated = true;
                    } else if (activeFlag.isEmpty()) {
                        existingFlags = activateCaseFlag(asylumCase, existingFlags, INTERPRETER_LANGUAGE_FLAG, flagName, currentDateTime);
                        caseDataUpdated = true;
                    }
                }

                if (caseDataUpdated) {
                    Optional<List<PartyFlagIdValue>> finalExistingFlags = returnUpdatedExistingFlags(witness, existingFlags, existingCaseFlags);
                    asylumCase.write(WITNESS_LEVEL_FLAGS, finalExistingFlags);
                }
                caseDataUpdated = false;
            }
        } else {
            deactivateAnyActiveCaseFlags(existingCaseFlags, asylumCase, INTERPRETER_LANGUAGE_FLAG, currentDateTime);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<List<PartyFlagIdValue>> returnUpdatedExistingFlags(WitnessDetails witness, List<CaseFlagDetail> existingFlags,
                                                                        Optional<List<PartyFlagIdValue>> existingCaseFlags) {
        StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                witness.buildWitnessFullName(), StrategicCaseFlag.ROLE_ON_CASE_WITNESS, existingFlags);

        PartyFlagIdValue val = new PartyFlagIdValue(witness.getWitnessPartyId(), caseFlag);

        if (existingCaseFlags.isEmpty()) {
            existingCaseFlags = Optional.of(new ArrayList<>());
            existingCaseFlags.get().add(val);
        } else {
            boolean updated = false;
            for (int i = 0; i < existingCaseFlags.get().size(); i++) {
                PartyFlagIdValue value = existingCaseFlags.get().get(i);
                if (value.getPartyId().equals(val.getPartyId())) {
                    existingCaseFlags.get().set(i, val);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                existingCaseFlags.get().add(val);
            }
        }
        return existingCaseFlags;
    }

    private List<CaseFlagDetail> tryDeactivateFlags(List<CaseFlagDetail> existingFlags, Optional<InterpreterLanguageRefData> refData,
                                                    CaseFlagDetail activeFlag, boolean isWitnessInterpreterRequired, String currentDateTime) {
        if (refData.isPresent()) {
            String flagName = getChosenSpokenLanguage(refData.get());
            if (!isWitnessInterpreterRequired || selectedLanguageDiffers(flagName, activeFlag)) {
                existingFlags = deactivateCaseFlag(existingFlags, INTERPRETER_LANGUAGE_FLAG, currentDateTime);
                caseDataUpdated = true;
            }
        } else {
            existingFlags = deactivateCaseFlag(existingFlags, INTERPRETER_LANGUAGE_FLAG, currentDateTime);
            caseDataUpdated = true;
        }
        return existingFlags;
    }

    private Map<WitnessDetails, Optional<InterpreterLanguageRefData>> mapWitness(List<IdValue<WitnessDetails>> witnessDetails, AsylumCase asylumCase) {
        Map<WitnessDetails, Optional<InterpreterLanguageRefData>> witnessRefMap = new HashMap<>();
        for (int i = 0; i < witnessDetails.size(); i++) {
            WitnessDetails details = witnessDetails.get(i).getValue();
            Optional<InterpreterLanguageRefData> witnessRefData = asylumCase.read(mapWitnessToRefData(i), InterpreterLanguageRefData.class);
            if (witnessRefData.isEmpty() || (witnessRefData.get().getLanguageRefData() == null && witnessRefData.get().getLanguageManualEntry() == null)) {
                witnessRefData = Optional.empty();
            }
            witnessRefMap.put(details, witnessRefData);
        }
        return witnessRefMap;
    }

    private String getChosenSpokenLanguage(InterpreterLanguageRefData witnessSpokenLanguage) {
        String chosenLanguage;
        if (witnessSpokenLanguage.getLanguageManualEntry() == null || witnessSpokenLanguage.getLanguageManualEntry().isEmpty()) {
            chosenLanguage = witnessSpokenLanguage.getLanguageRefData().getValue().getLabel();
        } else {
            chosenLanguage = witnessSpokenLanguage.getLanguageManualEntryDescription();
        }
        return INTERPRETER_LANGUAGE_FLAG.getName().concat(" " + chosenLanguage);
    }

    private boolean selectedLanguageDiffers(String flagName, CaseFlagDetail activeCaseFlag) {
        return !flagName.equals(activeCaseFlag.getCaseFlagValue().getName());
    }

    private AsylumCaseFieldDefinition mapWitnessToRefData(int id) {
        switch (id) {
            case 0:
                return WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
            case 1:
                return WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE;
            case 2:
                return WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE;
            case 3:
                return WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE;
            case 4:
                return WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE;
            case 5:
                return WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE;
            case 6:
                return WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE;
            case 7:
                return WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE;
            case 8:
                return WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE;
            case 9:
                return WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE;
            default:
                return null;
        }
    }
}
