package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class SignLanguageForAppellantCaseFlagsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public SignLanguageForAppellantCaseFlagsHandler(DateProvider systemDateProvider) {
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

        Optional<InterpreterLanguageRefData> appellantSignLanguage = asylumCase
                .read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class);

        boolean isSignServicesNeeded = appellantSignLanguage.isPresent();

        List<CaseFlagDetail> existingCaseFlagDetails = existingCaseflags
                .map(StrategicCaseFlag::getDetails).orElse(Collections.emptyList());

        boolean caseDataUpdated = false;

        Optional<CaseFlagDetail> activeFlag = getActiveTargetCaseFlag(existingCaseFlagDetails);

        if (isSignServicesNeeded) {
            String chosenLanguage = getChosenSignLanguage(appellantSignLanguage.get());
            if (activeFlag.isEmpty()) {
                existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, chosenLanguage);
                caseDataUpdated = true;
            } else if (asylumCaseBefore.isPresent() &&
                selectedLanguageDiffers(appellantSignLanguage.get(), asylumCaseBefore.get().getCaseData())) {
                existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails);
                existingCaseFlagDetails = activateCaseFlag(asylumCase, existingCaseFlagDetails, chosenLanguage);
                caseDataUpdated = true;
            }
        } else {
            if (activeFlag.isPresent()) {
                existingCaseFlagDetails = deactivateCaseFlag(existingCaseFlagDetails);
                caseDataUpdated = true;
            }
        }

        if (caseDataUpdated) {
            String appellantDisplayName = getAppellantDisplayName(existingCaseflags, asylumCase);

            if (appellantDisplayName == null) {
                throw new IllegalStateException("Appellant full name is not present");
            }
            asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(
                    appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingCaseFlagDetails));
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getChosenSignLanguage(InterpreterLanguageRefData appellantSignLanguage) {
        if (appellantSignLanguage.getLanguageManualEntry() == null || appellantSignLanguage.getLanguageManualEntry().isEmpty()) {
            return appellantSignLanguage.getLanguageRefData().getValue().getLabel();
        }
        return appellantSignLanguage.getLanguageManualEntryDescription();
    }

    private boolean selectedLanguageDiffers(InterpreterLanguageRefData appellantSignLanguage, AsylumCase asylumCaseBefore) {
        Optional<InterpreterLanguageRefData> appellantSpokenLanguageBefore = asylumCaseBefore
                .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class);

        return !Objects.equals(appellantSignLanguage, appellantSpokenLanguageBefore.orElse(null));
    }

    private List<CaseFlagDetail> activateCaseFlag(
            AsylumCase asylumCase,
            List<CaseFlagDetail> existingCaseFlagDetails,
            String chosenLanguage
    ) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .flagCode(StrategicCaseFlagType.SIGN_LANGUAGE.getFlagCode())
                .name(StrategicCaseFlagType.SIGN_LANGUAGE.getName().concat(" " + chosenLanguage))
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
            List<CaseFlagDetail> caseFlagDetails) {
        if (hasActiveTargetCaseFlag(caseFlagDetails)) {
            caseFlagDetails = caseFlagDetails.stream().map(detail -> {
                CaseFlagValue value = detail.getCaseFlagValue();
                if (isActiveTargetCaseFlag(value)) {
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

    private boolean hasActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails) {
        return caseFlagDetails
                .stream()
                .anyMatch(flagDetail ->
                    isActiveTargetCaseFlag(flagDetail.getCaseFlagValue()));
    }

    private Optional<CaseFlagDetail> getActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails) {
        return caseFlagDetails
                .stream()
                .filter(caseFlagDetail ->
                    isActiveTargetCaseFlag(caseFlagDetail.getCaseFlagValue()))
                .findFirst();
    }

    private boolean isActiveTargetCaseFlag(CaseFlagValue value) {
        return Objects.equals(value.getFlagCode(), StrategicCaseFlagType.SIGN_LANGUAGE.getFlagCode())
                && Objects.equals(value.getStatus(), "Active");
    }

    private String getAppellantDisplayName(Optional<StrategicCaseFlag> existingCaseFlags, AsylumCase asylumCase) {

        return existingCaseFlags.isPresent() ? existingCaseFlags.get().getPartyName()
                : asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
                    final String appellantGivenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(null);
                    final String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(null);
                    return !(appellantGivenNames == null || appellantFamilyName == null) ?
                            appellantGivenNames + " " + appellantFamilyName : null;
                });
    }
}
