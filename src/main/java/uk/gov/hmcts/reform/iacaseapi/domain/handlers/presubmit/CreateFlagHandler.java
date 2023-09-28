package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
class CreateFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.CREATE_FLAG;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        handleAppellantLevelFlags(asylumCase);
        handleCaseLevelFlags(asylumCase);
        handleWitnessLevelFlags(asylumCase);
        handleInterpreterLevelFlags(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void handleAppellantLevelFlags(AsylumCase asylumCase) {
        final String appellantFullName = HandlerUtils.getAppellantFullName(asylumCase);
        asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .ifPresentOrElse(existingAppellantLevelFlags -> {
                    if (!Objects.equals(existingAppellantLevelFlags.getPartyName(), appellantFullName)) {
                        StrategicCaseFlag updatedAppellantLevelFlags = new StrategicCaseFlag(appellantFullName,
                            StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingAppellantLevelFlags.getDetails());
                        asylumCase.write(APPELLANT_LEVEL_FLAGS, updatedAppellantLevelFlags);
                    }
                }, () -> asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(
                    appellantFullName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT))
            );
    }

    private void handleCaseLevelFlags(AsylumCase asylumCase) {
        if (asylumCase.read(CASE_LEVEL_FLAGS).isEmpty()) {
            asylumCase.write(CASE_LEVEL_FLAGS, new StrategicCaseFlag());
        }
    }

    private void handleWitnessLevelFlags(AsylumCase asylumCase) {
        Optional<List<PartyFlagIdValue>> witnessLevelFlagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        final Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        witnessDetailsOptional.ifPresent(idValues -> {
            final List<WitnessDetails> witnessDetailsList = idValues.stream().map(IdValue::getValue).toList();
            witnessLevelFlagsOptional.ifPresentOrElse(
                existingWitnessFlags -> asylumCase
                    .write(WITNESS_LEVEL_FLAGS, mapWitnessesToFlag(witnessDetailsList, existingWitnessFlags)),
                () -> asylumCase
                    .write(WITNESS_LEVEL_FLAGS, mapWitnessesToFlag(witnessDetailsList, Collections.emptyList()))
            );
        });
    }

    private void handleInterpreterLevelFlags(AsylumCase asylumCase) {
        Optional<List<PartyFlagIdValue>> interpreterLevelFlagsOptional = asylumCase.read(INTERPRETER_LEVEL_FLAGS);
        final Optional<List<IdValue<InterpreterDetails>>> interpreterDetailsOptional = asylumCase.read(INTERPRETER_DETAILS);

        interpreterDetailsOptional.ifPresent(idValues -> {
            final List<InterpreterDetails> interpreterDetailsList = idValues.stream().map(IdValue::getValue).toList();
            interpreterLevelFlagsOptional.ifPresentOrElse(
                    existingWitnessFlags -> asylumCase
                            .write(INTERPRETER_LEVEL_FLAGS, mapInterpreterToFlag(interpreterDetailsList, existingWitnessFlags)),
                    () -> asylumCase
                            .write(INTERPRETER_LEVEL_FLAGS, mapInterpreterToFlag(interpreterDetailsList, Collections.emptyList()))
            );
        });
    }

    private List<PartyFlagIdValue> mapInterpreterToFlag(
            List<InterpreterDetails> interpreterDetailsList, List<PartyFlagIdValue> existingInterpreterFlags) {
        Map<String, StrategicCaseFlag> idToFlagMap = existingInterpreterFlags.isEmpty()
                ? Collections.emptyMap()
                : existingInterpreterFlags.stream()
                .collect(Collectors.toMap(PartyFlagIdValue::getPartyId, PartyFlagIdValue::getValue));

        return interpreterDetailsList.stream()
                .filter(interpreterDetails -> interpreterDetails.getInterpreterId() != null)
                .map(interpreterDetails -> {
                    List<CaseFlagDetail> flagDetails = Collections.emptyList();
                    String interpreterName = interpreterDetails.buildInterpreterFullName();
                    if (idToFlagMap.containsKey(interpreterDetails.getInterpreterId())) {
                        StrategicCaseFlag existingFlag = idToFlagMap.get(interpreterDetails.getInterpreterId());
                        flagDetails = existingFlag.getDetails();
                    }
                    return new PartyFlagIdValue(interpreterDetails.getInterpreterId(), new StrategicCaseFlag(
                            interpreterName, StrategicCaseFlag.ROLE_ON_CASE_INTERPRETER, flagDetails));
                }).collect(Collectors.toList());
    }

    private List<PartyFlagIdValue> mapWitnessesToFlag(
        List<WitnessDetails> witnessDetailsList, List<PartyFlagIdValue> existingWitnessFlags) {
        Map<String, StrategicCaseFlag> idToFlagMap = existingWitnessFlags.isEmpty()
            ? Collections.emptyMap()
            : existingWitnessFlags.stream()
                .collect(Collectors.toMap(PartyFlagIdValue::getPartyId, PartyFlagIdValue::getValue));

        // Flags are created only for witnesses with party ID set to a non-null value
        return witnessDetailsList.stream()
            .filter(witnessDetails -> witnessDetails.getWitnessPartyId() != null)
            .map(witnessDetails -> {
                List<CaseFlagDetail> flagDetails = Collections.emptyList();
                String witnessName = witnessDetails.buildWitnessFullName();
                if (idToFlagMap.containsKey(witnessDetails.getWitnessPartyId())) {
                    StrategicCaseFlag existingFlag = idToFlagMap.get(witnessDetails.getWitnessPartyId());
                    flagDetails = existingFlag.getDetails();
                }
                return new PartyFlagIdValue(witnessDetails.getWitnessPartyId(), new StrategicCaseFlag(
                    witnessName, StrategicCaseFlag.ROLE_ON_CASE_WITNESS, flagDetails));
            }).collect(Collectors.toList());
    }
}
