package uk.gov.hmcts.reform.iacaseapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeeDtoTest {

    @Mock FeeResponse feeResponse;

    private BigDecimal calculatedAmount = new BigDecimal("140.00");
    private String description = "Appeal determined with a hearing";
    private Integer version = 1;
    private String code = "FEE0123";

    private FeeDto feeDto;

    @Before
    public void setUp() {

        feeDto = new FeeDto(calculatedAmount, description, version, code);
    }

    @Test
    public void should_hold_onto_values() {

        assertThat(feeDto.getCalculatedAmount()).isEqualTo(calculatedAmount);
        assertThat(feeDto.getDescription()).isEqualTo(description);
        assertThat(feeDto.getVersion()).isEqualTo(version);
        assertThat(feeDto.getCode()).isEqualTo(code);
    }

    @Test
    public void should_not_allow_null_values() {

        assertThatThrownBy(() -> new FeeDto(null, description, version, code))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new FeeDto(calculatedAmount, null, version, code))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new FeeDto(calculatedAmount, description, null, code))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new FeeDto(calculatedAmount, description, version, null))
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
