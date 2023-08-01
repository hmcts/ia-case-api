package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LOOP_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_ROOM_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
class HearingRequirementsAppellantCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

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
        Optional<StrategicCaseFlag> existingCaseflags = asylumCase
                .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class);
        String appellantDisplayName = getAppellantDisplayName(existingCaseflags, asylumCase);
        boolean isHearingLoopNeeded = asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)
            .map(hearingLoopNeeded -> YesOrNo.YES == hearingLoopNeeded).orElse(false);
        boolean isHearingRoomNeeded = asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)
            .map(hearingRoomNeeded -> YesOrNo.YES == hearingRoomNeeded).orElse(false);
        List<CaseFlagDetail> existingCaseFlagDetails = existingCaseflags
            .map(StrategicCaseFlag::getDetails).orElse(Collections.emptyList());

        boolean caseDataUpdated = false;

        if (isHearingLoopNeeded && !hasActiveTargetCaseFlag(existingCaseFlagDetails, HEARING_LOOP)) {
            existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, HEARING_LOOP);
            caseDataUpdated = true;
        }
        if (isHearingRoomNeeded && !hasActiveTargetCaseFlag(existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS)) {
            existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS);
            caseDataUpdated = true;
        }
        if (!isHearingLoopNeeded && hasActiveTargetCaseFlag(existingCaseFlagDetails, HEARING_LOOP)) {
            existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, HEARING_LOOP);
            caseDataUpdated = true;
        }
        if (!isHearingRoomNeeded && hasActiveTargetCaseFlag(existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS)) {
            existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, STEP_FREE_WHEELCHAIR_ACCESS);
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
                        .hearingRelevant(value.getHearingRelevant())
                        .dateTimeModified(systemDateProvider.nowWithTime().toString())
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
