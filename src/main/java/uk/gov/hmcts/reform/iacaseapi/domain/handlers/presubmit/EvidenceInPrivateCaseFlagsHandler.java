package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IN_CAMERA_COURT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_IN_CAMERA_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.CASE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;
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
public class EvidenceInPrivateCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public EvidenceInPrivateCaseFlagsHandler(DateProvider systemDateProvider) {
        this.systemDateProvider = systemDateProvider;
    }

    public static String CASE_GRANTED =  "Granted";
    public static String CASE_REFUSED =  "Refused";


    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = List.of(REVIEW_HEARING_REQUIREMENTS, UPDATE_HEARING_ADJUSTMENTS);
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
        StrategicCaseFlagService strategicCaseFlagService = new StrategicCaseFlagService(asylumCase
            .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class).orElse(null));

        boolean inCameraCourtRequested = asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)
            .map(cameraCourtNeeded -> YES == cameraCourtNeeded).orElse(false);
        boolean inCameraCourtGranted = asylumCase.read(IS_IN_CAMERA_COURT_ALLOWED, String.class)
            .map(decision -> CASE_GRANTED.equals(decision))
            .orElse(false);

        String currentDateTime = systemDateProvider.nowWithTime().toString();

        boolean caseDataUpdated;

        if (inCameraCourtRequested && inCameraCourtGranted) {
            strategicCaseFlagService
                .initializeIfEmpty(HandlerUtils.getAppellantFullName(asylumCase), ROLE_ON_CASE_APPELLANT);

            caseDataUpdated = strategicCaseFlagService.activateFlag(CASE_GIVEN_IN_PRIVATE, YES, currentDateTime);
        } else {
            caseDataUpdated = strategicCaseFlagService.deactivateFlag(CASE_GIVEN_IN_PRIVATE, currentDateTime);
        }

        if (caseDataUpdated) {

            asylumCase.write(
                APPELLANT_LEVEL_FLAGS,
                strategicCaseFlagService.getStrategicCaseFlag());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}