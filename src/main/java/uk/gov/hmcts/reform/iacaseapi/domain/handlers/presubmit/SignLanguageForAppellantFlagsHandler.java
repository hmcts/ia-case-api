package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService;

@Component
public class SignLanguageForAppellantFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public SignLanguageForAppellantFlagsHandler(DateProvider systemDateProvider) {
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
        Language selectedSignLanguage = asylumCase
            .read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
            .map(Language::of).orElse(null);
        boolean signLanguageServiceNeeded = asylumCase.read(IS_SIGN_SERVICES_NEEDED, YesOrNo.class)
            .map(serviceNeeded -> YES == serviceNeeded).orElse(false);
        StrategicCaseFlagService strategicCaseFlagService = asylumCase
            .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(StrategicCaseFlagService::new)
            .orElseGet(() ->
                new StrategicCaseFlagService(HandlerUtils.getAppellantFullName(asylumCase), ROLE_ON_CASE_APPELLANT));
        String currentDateTime = systemDateProvider.nowWithTime().toString();

        boolean caseDataUpdated;
        if (signLanguageServiceNeeded && selectedSignLanguage != null) {
            caseDataUpdated = strategicCaseFlagService
                .activateFlag(SIGN_LANGUAGE, YES, currentDateTime, selectedSignLanguage);
        } else if (signLanguageServiceNeeded) {
            caseDataUpdated = strategicCaseFlagService
                .activateFlag(SIGN_LANGUAGE, YES, currentDateTime);
        } else {
            caseDataUpdated = strategicCaseFlagService.deactivateFlag(SIGN_LANGUAGE, currentDateTime);
        }

        if (caseDataUpdated) {
            asylumCase.write(
                APPELLANT_LEVEL_FLAGS,
                strategicCaseFlagService.getStrategicCaseFlag());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
