package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeErrorTest {

    private HomeOfficeError error;

    @Before
    public void setUp() {
        error = new HomeOfficeError("1100", "some-error", false);
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(error);
        assertEquals("1100", error.getErrorCode());
        assertEquals("some-error", error.getMessageText());
        assertFalse(error.isSuccess());
    }
}
