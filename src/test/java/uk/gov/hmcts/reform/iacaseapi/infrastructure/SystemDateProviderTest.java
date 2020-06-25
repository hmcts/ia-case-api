package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;

public class SystemDateProviderTest {

    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    public void returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        assertNotNull(actualDate);
        assertFalse(actualDate.isAfter(LocalDate.now()));
    }

    @Test
    public void returns_now_datetime() {
        LocalDateTime actualDateTime = systemDateProvider.nowWithTime();
        assertNotNull(actualDateTime);
        assertFalse(actualDateTime.isAfter(LocalDateTime.now()));
    }
}
