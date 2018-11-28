package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CheckValuesTest {

    private final List<String> values = new ArrayList<>();

    private CheckValues<String> checkValues = new CheckValues<>(values);

    @Test
    public void should_hold_onto_values() {

        assertEquals(values, checkValues.getValues());
    }
}
