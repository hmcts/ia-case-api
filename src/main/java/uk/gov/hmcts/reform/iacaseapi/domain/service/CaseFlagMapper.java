package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.Flag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.FlagDetail;

public class CaseFlagMapper {

    public static final String CASE_FLAG = "Case";
    public static final String PARTY_FLAG = "Party";

    private CaseFlagMapper() {
        throw new AssertionError("Instantiating utility class.");
    }

    public static StrategicCaseFlag buildStrategicCaseFlagDetail(Flag dtoFlags, StrategicCaseFlagType strategicCaseFlagType,
                                                                 String caseLevel, String appellantName) {

        List<FlagDetail> dtoLevelFlags = dtoFlags.getFlagDetails().stream()
            .flatMap(dt1 -> dt1.getChildFlags().stream()).collect(Collectors.toList());

        FlagDetail dtoFlagDetail = dtoLevelFlags.stream().filter(f -> f.getFlagCode().equals(strategicCaseFlagType.getFlagCode())).findAny()
            .orElseThrow(() -> new IllegalArgumentException("Couldn't find Strategic flag type from REF DATA === " + strategicCaseFlagType));

        List<CaseFlagPath> listOfPath = dtoFlagDetail.getPath().stream().map(p -> new CaseFlagPath(null, p)).collect(Collectors.toList());

        CaseFlagValue newStrategicFlagValue = CaseFlagValue.builder()
            .name(dtoFlagDetail.getName())
            .status("Active")
            .flagCode(dtoFlagDetail.getFlagCode())
            .dateTimeCreated(LocalDateTime.now().toString())
            .hearingRelevant(dtoFlagDetail.getHearingRelevant() ? YesOrNo.YES : YesOrNo.NO)
            .caseFlagPath(listOfPath)
            .build();

        List<CaseFlagDetail> details = new ArrayList<>();

        CaseFlagDetail addedCaseFlag = new CaseFlagDetail(null, newStrategicFlagValue);

        details.add(addedCaseFlag);

        if (caseLevel.equals(CASE_FLAG)) {
            return StrategicCaseFlag.builder().details(details).build();
        } else {
            return StrategicCaseFlag.builder().details(details)
                .roleOnCase("Appellant")
                .partyName(appellantName)
                .build();
        }
    }
}
