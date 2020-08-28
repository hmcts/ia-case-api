package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SystemDateProviderTest {

    final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    void returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        assertNotNull(actualDate);
        assertFalse(actualDate.isAfter(LocalDate.now()));
    }

    @Test
    void returns_now_datetime() {
        LocalDateTime actualDateTime = systemDateProvider.nowWithTime();
        assertNotNull(actualDateTime);
        assertFalse(actualDateTime.isAfter(LocalDateTime.now()));
    }
}
