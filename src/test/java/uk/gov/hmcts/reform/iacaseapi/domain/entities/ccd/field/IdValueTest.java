package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IdValueTest {

    final String id = "1";
    final Integer value = 1234;

    IdValue<Integer> addressUk = new IdValue<>(id, value);

    @Test
    void should_hold_onto_values() {
        assertEquals(id, addressUk.getId());
        assertEquals(value, addressUk.getValue());
    }

}
