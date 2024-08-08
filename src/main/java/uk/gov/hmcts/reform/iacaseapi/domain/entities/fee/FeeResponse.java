package uk.gov.hmcts.reform.iacaseapi.domain.entities.fee;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class FeeResponse {

    private String code;
    private String description;
    private String version;

    @JsonProperty(value = "fee_amount")
    private BigDecimal amount;

    private FeeResponse() {

    }

    public FeeResponse(String code, String description, String version, BigDecimal amount) {

        this.code = code;
        this.description = description;
        this.version = version;
        this.amount = amount;
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public String getVersion() {
        requireNonNull(version);
        return version;
    }

    public BigDecimal getAmount() {
        requireNonNull(amount);
        return amount;
    }
}
