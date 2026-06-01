package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatesToAvoidTest {

    private final LocalDate dateToAvoid = LocalDate.parse("2019-11-29");
    private final String dateToAvoidReason = "Some Reason";
    private DatesToAvoid datesToAvoid;

    public DatesToAvoidTest() {
    }

    @BeforeEach
    public void setUp() {
        datesToAvoid = new DatesToAvoid();
        datesToAvoid.setDateToAvoid(dateToAvoid);
        datesToAvoid.setDateToAvoidReason(dateToAvoidReason);
    }

    @Test
    void should_hold_onto_values() {
        Assertions.assertEquals(dateToAvoid, datesToAvoid.getDateToAvoid());
        Assertions.assertEquals(dateToAvoidReason, datesToAvoid.getDateToAvoidReason());
    }
}
