package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatesToAvoidTest {

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
    public void should_hold_onto_values() {
        Assert.assertEquals(dateToAvoid, datesToAvoid.getDateToAvoid());
        Assert.assertEquals(dateToAvoidReason, datesToAvoid.getDateToAvoidReason());
    }
}
