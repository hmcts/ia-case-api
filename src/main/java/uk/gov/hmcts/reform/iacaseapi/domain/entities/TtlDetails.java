package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Data
@Builder
public class TtlDetails {
    private final String systemSetTtl;
    private final String manualTtlOverride;
    private YesOrNo doNotDelete;

    @JsonCreator
    public TtlDetails(
        @JsonProperty("system_set_ttl") String systemSetTtl,
        @JsonProperty("manual_ttl_override") String manualTtlOverride,
        @JsonProperty("do_not_delete") YesOrNo doNotDelete
    ) {
        this.systemSetTtl = systemSetTtl;
        this.manualTtlOverride = manualTtlOverride;
        this.doNotDelete = doNotDelete;
    }
}
