package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService;

@Component
public class SpokenLanguageForInterpreterFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public SpokenLanguageForInterpreterFlagsHandler(DateProvider systemDateProvider) {
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
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Language selectedSpokenLanguage = asylumCase
            .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
            .map(Language::of).orElse(null);
        boolean interpreterServiceNeeded = asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
            .map(interpreterNeeded -> YES == interpreterNeeded).orElse(false);
        StrategicCaseFlagService strategicCaseFlagService = new StrategicCaseFlagService(asylumCase
            .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class).orElse(null));
        String currentDateTime = systemDateProvider.nowWithTime().toString();

        boolean caseDataUpdated;
        if (interpreterServiceNeeded && selectedSpokenLanguage != null) {
            strategicCaseFlagService.initializeIfEmpty(
                HandlerUtils.getAppellantFullName(asylumCase), StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT);
            caseDataUpdated = strategicCaseFlagService
                .activateFlag(INTERPRETER_LANGUAGE_FLAG, YES, currentDateTime, selectedSpokenLanguage);
        } else if (interpreterServiceNeeded) {
            strategicCaseFlagService.initializeIfEmpty(
                HandlerUtils.getAppellantFullName(asylumCase), StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT);
            caseDataUpdated = strategicCaseFlagService
                .activateFlag(INTERPRETER_LANGUAGE_FLAG, YES, currentDateTime);
        } else {
            caseDataUpdated = strategicCaseFlagService.deactivateFlag(INTERPRETER_LANGUAGE_FLAG, currentDateTime);
        }

        if (caseDataUpdated) {
            asylumCase.write(
                APPELLANT_LEVEL_FLAGS,
                strategicCaseFlagService.getStrategicCaseFlag());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}


