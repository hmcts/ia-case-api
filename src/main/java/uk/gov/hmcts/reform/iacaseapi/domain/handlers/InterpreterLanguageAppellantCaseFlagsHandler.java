package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

@Component
public class InterpreterLanguageAppellantCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public InterpreterLanguageAppellantCaseFlagsHandler(DateProvider systemDateProvider) {
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
        Optional<CaseDetails<AsylumCase>> asylumCaseBefore = callback.getCaseDetailsBefore();
        Optional<StrategicCaseFlag> existingCaseflags = asylumCase
                .read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class);
        String appellantDisplayName = getAppellantDisplayName(existingCaseflags, asylumCase);

        boolean isInterpreterServicesNeeded = asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
                .map(interpreterNeeded -> YesOrNo.YES == interpreterNeeded).orElse(false);

        Optional<InterpreterLanguageRefData> appellantSpokenLanguage = asylumCase
                .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class);

        List<CaseFlagDetail> existingCaseFlagDetails = existingCaseflags
                .map(StrategicCaseFlag::getDetails).orElse(Collections.emptyList());

        boolean caseDataUpdated = false;

        Optional<CaseFlagDetail> activeFlag = getActiveTargetCaseFlag(existingCaseFlagDetails, INTERPRETER_LANGUAGE_FLAG);

        if (isInterpreterServicesNeeded) {
            if (appellantSpokenLanguage.isPresent()) {
                String chosenLanguage = getChosenSpokenLanguage(appellantSpokenLanguage.get());
                if (!activeFlag.isPresent()) {
                    existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, INTERPRETER_LANGUAGE_FLAG, chosenLanguage);
                    caseDataUpdated = true;
                } else if (asylumCaseBefore.isPresent() &&
                        selectedLanguageDiffers(appellantSpokenLanguage.get(), asylumCaseBefore.get().getCaseData())) {
                    existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, INTERPRETER_LANGUAGE_FLAG);
                    existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, INTERPRETER_LANGUAGE_FLAG, chosenLanguage);
                    caseDataUpdated = true;
                }
            }
        } else {
            if (activeFlag.isPresent()) {
                existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails, INTERPRETER_LANGUAGE_FLAG);
                caseDataUpdated = true;
            }
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

    private String getChosenSpokenLanguage(InterpreterLanguageRefData appellantSpokenLanguage) {
        if (appellantSpokenLanguage.getLanguageManualEntry().isEmpty()){
            return appellantSpokenLanguage.getLanguageRefData().getValue().getLabel();
        }
        return appellantSpokenLanguage.getLanguageManualEntryDescription();
    }

    private boolean selectedLanguageDiffers(InterpreterLanguageRefData appellantSpokenLanguage, AsylumCase asylumCaseBefore) {
        Optional<InterpreterLanguageRefData> appellantSpokenLanguageBefore = asylumCaseBefore
                .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class);

        return !appellantSpokenLanguage.equals(appellantSpokenLanguageBefore.get());
    }

    private List<CaseFlagDetail> activateCaseFlag(
            AsylumCase asylumCase,
            List<CaseFlagDetail> existingCaseFlagDetails,
            StrategicCaseFlagType caseFlagType,
            String language
    ) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .flagCode(caseFlagType.getFlagCode())
                .name(caseFlagType.getName().concat(" " + language))
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

    private Optional<CaseFlagDetail> getActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
                .stream()
                .filter(caseFlagDetail -> isActiveTargetCaseFlag(caseFlagDetail.getCaseFlagValue(), caseFlagType))
                .findFirst();
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


