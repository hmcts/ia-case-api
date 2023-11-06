package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_WITNESS;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService;

@Slf4j
@Component
public class WitnessInterpreterLanguageFlagsHandler extends WitnessCaseFlagsHandler
        implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public WitnessInterpreterLanguageFlagsHandler(DateProvider systemDateProvider) {
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
        return DispatchPriority.LAST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        String currentDateTime = systemDateProvider.nowWithTime().toString();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        log.info("WitnessInterpreterLanguageFlagsHandler running on case {}", callback.getCaseDetails().getId());
        updateWitnessInterpreterFlags(asylumCase, currentDateTime);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void updateWitnessInterpreterFlags(AsylumCase asylumCase, String currentDateTime) {

        Map<String, WitnessDetails> witnessDetailsMap = getWitnessDetailsMap(asylumCase);
        log.info("witnessDetailsMap: {}", witnessDetailsMap);
        Map<String, Language> witnessesSpokenLanguageMap =
                getWitnessInterpreterLanguageMap(asylumCase, witnessDetailsMap);
        log.info("witnessesSpokenLanguageMap: {}", witnessesSpokenLanguageMap);
        Map<String, Language> witnessesSignLanguageMap = getWitnessSignLanguageMap(asylumCase, witnessDetailsMap);
        log.info("witnessesSignLanguageMap: {}", witnessesSignLanguageMap);
        Map<String, StrategicCaseFlag> witnessFlagsMap = getWitnessFlagsMap(asylumCase);
        log.info("witnessFlagsMap: {}", witnessFlagsMap);

        StrategicCaseFlagService strategicCaseFlagService;

        boolean caseDataUpdated = false;
        for (WitnessDetails details : witnessDetailsMap.values()) {
            boolean flagsUpdated = false;
            StrategicCaseFlag existingCaseFlag = witnessFlagsMap.get(details.getWitnessPartyId());
            strategicCaseFlagService = existingCaseFlag == null
                    ? new StrategicCaseFlagService(details.buildWitnessFullName(), ROLE_ON_CASE_WITNESS)
                    : new StrategicCaseFlagService(existingCaseFlag);

            Language selectedSpokenLanguage = witnessesSpokenLanguageMap.get(details.getWitnessPartyId());
            flagsUpdated |= tryUpdate(
                strategicCaseFlagService, INTERPRETER_LANGUAGE_FLAG, currentDateTime, selectedSpokenLanguage);

            Language selectedSignLanguage = witnessesSignLanguageMap.get(details.getWitnessPartyId());
            flagsUpdated |= tryUpdate(
                strategicCaseFlagService, SIGN_LANGUAGE_INTERPRETER, currentDateTime, selectedSignLanguage);

            if (flagsUpdated) {
                caseDataUpdated = true;
                witnessFlagsMap.put(details.getWitnessPartyId(), strategicCaseFlagService.getStrategicCaseFlag());
            }
        }

        List<PartyFlagIdValue> witnessFlagsIdValues;
        if (caseDataUpdated) {
            witnessFlagsIdValues = witnessFlagsMap
                .entrySet().stream()
                .map(entry -> new PartyFlagIdValue(entry.getKey(), entry.getValue())).toList();
            asylumCase
                .write(WITNESS_LEVEL_FLAGS, witnessFlagsIdValues);
        }
    }

    private static boolean tryUpdate(
        StrategicCaseFlagService strategicCaseFlagService,
        StrategicCaseFlagType caseFlagType,
        String currentDateTime,
        Language selectedLanguage) {

        return selectedLanguage == null || selectedLanguage.isEmpty()
            ? strategicCaseFlagService.deactivateFlag(caseFlagType, currentDateTime)
            : strategicCaseFlagService
                .activateFlag(caseFlagType, YES, currentDateTime, selectedLanguage);
    }
}
