package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
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
