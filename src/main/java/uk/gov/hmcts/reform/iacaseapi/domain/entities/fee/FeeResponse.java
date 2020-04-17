package uk.gov.hmcts.reform.iacaseapi.domain.entities.fee;

import static java.util.Objects.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeResponse {

    private String code;
    private String description;
    private Integer version;

    @JsonProperty(value = "fee_amount")
    private BigDecimal amount;

    public FeeResponse(String code, String description, Integer version, BigDecimal amount) {
        requireNonNull(code);
        requireNonNull(description);
        requireNonNull(version);
        requireNonNull(amount);

        this.code = code;
        this.description = description;
        this.version = version;
        this.amount = amount;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Integer getVersion() {
        return version;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
