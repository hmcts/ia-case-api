package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class IdValueTest {

    private final String id = "1";
    private final Integer value = 1234;

    private IdValue<Integer> addressUk = new IdValue<>(id, value);

    @Test
    public void should_hold_onto_values() {
        assertEquals(id, addressUk.getId());
        assertEquals(value, addressUk.getValue());
    }

}
