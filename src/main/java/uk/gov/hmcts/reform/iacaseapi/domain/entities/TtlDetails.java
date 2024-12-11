package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Data
@Builder
public class TtlDetails {
    private final String systemTtl;
    private final String overrideTTL;
    private YesOrNo isSuspended;

    @JsonCreator
    public TtlDetails(
        @JsonProperty("SystemTTL") String systemTtl,
        @JsonProperty("OverrideTTL") String overrideTTL,
        @JsonProperty("Suspended") YesOrNo isSuspended
    ) {
        this.systemTtl = systemTtl;
        this.overrideTTL = overrideTTL;
        this.isSuspended = isSuspended;
    }
}
