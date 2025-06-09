package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
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

import java.util.EnumSet;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.DETAINED_INDIVIDUAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT;

@Component
class DetainedIndividualFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final EnumSet<Event> SUPPORTED_EVENTS = EnumSet.of(
            SUBMIT_APPEAL, MARK_APPEAL_AS_DETAINED
    );

    private final DateProvider systemDateProvider;

    public DetainedIndividualFlagHandler(DateProvider systemDateProvider) {
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && SUPPORTED_EVENTS.contains(callback.getEvent());
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

        YesOrNo appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO);

        if (appellantInDetention.equals(YES)) {
            StrategicCaseFlagService strategicCaseFlagService = asylumCase
                    .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
                    .map(StrategicCaseFlagService::new)
                    .orElseGet(() ->
                            new StrategicCaseFlagService(HandlerUtils.getAppellantFullName(asylumCase), ROLE_ON_CASE_APPELLANT));

            boolean caseDataUpdated = strategicCaseFlagService.activateFlag(
                    DETAINED_INDIVIDUAL, YES, systemDateProvider.nowWithTime().toString());

            if (caseDataUpdated) {
                asylumCase.write(
                        APPELLANT_LEVEL_FLAGS,
                        strategicCaseFlagService.getStrategicCaseFlag());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
