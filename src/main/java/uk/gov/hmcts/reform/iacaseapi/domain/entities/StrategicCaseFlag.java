package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    String partyName;
    String roleOnCase;

    @JsonProperty("details")
    List<CaseFlagDetail> details;

    public StrategicCaseFlag(String partyFullName, String roleOnCase) {
        this.partyName = partyFullName;
        this.roleOnCase = roleOnCase;
        this.details = Collections.emptyList();
    }

    public StrategicCaseFlag(String partyFullName, String roleOnCase, List<CaseFlagDetail> details) {
        this.partyName = partyFullName;
        this.roleOnCase = roleOnCase;
        this.details = details == null ? Collections.emptyList() : details;
    }

    public StrategicCaseFlag() {
        this.details = Collections.emptyList();
        this.partyName = null;
        this.roleOnCase = null;
    }
}
