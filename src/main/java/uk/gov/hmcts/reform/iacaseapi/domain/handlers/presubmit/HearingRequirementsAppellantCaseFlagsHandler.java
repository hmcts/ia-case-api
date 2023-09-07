package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LOOP_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_ROOM_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
class HearingRequirementsAppellantCaseFlagsHandler
        extends AppellantCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public HearingRequirementsAppellantCaseFlagsHandler(DateProvider systemDateProvider) {
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
        StrategicCaseFlag caseFlags = getOrCreateAppellantCaseFlags(asylumCase);
        boolean isHearingLoopNeeded = asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)
            .map(hearingLoopNeeded -> YesOrNo.YES == hearingLoopNeeded).orElse(false);
        boolean isHearingRoomNeeded = asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)
            .map(hearingRoomNeeded -> YesOrNo.YES == hearingRoomNeeded).orElse(false);
        List<CaseFlagDetail> existingCaseFlagDetails = caseFlags.getDetails();
        String currentDateTime = systemDateProvider.nowWithTime().toString();

        boolean caseDataUpdated = false;

        if (isHearingLoopNeeded && !hasActiveTargetCaseFlag(existingCaseFlagDetails, HEARING_LOOP)) {
            existingCaseFlagDetails = activateCaseFlag(
                asylumCase, existingCaseFlagDetails, HEARING_LOOP, currentDateTime);
            caseDataUpdated = true;
        }
        if (isHearingRoomNeeded && !hasActiveTargetCaseFlag(existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS)) {
            existingCaseFlagDetails = activateCaseFlag(
                asylumCase, existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS, currentDateTime);
            caseDataUpdated = true;
        }
        if (!isHearingLoopNeeded && hasActiveTargetCaseFlag(existingCaseFlagDetails, HEARING_LOOP)) {
            existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, HEARING_LOOP, currentDateTime);
            caseDataUpdated = true;
        }
        if (!isHearingRoomNeeded && hasActiveTargetCaseFlag(existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS)) {
            existingCaseFlagDetails = deactivateCaseFlag(
                existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS, currentDateTime);
            caseDataUpdated = true;
        }

        if (caseDataUpdated) {

            asylumCase.write(
                APPELLANT_LEVEL_FLAGS,
                new StrategicCaseFlag(
                    caseFlags.getPartyName(),
                    StrategicCaseFlag.ROLE_ON_CASE_APPELLANT,
                    existingCaseFlagDetails));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}