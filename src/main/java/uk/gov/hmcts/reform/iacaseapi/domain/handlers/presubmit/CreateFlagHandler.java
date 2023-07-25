package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
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

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<StrategicCaseFlag> existingCaseLevelFlags = asylumCase.read(CASE_LEVEL_FLAGS);
        Optional<StrategicCaseFlag> existingAppellantLevelFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS);
        Optional<List<IdValue<StrategicCaseFlag>>> existingWitnessLevelFlags = asylumCase.read(WITNESS_LEVEL_FLAGS);
        final Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);

        if (existingAppellantLevelFlags.isEmpty()
            || existingAppellantLevelFlags.get().getPartyName() == null
            || existingAppellantLevelFlags.get().getPartyName().isBlank()) {

            final String appellantNameForDisplay =
                asylumCase
                    .read(APPELLANT_NAME_FOR_DISPLAY, String.class)
                    .orElseThrow(() -> new IllegalStateException("appellantNameForDisplay is not present"));

            asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(appellantNameForDisplay, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT));
        } else {
            log.info("Existing Appellant Level flags: {}", existingAppellantLevelFlags);
        }

        if (existingCaseLevelFlags.isEmpty()) {
            asylumCase.write(CASE_LEVEL_FLAGS, new StrategicCaseFlag());
        } else {
            log.info("Existing Case Level flags: {}", existingCaseLevelFlags);
        }

        if (witnessDetails.isPresent()) {
            if (existingWitnessLevelFlags.isEmpty() || existingWitnessLevelFlags.get().isEmpty()) {
                asylumCase.write(WITNESS_LEVEL_FLAGS, mapWitnessesToFlag(witnessDetails.get()));
            } else {
                log.info("Existing Witness Level flags: {}", existingWitnessLevelFlags);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<IdValue<StrategicCaseFlag>> mapWitnessesToFlag(List<IdValue<WitnessDetails>> witnessDetails) {
        return witnessDetails.stream().map(details -> {
            String witnessName = details.getValue().buildWitnessFullName();
            if (witnessName.isBlank()) {
                return null;
            } else {
                StrategicCaseFlag caseFlag = new StrategicCaseFlag(witnessName, StrategicCaseFlag.ROLE_ON_CASE_WITNESS);
                return new IdValue<>(witnessName, caseFlag);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
