package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LocalAuthority {

    @JsonProperty("localAuthorityCode")
    private String localAuthorityCode;

    @JsonProperty("checklistNoLocalAuthority")
    private List<String> checklistNoLocalAuthority;

    @JsonProperty("localAuthorityManual")
    private LocalAuthorityManual localAuthorityManual;

}
