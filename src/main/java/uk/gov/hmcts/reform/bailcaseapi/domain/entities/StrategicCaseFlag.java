package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Value
@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    public static final String ROLE_ON_CASE_APPLICANT = "Applicant";
    public static final String ROLE_ON_CASE_FCS = "FCS";

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
        this.details = details;
    }

    public StrategicCaseFlag() {
        this.details = Collections.emptyList();
        this.partyName = null;
        this.roleOnCase = null;
    }
}
