package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;

public class WitnessCaseFlagsHandler extends AppellantCaseFlagsHandler {

    protected DateProvider systemDateProvider;

    protected List<CaseFlagDetail> getWitnessCaseFlags(Optional<List<PartyFlagIdValue>> optionalPartyFlags, String witnessId) {
        List<CaseFlagDetail> witnessCaseFlags = new ArrayList<>();
        if (optionalPartyFlags.isPresent()) {
            Optional<PartyFlagIdValue> witnessPartyValue = optionalPartyFlags.get()
                    .stream()
                    .filter(partyFlagIdValue -> partyFlagIdValue.getPartyId().equals(witnessId))
                    .findFirst();

            if (witnessPartyValue.isPresent()) {
                witnessCaseFlags = witnessPartyValue.get().getValue().getDetails();
            }
        }
        return witnessCaseFlags;
    }

    protected List<CaseFlagDetail> activateCaseFlag(
            AsylumCase asylumCase,
            List<CaseFlagDetail> existingCaseFlagDetails,
            StrategicCaseFlagType caseFlagType,
            String flagName,
            String dateTimeCreated) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .flagCode(caseFlagType.getFlagCode())
                .name(flagName)
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

    protected List<CaseFlagDetail> deactivateCaseFlag(
            List<CaseFlagDetail> caseFlagDetails,
            StrategicCaseFlagType caseFlagType,
            String dateTimeModified) {
        if (hasActiveTargetCaseFlag(caseFlagDetails, caseFlagType)) {
            caseFlagDetails = caseFlagDetails.stream().map(detail -> {
                CaseFlagValue value = detail.getCaseFlagValue();
                if (isActiveTargetCaseFlag(value, caseFlagType)) {
                    return new CaseFlagDetail(detail.getId(), CaseFlagValue.builder()
                            .flagCode(value.getFlagCode())
                            .name(value.getName())
                            .status("Inactive")
                            .dateTimeModified(dateTimeModified)
                            .dateTimeCreated(value.getDateTimeCreated())
                            .hearingRelevant(value.getHearingRelevant())
                            .build());
                } else {
                    return detail;
                }
            }).collect(Collectors.toList());
        }

        return caseFlagDetails;
    }

    protected Optional<CaseFlagDetail> getActiveTargetCaseFlag(List<CaseFlagDetail> caseFlagDetails, StrategicCaseFlagType caseFlagType) {
        return caseFlagDetails
                .stream()
                .filter(caseFlagDetail -> isActiveTargetCaseFlag(caseFlagDetail.getCaseFlagValue(), caseFlagType))
                .findFirst();
    }

}
