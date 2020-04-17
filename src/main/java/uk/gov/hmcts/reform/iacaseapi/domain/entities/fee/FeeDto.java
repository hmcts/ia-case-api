package uk.gov.hmcts.reform.iacaseapi.domain.entities.fee;

import static java.util.Objects.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public class FeeDto {

    @JsonProperty("calculated_amount")
    private BigDecimal calculatedAmount;
    private String description;
    private Integer version;
    private String code;

    public FeeDto(BigDecimal calculatedAmount, String description, Integer version, String code) {
        requireNonNull(calculatedAmount);
        requireNonNull(description);
        requireNonNull(version);
        requireNonNull(code);

        this.calculatedAmount = calculatedAmount;
        this.description = description;
        this.version = version;
        this.code = code;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public String getDescription() {
        return description;
    }

    public Integer getVersion() {
        return version;
    }

    public String getCode() {
        return code;
    }
}
