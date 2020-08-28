package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckValuesTest {

    private final List<String> values = new ArrayList<>();

    private CheckValues<String> checkValues = new CheckValues<>(values);

    @Test
    void should_hold_onto_values() {

        assertEquals(values, checkValues.getValues());
    }
}
