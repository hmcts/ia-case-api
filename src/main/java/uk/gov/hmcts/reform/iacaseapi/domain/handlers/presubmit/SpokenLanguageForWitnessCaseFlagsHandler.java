package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Component
public class SpokenLanguageForWitnessCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public SpokenLanguageForWitnessCaseFlagsHandler(DateProvider systemDateProvider) {
        this.systemDateProvider = systemDateProvider;
    }

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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<CaseDetails<AsylumCase>> asylumCaseBefore = callback.getCaseDetailsBefore();

        Optional<List<PartyFlagIdValue>> existingCaseFlags = asylumCase.read(WITNESS_LEVEL_FLAGS);

        boolean isWitnessInterpreterRequired = asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)
                .map(yesOrNo -> yesOrNo.equals(YES))
                .orElse(false);

        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);
            if (witnessDetailsOptional.isPresent() && isWitnessInterpreterRequired) {
                Map<WitnessDetails, Optional<InterpreterLanguageRefData>> witnessRefMap = mapWitness(witnessDetailsOptional.get(), asylumCase);

                // loop through each witness
                for (Map.Entry<WitnessDetails, Optional<InterpreterLanguageRefData>> entry : witnessRefMap.entrySet()) {
                    boolean caseDataUpdated = false;
                    WitnessDetails witness = entry.getKey();
                    Optional<InterpreterLanguageRefData> refData = entry.getValue();
                    Optional<PartyFlagIdValue> existingPartyFlagId = Optional.empty();

                    // check for existing case flags on the case, maps case flag based on party id
                    if (!existingCaseFlags.isEmpty()) {
                        existingPartyFlagId = existingCaseFlags.get()
                                .stream()
                                .filter(partyFlagIdValue -> partyFlagIdValue.getPartyId().equals(witness.getWitnessPartyId()))
                                .findFirst();
                    }

                    List<CaseFlagDetail> existingFlags = new ArrayList<>();

                    // if we have an existing case flag with the partyid then we get the details of all the case flags related to the witness
                    if (existingPartyFlagId.isPresent()) {
                        existingFlags = existingPartyFlagId.get().getValue().getDetails();
                    }

                    // check if the flag is active
                    Optional<CaseFlagDetail> activeFlag = getActiveTargetCaseFlag(existingFlags, INTERPRETER_LANGUAGE_FLAG);

                    // check is this present every time ## potential issue
                    if (refData.isPresent()) {
                        String flagName = getChosenSpokenLanguage(refData.get());
                        if (activeFlag.isEmpty()) {
                            existingFlags = activateCaseFlag(asylumCase, existingFlags, INTERPRETER_LANGUAGE_FLAG, flagName);
                            caseDataUpdated = true;
                        } else if (asylumCaseBefore.isPresent() &&
                                selectedLanguageDiffers(flagName, activeFlag.get())) {
                            existingFlags = deactivateCaseFlag(existingFlags, INTERPRETER_LANGUAGE_FLAG);
                            existingFlags = activateCaseFlag(asylumCase, existingFlags, INTERPRETER_LANGUAGE_FLAG, flagName);
                            caseDataUpdated = true;
                        }
                    } else if (activeFlag.isPresent()) {
                        existingFlags = deactivateCaseFlag(existingFlags, INTERPRETER_LANGUAGE_FLAG);
                        caseDataUpdated = true;
                    }

                    if (caseDataUpdated) {
                        StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                                witness.buildWitnessFullName(), StrategicCaseFlag.ROLE_ON_CASE_WITNESS, existingFlags);

                        PartyFlagIdValue val = new PartyFlagIdValue(witness.getWitnessPartyId(), caseFlag);

                        if (existingCaseFlags.isEmpty()){
                            existingCaseFlags = Optional.of(new ArrayList<>());
                            existingCaseFlags.get().add(val);
                        } else {
                            boolean updated = false;
                            for(int i = 0; i < existingCaseFlags.get().size(); i++){
                                PartyFlagIdValue value = existingCaseFlags.get().get(i);
                                if (value.getPartyId().equals(val.getPartyId())){
                                    existingCaseFlags.get().set(i, val);
                                    updated = true;
                                    break;
                                }
                            }
                            if (!updated){
                                existingCaseFlags.get().add(val);
                            }
                        }
                        asylumCase.write(WITNESS_LEVEL_FLAGS, existingCaseFlags);
                    }
                }
            } else {
                deactivateAnyActiveCaseFlags(existingCaseFlags);
                asylumCase.write(WITNESS_LEVEL_FLAGS, existingCaseFlags);
            }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void deactivateAnyActiveCaseFlags(Optional<List<PartyFlagIdValue>> existingCaseFlags) {
        if (existingCaseFlags.isPresent()) {
            for (int i = 0; i < existingCaseFlags.get().size(); i++){
                PartyFlagIdValue partyFlag = existingCaseFlags.get().get(i);
                Optional<CaseFlagDetail> activeFlag = getActiveTargetCaseFlag(partyFlag.getValue().getDetails(), INTERPRETER_LANGUAGE_FLAG);
                if (activeFlag.isPresent()){
                   List<CaseFlagDetail> flags = deactivateCaseFlag(partyFlag.getValue().getDetails(), INTERPRETER_LANGUAGE_FLAG);
                    StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                            partyFlag.getValue().getPartyName(), StrategicCaseFlag.ROLE_ON_CASE_WITNESS, flags);
                   existingCaseFlags.get().set(i, new PartyFlagIdValue(partyFlag.getPartyId(), caseFlag));
                }
            }
        }
    }

    private Map<WitnessDetails, Optional<InterpreterLanguageRefData>> mapWitness(List<IdValue<WitnessDetails>> witnessDetails, AsylumCase asylumCase) {
        Map<WitnessDetails, Optional<InterpreterLanguageRefData>> witnessRefMap = new HashMap<>();
        for (int i = 0; i < witnessDetails.size(); i++) {
            WitnessDetails details = witnessDetails.get(i).getValue();
            Optional<InterpreterLanguageRefData> witnessRefData = asylumCase.read(mapWitnessToRefData(i), InterpreterLanguageRefData.class);
            if (witnessRefData.isEmpty() || (witnessRefData.get().getLanguageRefData() == null && witnessRefData.get().getLanguageManualEntry() == null))
                witnessRefData = Optional.empty();
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

    private List<CaseFlagDetail> activateCaseFlag(
            AsylumCase asylumCase,
            List<CaseFlagDetail> existingCaseFlagDetails,
            StrategicCaseFlagType caseFlagType,
            String flagName) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .flagCode(caseFlagType.getFlagCode())
                .name(flagName)
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .dateTimeCreated(systemDateProvider.nowWithTime().toString())
                .build();
        String caseFlagId = asylumCase.read(CASE_FLAG_ID, String.class).orElse(UUID.randomUUID().toString());
        List<CaseFlagDetail> caseFlagDetails = existingCaseFlagDetails.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(existingCaseFlagDetails);
        caseFlagDetails.add(new CaseFlagDetail(caseFlagId, caseFlagValue));

        return caseFlagDetails;
    }

    private List<CaseFlagDetail> deactivateCaseFlag(
            List<CaseFlagDetail> caseFlagDetails,
            StrategicCaseFlagType caseFlagType) {
        if (hasActiveTargetCaseFlag(caseFlagDetails, caseFlagType)) {
            caseFlagDetails = caseFlagDetails.stream().map(detail -> {
                CaseFlagValue value = detail.getCaseFlagValue();
                if (isActiveTargetCaseFlag(value, caseFlagType)) {
                    return new CaseFlagDetail(detail.getId(), CaseFlagValue.builder()
                            .flagCode(value.getFlagCode())
                            .name(value.getName())
                            .status("Inactive")
                            .dateTimeModified(systemDateProvider.nowWithTime().toString())
                            .hearingRelevant(value.getHearingRelevant())
                            .build());
                } else {
                    return detail;
                }
            }).collect(Collectors.toList());
        }

        return caseFlagDetails;
    }

    private boolean hasActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
                .stream()
                .anyMatch(flagDetail -> isActiveTargetCaseFlag(flagDetail.getCaseFlagValue(), caseFlagType));
    }

    private Optional<CaseFlagDetail> getActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
                .stream()
                .filter(caseFlagDetail -> isActiveTargetCaseFlag(caseFlagDetail.getCaseFlagValue(), caseFlagType))
                .findFirst();
    }

    private boolean isActiveTargetCaseFlag(CaseFlagValue value, StrategicCaseFlagType targetCaseFlagType) {
        return Objects.equals(value.getFlagCode(), targetCaseFlagType.getFlagCode())
                && Objects.equals(value.getStatus(), "Active");
    }

    private String getwitnessDisplayName(Optional<StrategicCaseFlag> existingCaseFlags, AsylumCase asylumCase) {

        return existingCaseFlags.isPresent()
                ? existingCaseFlags.get().getPartyName()
                : asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
            final String appellantGivenNames = asylumCase
                    .read(APPELLANT_GIVEN_NAMES, String.class).orElse(null);
            final String appellantFamilyName =
                    asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(null);
            return !(appellantGivenNames == null || appellantFamilyName == null) ?
                    appellantGivenNames + " " + appellantFamilyName : null;
        });
    }

    private AsylumCaseFieldDefinition mapWitnessToRefData(int id) {
        switch (id) {
            case 0: return WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
            case 1: return WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE;
            case 2: return WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE;
            case 3: return WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE;
            case 4: return WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE;
            case 5: return WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE;
            case 6: return WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE;
            case 7: return WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE;
            case 8: return WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE;
            case 9: return WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE;
            default: return null;
        }
    }
}
