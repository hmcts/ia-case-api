package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PartyFlagIdValueTest {

    private final String partyId = "party-id";
    private final StrategicCaseFlag flag = mock(StrategicCaseFlag.class);
    private PartyFlagIdValue partyFlagIdValue;

    @BeforeEach
    public void setUp() {
        partyFlagIdValue = new PartyFlagIdValue(
            partyId,
            flag);
    }

    @Test
    void should_hold_onto_values() {

        assertThat(partyFlagIdValue.getPartyId()).isEqualTo(partyId);
        assertThat(partyFlagIdValue.getValue()).isEqualTo(flag);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new PartyFlagIdValue(null, flag))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PartyFlagIdValue("", null))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
