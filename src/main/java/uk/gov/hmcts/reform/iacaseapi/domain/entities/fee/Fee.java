package uk.gov.hmcts.reform.iacaseapi.domain.entities.fee;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Fee {

    private BigDecimal calculatedAmount;
    private String description;
    private String version;
    private String code;

    private Fee() {

    }

    public Fee(String code, String description, String version, BigDecimal calculatedAmount) {

        this.calculatedAmount = calculatedAmount;
        this.description = description;
        this.version = version;
        this.code = code;
    }

    public BigDecimal getCalculatedAmount() {
        requireNonNull(calculatedAmount);
        return calculatedAmount;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public String getVersion() {
        requireNonNull(version);
        return version;
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

    @JsonIgnore
    public String getAmountAsString() {

        return new DecimalFormat("#0.##").format(getCalculatedAmount());
    }

    @Override
    public String toString() {
        return "Fee{ calculatedAmount=" + calculatedAmount
            + ", description='" + description + '\''
            + ", version=" + version
            + ", code='" + code + '\'' + '}';
    }
}
