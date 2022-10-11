package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class StrategicCaseFlag {
    String partyName;
    String roleOnCase;

    @JsonProperty("details")
    List<CaseFlagDetail> details;
}
