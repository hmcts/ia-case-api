package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;

import java.util.*;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

class AppellantCaseFlagsHandler  {

    protected List<CaseFlagDetail> activateCaseFlag(
        AsylumCase asylumCase,
        List<CaseFlagDetail> existingCaseFlagDetails,
        StrategicCaseFlagType caseFlagType,
        String dateTimeCreated) {
        return activateCaseFlag(asylumCase, existingCaseFlagDetails, caseFlagType,
            dateTimeCreated, caseFlagType.getName(), null);
    }

    protected List<CaseFlagDetail> activateCaseFlag(
        AsylumCase asylumCase,
        List<CaseFlagDetail> existingCaseFlagDetails,
        StrategicCaseFlagType caseFlagType,
        String dateTimeCreated,
        String languageName,
        String languageCode) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
            .flagCode(caseFlagType.getFlagCode())
            .subTypeKey(languageCode)
            .subTypeValue(languageName)
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .status("Active")
            .hearingRelevant(YesOrNo.YES)
            .dateTimeCreated(dateTimeCreated)
            .build();
        String caseFlagId = asylumCase.read(CASE_FLAG_ID, String.class).orElse(UUID.randomUUID().toString());
        List<CaseFlagDetail> caseFlagDetails = existingCaseFlagDetails.isEmpty()
            ? new ArrayList<>()
            : new ArrayList<>(existingCaseFlagDetails);
        caseFlagDetails.add(new CaseFlagDetail(caseFlagId, caseFlagValue));

        return caseFlagDetails;
    }

    protected List<CaseFlagDetail> deactivateCaseFlags(
        List<CaseFlagDetail> caseFlagDetails,
        StrategicCaseFlagType caseFlagType,
        String dateTimeModified) {

        if (hasActiveTargetCaseFlag(caseFlagDetails, caseFlagType)) {
            return caseFlagDetails.stream()
                .map(detail -> tryDeactivateCaseFlag(caseFlagType, dateTimeModified, detail))
                .collect(Collectors.toList());
        } else {
            return caseFlagDetails;
        }
    }

    protected boolean hasActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
            .stream()
            .anyMatch(flagDetail -> isActiveTargetCaseFlag(flagDetail.getCaseFlagValue(), caseFlagType));
    }

    protected boolean isActiveTargetCaseFlag(CaseFlagValue value, StrategicCaseFlagType targetCaseFlagType) {
        return Objects.equals(value.getFlagCode(), targetCaseFlagType.getFlagCode())
            && Objects.equals(value.getStatus(), "Active");
    }

    protected StrategicCaseFlag getOrCreateAppellantCaseFlags(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .orElseGet(() -> new StrategicCaseFlag(
                HandlerUtils.getAppellantFullName(asylumCase),
                StrategicCaseFlag.ROLE_ON_CASE_APPELLANT));
    }

    private CaseFlagDetail tryDeactivateCaseFlag(
        StrategicCaseFlagType caseFlagType,String dateTimeModified, CaseFlagDetail detail) {
        CaseFlagValue value = detail.getCaseFlagValue();

        return isActiveTargetCaseFlag(value, caseFlagType)
            ? new CaseFlagDetail(detail.getId(), CaseFlagValue.builder()
                .flagCode(value.getFlagCode())
                .subTypeKey(value.getSubTypeKey())
                .subTypeValue(value.getSubTypeValue())
                .name(value.getName())
                .status("Inactive")
                .hearingRelevant(value.getHearingRelevant())
                .dateTimeModified(dateTimeModified)
                .dateTimeCreated(value.getDateTimeCreated())
                .build())
            : detail;
    }

    protected Optional<CaseFlagDetail> getActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
            .stream()
            .filter(caseFlagDetail -> isActiveTargetCaseFlag(caseFlagDetail.getCaseFlagValue(), caseFlagType))
            .findFirst();
    }

}
