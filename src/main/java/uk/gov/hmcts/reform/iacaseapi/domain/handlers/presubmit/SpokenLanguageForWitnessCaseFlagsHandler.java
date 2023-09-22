package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService;

@Component
public class SpokenLanguageForWitnessCaseFlagsHandler extends WitnessCaseFlagsHandler
        implements PreSubmitCallbackHandler<AsylumCase> {

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

        String currentDateTime = systemDateProvider.nowWithTime().toString();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Map<String, WitnessDetails> witnessDetailsMap = getWitnessDetailsMap(asylumCase);
        Map<String, Language> witnessInterpreterLanguageMap =
            getWitnessInterpreterLanguageMap(asylumCase, witnessDetailsMap);
        Map<String, StrategicCaseFlag> witnessFlagsMap = getWitnessFlags(asylumCase);

        boolean caseDataUpdated = false;
        for (WitnessDetails details : witnessDetailsMap.values()) {
            StrategicCaseFlagService strategicCaseFlagService =
                new StrategicCaseFlagService(witnessFlagsMap.get(details.getWitnessPartyId()));
            StrategicCaseFlag updatedInterpreterLanguageFlag = tryUpdateFlags(
                strategicCaseFlagService, details, witnessInterpreterLanguageMap, currentDateTime);
            if (updatedInterpreterLanguageFlag != null) {
                caseDataUpdated = true;
                witnessFlagsMap.put(details.getWitnessPartyId(), updatedInterpreterLanguageFlag);
            }
        }

        if (caseDataUpdated) {
            List<PartyFlagIdValue> witnessFlagsIdValues = witnessFlagsMap
                .entrySet().stream()
                .map(entry -> new PartyFlagIdValue(entry.getKey(), entry.getValue())).toList();
            asylumCase
                .write(WITNESS_LEVEL_FLAGS, Optional.of(witnessFlagsIdValues));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private StrategicCaseFlag tryUpdateFlags(
        StrategicCaseFlagService strategicCaseFlagService,
        WitnessDetails details,
        Map<String, Language> witnessInterpreterLanguageMap,
        String currentDateTime) {

        boolean caseDataUpdated = false;
        if (witnessInterpreterLanguageMap.containsKey(details.getWitnessPartyId())) {
            // Try to activate flags for witnesses with interpreter language

            Language interpreterLanguage = witnessInterpreterLanguageMap.get(details.getWitnessPartyId());
            strategicCaseFlagService.initializeIfEmpty(
                details.buildWitnessFullName(), StrategicCaseFlagService.ROLE_ON_CASE_WITNESS);
            caseDataUpdated = strategicCaseFlagService
                .activateFlag(INTERPRETER_LANGUAGE_FLAG, YES, currentDateTime, interpreterLanguage);
        } else {
            // Try to deactivate flags for witnesses without interpreter language

            caseDataUpdated = strategicCaseFlagService.deactivateFlag(INTERPRETER_LANGUAGE_FLAG, currentDateTime);
        }

        return caseDataUpdated ? strategicCaseFlagService.getStrategicCaseFlag() : null;
    }
}
