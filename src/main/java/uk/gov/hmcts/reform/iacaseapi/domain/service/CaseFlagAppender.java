package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class CaseFlagAppender {

    public List<IdValue<CaseFlag>> append(
        List<IdValue<CaseFlag>> existingCaseFlags,
        CaseFlagType caseFlagType,
        String caseFlagAdditionalInformation
    ) {
        requireNonNull(existingCaseFlags, "existingCaseFlags must not be null");
        requireNonNull(caseFlagType, "caseFlagType must not be null");
        requireNonNull(caseFlagAdditionalInformation, "caseFlagAdditionalInformation must not be null");

        final CaseFlag newCaseFlag = new CaseFlag(caseFlagType, caseFlagAdditionalInformation);

        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();

        int index = existingCaseFlags.size() + 1;
        boolean addedNewCaseFlag = false;

        for (IdValue<CaseFlag> existingCaseFlag : existingCaseFlags) {
            if (existingCaseFlag.getValue().getCaseFlagType() == caseFlagType) {
                allCaseFlags.add(new IdValue<>(String.valueOf(index--), newCaseFlag));
                addedNewCaseFlag = true;
            } else {
                allCaseFlags.add(new IdValue<>(String.valueOf(index--), existingCaseFlag.getValue()));
            }
        }

        if (!addedNewCaseFlag) {
            allCaseFlags.add(new IdValue<>(String.valueOf(index), newCaseFlag));
        }

        return allCaseFlags;
    }
}
