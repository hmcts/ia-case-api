package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;

@ToString
@Data

public class TTL {

    @JsonProperty("Suspended")
    private YesOrNo suspended;
    @JsonProperty("SystemTTL")
    private String systemTTL;
    @JsonProperty("OverrideTTL")
    private String overrideTTL;

    private TTL() {
    }

    public TTL(
        YesOrNo suspended,
        String systemTTL,
        String overrideTTL
    ) {
        this.setSuspended(requireNonNull(suspended));
        this.setSystemTTL(requireNonNull(systemTTL));
        this.setOverrideTTL(requireNonNull(overrideTTL));

    }

}
