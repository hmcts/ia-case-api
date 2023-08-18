package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGACY_CASE_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVE_FLAG_TYPE_OF_FLAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RemoveFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REMOVE_FLAG;
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

        final DynamicList flagType = asylumCase.read(REMOVE_FLAG_TYPE_OF_FLAG, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("removeFlagTypeOfFlag is missing"));

        final Optional<List<IdValue<LegacyCaseFlag>>> maybeExistingCaseFlags = asylumCase.read(LEGACY_CASE_FLAGS);
        final List<IdValue<LegacyCaseFlag>> existingCaseFlags = maybeExistingCaseFlags.orElse(Collections.emptyList());

        final List<IdValue<LegacyCaseFlag>> newCaseFlags = new ArrayList<>();

        for (IdValue<LegacyCaseFlag> idValue : existingCaseFlags) {
            if (!idValue.getId().equals(flagType.getValue().getCode())) {
                newCaseFlags.add(idValue);
            } else {
                clearDisplayFlags(idValue.getValue().getLegacyCaseFlagType(), asylumCase);
            }
        }

        asylumCase.write(LEGACY_CASE_FLAGS, newCaseFlags);
        asylumCase.clear(REMOVE_FLAG_TYPE_OF_FLAG);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public void clearDisplayFlags(CaseFlagType caseFlagType, AsylumCase asylumCase) {
        CaseFlagType flag = Stream.of(CaseFlagType.values())
            .filter(f -> f.equals(caseFlagType))
            .findFirst().orElse(CaseFlagType.UNKNOWN);

        AsylumCaseFieldDefinition flagExistsField = AsylumCaseFieldDefinition
            .valueOf("CASE_FLAG_" + flag.name() + "_EXISTS");
        asylumCase.clear(flagExistsField);
        AsylumCaseFieldDefinition flagAdditionalInformationField = AsylumCaseFieldDefinition
            .valueOf("CASE_FLAG_" + flag.name() + "_ADDITIONAL_INFORMATION");
        asylumCase.clear(flagAdditionalInformationField);

    }
}
