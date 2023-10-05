package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IN_CAMERA_COURT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LOOP_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_ROOM_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_IN_CAMERA_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.CASE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT;

import java.util.List;
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

@Component
class HearingRequirementsAppellantFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static String CASE_GRANTED =  "Granted";
    public static String CASE_REFUSED =  "Refused";
    private final DateProvider systemDateProvider;

    public HearingRequirementsAppellantFlagsHandler(DateProvider systemDateProvider) {
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = List.of(
            REVIEW_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS, UPDATE_HEARING_ADJUSTMENTS);
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
        StrategicCaseFlagService strategicCaseFlagService = asylumCase
            .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(StrategicCaseFlagService::new)
            .orElseGet(() ->
                new StrategicCaseFlagService(HandlerUtils.getAppellantFullName(asylumCase), ROLE_ON_CASE_APPELLANT));
        String currentDateTime = systemDateProvider.nowWithTime().toString();
        boolean isHearingLoopNeeded = asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)
            .map(hearingLoopNeeded -> YesOrNo.YES == hearingLoopNeeded).orElse(false);
        boolean isHearingRoomNeeded = asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)
            .map(hearingRoomNeeded -> YesOrNo.YES == hearingRoomNeeded).orElse(false);
        boolean inCameraCourtRequested = asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)
            .map(cameraCourtNeeded -> YES == cameraCourtNeeded).orElse(false);
        boolean inCameraCourtGranted = asylumCase.read(IS_IN_CAMERA_COURT_ALLOWED, String.class)
            .map(decision -> CASE_GRANTED.equals(decision))
            .orElse(false);
        boolean caseDataUpdated = false;

        switch (callback.getEvent()) {
            case REVIEW_HEARING_REQUIREMENTS -> {
                caseDataUpdated |= handleHearingLoopFlag(
                    strategicCaseFlagService, currentDateTime, isHearingLoopNeeded);
                caseDataUpdated |= handleHearingRoomFlag(strategicCaseFlagService, currentDateTime, isHearingRoomNeeded);
                caseDataUpdated |= handleEvidenceInPrivate(
                    strategicCaseFlagService, currentDateTime, inCameraCourtRequested && inCameraCourtGranted);
            }
            case UPDATE_HEARING_REQUIREMENTS -> {
                caseDataUpdated |= handleHearingLoopFlag(
                    strategicCaseFlagService, currentDateTime, isHearingLoopNeeded);
                caseDataUpdated |= handleHearingRoomFlag(
                    strategicCaseFlagService, currentDateTime, isHearingRoomNeeded);
            }
            default -> caseDataUpdated |= handleEvidenceInPrivate(
                strategicCaseFlagService, currentDateTime, inCameraCourtRequested && inCameraCourtGranted);
        }

        if (caseDataUpdated) {

            asylumCase.write(
                APPELLANT_LEVEL_FLAGS,
                strategicCaseFlagService.getStrategicCaseFlag());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean handleHearingLoopFlag(
        StrategicCaseFlagService caseFlagService, String currentDateTime, boolean hearingLoopNeeded) {

        return hearingLoopNeeded ? caseFlagService.activateFlag(HEARING_LOOP, YES, currentDateTime)
            : caseFlagService.deactivateFlag(HEARING_LOOP, currentDateTime);
    }

    private boolean handleHearingRoomFlag(
        StrategicCaseFlagService caseFlagService, String currentDateTime, boolean hearingRoomNeeded) {

        return hearingRoomNeeded ? caseFlagService.activateFlag(STEP_FREE_WHEELCHAIR_ACCESS, YES, currentDateTime)
            : caseFlagService.deactivateFlag(STEP_FREE_WHEELCHAIR_ACCESS, currentDateTime);
    }

    private boolean handleEvidenceInPrivate(
        StrategicCaseFlagService caseFlagService, String currentDateTime, boolean inCameraCourtRequestedAndGranted) {

        return inCameraCourtRequestedAndGranted ? caseFlagService.activateFlag(CASE_GIVEN_IN_PRIVATE, YES, currentDateTime)
            : caseFlagService.deactivateFlag(CASE_GIVEN_IN_PRIVATE, currentDateTime);
    }

}