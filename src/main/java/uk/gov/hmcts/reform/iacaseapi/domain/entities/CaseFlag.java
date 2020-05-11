package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CaseFlag {

    private CaseFlagType caseFlagType;
    private String caseFlagAdditionalInformation;

    private CaseFlag() {

    }

    public CaseFlag(CaseFlagType caseFlagType, String caseFlagAdditionalInformation) {
        requireNonNull(caseFlagType);
        requireNonNull(caseFlagAdditionalInformation);
        this.caseFlagType = caseFlagType;
        this.caseFlagAdditionalInformation = caseFlagAdditionalInformation;
    }

    public CaseFlagType getCaseFlagType() {
        return caseFlagType;
    }

    public String getCaseFlagAdditionalInformation() {
        return caseFlagAdditionalInformation;
    }
}
