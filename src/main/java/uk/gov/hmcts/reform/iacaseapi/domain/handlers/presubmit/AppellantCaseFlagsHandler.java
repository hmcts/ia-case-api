package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
            .flagCode(caseFlagType.getFlagCode())
            .name(caseFlagType.getName())
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

        return caseFlagDetails.stream()
            .map(detail -> tryDeactivateCaseFlag(caseFlagType, dateTimeModified, detail))
            .collect(Collectors.toList());
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
                .name(value.getName())
                .status("Inactive")
                .hearingRelevant(value.getHearingRelevant())
                .dateTimeModified(dateTimeModified)
                .build())
            : detail;
    }

}
