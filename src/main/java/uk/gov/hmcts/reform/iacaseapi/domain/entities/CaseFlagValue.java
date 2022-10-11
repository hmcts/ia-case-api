package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Value
@Builder
public class CaseFlagValue {
    private String name;
    private String status;
    private String flagCode;
    private String dateTimeCreated;
    private YesOrNo hearingRelevant;
    @JsonProperty("path")
    private List<CaseFlagPath> caseFlagPath;
}
