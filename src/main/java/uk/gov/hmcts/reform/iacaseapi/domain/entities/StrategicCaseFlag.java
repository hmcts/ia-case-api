package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;

@Value
@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    String partyName;
    String roleOnCase;

    @JsonProperty("details")
    List<CaseFlagDetail> details;

    public StrategicCaseFlag(String appellantNameForDisplay) {
        this.partyName = appellantNameForDisplay;
        this.roleOnCase = "Appellant";
        this.details = Collections.emptyList();
    }

    public StrategicCaseFlag() {
        this.details = Collections.emptyList();
        this.partyName = null;
        this.roleOnCase = null;
    }
}
