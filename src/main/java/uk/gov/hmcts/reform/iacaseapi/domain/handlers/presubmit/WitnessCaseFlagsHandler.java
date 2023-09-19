package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;

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

    protected void deactivateAnyActiveCaseFlags(Optional<List<PartyFlagIdValue>> existingCaseFlags, AsylumCase asylumCase, StrategicCaseFlagType flag) {
        boolean updated = false;
        if (existingCaseFlags.isPresent()) {
            for (int i = 0; i < existingCaseFlags.get().size(); i++) {
                PartyFlagIdValue partyFlag = existingCaseFlags.get().get(i);
                Optional<CaseFlagDetail> activeFlag = getActiveTargetCaseFlag(partyFlag.getValue().getDetails(), flag);
                if (activeFlag.isPresent()) {
                    List<CaseFlagDetail> flags = deactivateCaseFlag(partyFlag.getValue().getDetails(), flag);
                    StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                            partyFlag.getValue().getPartyName(), StrategicCaseFlag.ROLE_ON_CASE_WITNESS, flags);
                    existingCaseFlags.get().set(i, new PartyFlagIdValue(partyFlag.getPartyId(), caseFlag));
                    updated = true;
                }
            }
        }
        if (updated) {
            asylumCase.write(WITNESS_LEVEL_FLAGS, existingCaseFlags);
        }
    }

    protected List<CaseFlagDetail> activateCaseFlag(
            AsylumCase asylumCase,
            List<CaseFlagDetail> existingCaseFlagDetails,
            StrategicCaseFlagType caseFlagType,
            String flagName) {

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .flagCode(caseFlagType.getFlagCode())
                .name(flagName)
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

    protected List<CaseFlagDetail> deactivateCaseFlag(
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
