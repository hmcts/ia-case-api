package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;

@Data
@Builder
public class TtlDetails {

    private final LocalDate systemSetTtl;
    private final LocalDate manualTtlOverride;
    private YesOrNo doNotDelete;

    @JsonCreator
    public TtlDetails(@JsonProperty("system_set_ttl") LocalDate systemSetTtl,
                      @JsonProperty("manual_ttl_override") LocalDate manualTtlOverride,
                      @JsonProperty("do_not_delete") YesOrNo doNotDelete) {
        this.systemSetTtl = systemSetTtl;
        this.manualTtlOverride = manualTtlOverride;
        this.doNotDelete = doNotDelete;
    }
}
