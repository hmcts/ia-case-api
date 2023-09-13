package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IN_CAMERA_COURT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_IN_CAMERA_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.CASE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;

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
public class EvidenceInPrivateCaseFlagsHandler
        extends AppellantCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

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
        StrategicCaseFlag caseFlags = getOrCreateAppellantCaseFlags(asylumCase);

        boolean inCameraCourt = asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)
            .map(cameraCourtNeeded -> YesOrNo.YES == cameraCourtNeeded).orElse(false);

        String isInCameraCourtAllowed = asylumCase
            .read(IS_IN_CAMERA_COURT_ALLOWED, String.class)
            .orElse(CASE_REFUSED);

        List<CaseFlagDetail> existingCaseFlagDetails = caseFlags.getDetails();
        String currentDateTime = systemDateProvider.nowWithTime().toString();

        boolean caseDataUpdated = false;

        if (isInCameraCourtAllowed.equals(CASE_GRANTED)
            && !hasActiveTargetCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE)) {
            existingCaseFlagDetails = activateCaseFlag(
                asylumCase, existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE, currentDateTime);
            caseDataUpdated = true;
        }

        if (!inCameraCourt && hasActiveTargetCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE)) {
            existingCaseFlagDetails = deactivateCaseFlags(
                existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE, currentDateTime);
            caseDataUpdated = true;
        }

        if (isInCameraCourtAllowed.equals(CASE_REFUSED)
            && hasActiveTargetCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE)) {
            existingCaseFlagDetails = deactivateCaseFlags(
                existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE, currentDateTime);
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