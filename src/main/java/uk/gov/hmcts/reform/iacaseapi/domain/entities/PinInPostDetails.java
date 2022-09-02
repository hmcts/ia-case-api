package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Data
@Builder
public class PinInPostDetails {

    private final String accessCode;

    private final String expiryDate;
    private YesOrNo pinUsed;

    @JsonCreator
    public PinInPostDetails(@JsonProperty("accessCode") String accessCode,
                            @JsonProperty("expiryDate") String expiryDate,
                            @JsonProperty("pinUsed") YesOrNo pinUsed) {
        this.accessCode = accessCode;
        this.expiryDate = expiryDate;
        this.pinUsed = pinUsed;
    }
}
