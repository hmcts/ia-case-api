package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

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
        Optional<StrategicCaseFlag> existingCaseflags = asylumCase
                .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class);
        String appellantDisplayName = getAppellantDisplayName(existingCaseflags, asylumCase);

        boolean inCameraCourt = asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)
                .map(cameraCourtNeeded -> YesOrNo.YES == cameraCourtNeeded).orElse(false);

        String isInCameraCourtAllowed = asylumCase
                .read(IS_IN_CAMERA_COURT_ALLOWED, String.class)
                .orElse(CASE_REFUSED);

        List<CaseFlagDetail> existingCaseFlagDetails = existingCaseflags
                .map(StrategicCaseFlag::getDetails).orElse(Collections.emptyList());

        boolean caseDataUpdated = false;

        if (isInCameraCourtAllowed.equals(CASE_GRANTED) && !hasActiveTargetCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE)) {
            existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE);
            caseDataUpdated = true;
        }

        if (!inCameraCourt && hasActiveTargetCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE)) {
            existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE);
            caseDataUpdated = true;
        }

        if (isInCameraCourtAllowed.equals(CASE_REFUSED) && hasActiveTargetCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE)) {
            existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, CASE_GIVEN_IN_PRIVATE);
            caseDataUpdated = true;
        }

        if (caseDataUpdated) {
            if (appellantDisplayName == null) {
                throw new IllegalStateException("Appellant full name is not present");
            }

            asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(
                    appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingCaseFlagDetails));
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<CaseFlagDetail> activateCaseFlag(
            AsylumCase asylumCase,
            List<CaseFlagDetail> existingCaseFlagDetails,
            StrategicCaseFlagType caseFlagType) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .flagCode(caseFlagType.getFlagCode())
                .name(caseFlagType.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .dateTimeCreated(systemDateProvider.nowWithTime().toString())
                .build();
        String caseFlagId = asylumCase.read(CASE_FLAG_ID, String.class).orElse(UUID.randomUUID().toString());
        List<CaseFlagDetail> caseFlagDetails = existingCaseFlagDetails.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(existingCaseFlagDetails);
        caseFlagDetails.add(new CaseFlagDetail(caseFlagId, caseFlagValue));

        return caseFlagDetails;
    }

    private List<CaseFlagDetail> deactivateCaseFlag(
            List<CaseFlagDetail> caseFlagDetails,
            StrategicCaseFlagType caseFlagType) {
        if (hasActiveTargetCaseFlag(caseFlagDetails, caseFlagType)) {
            caseFlagDetails = caseFlagDetails.stream().map(detail -> {
                CaseFlagValue value = detail.getCaseFlagValue();
                if (isActiveTargetCaseFlag(value, caseFlagType)) {
                    return new CaseFlagDetail(detail.getId(), CaseFlagValue.builder()
                            .flagCode(value.getFlagCode())
                            .name(value.getName())
                            .status("Inactive")
                            .dateTimeModified(systemDateProvider.nowWithTime().toString())
                            .hearingRelevant(value.getHearingRelevant())
                            .build());
                } else {
                    return detail;
                }
            }).collect(Collectors.toList());
        }

        return caseFlagDetails;
    }

    private boolean hasActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
                .stream()
                .anyMatch(flagDetail -> isActiveTargetCaseFlag(flagDetail.getCaseFlagValue(), caseFlagType));
    }

    private boolean isActiveTargetCaseFlag(CaseFlagValue value, StrategicCaseFlagType targetCaseFlagType) {
        return Objects.equals(value.getFlagCode(), targetCaseFlagType.getFlagCode())
                && Objects.equals(value.getStatus(), "Active");
    }

    private String getAppellantDisplayName(Optional<StrategicCaseFlag> existingCaseFlags, AsylumCase asylumCase) {

        return existingCaseFlags.isPresent()
                ? existingCaseFlags.get().getPartyName()
                : asylumCase
                .read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
                    final String appellantGivenNames =
                            asylumCase
                                    .read(APPELLANT_GIVEN_NAMES, String.class).orElse(null);
                    final String appellantFamilyName =
                            asylumCase
                                    .read(APPELLANT_FAMILY_NAME, String.class).orElse(null);
                    return !(appellantGivenNames == null || appellantFamilyName == null)
                            ? appellantGivenNames + " " + appellantFamilyName
                            : null;
                });
    }
}
