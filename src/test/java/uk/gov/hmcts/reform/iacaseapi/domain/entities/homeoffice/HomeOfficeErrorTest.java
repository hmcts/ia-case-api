package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class HomeOfficeErrorTest {

    private HomeOfficeError error;

    @BeforeEach
    public void setUp() {
        error = new HomeOfficeError("1100", "some-error", false);
    }

    @Test
    void has_correct_values_after_setting() {
        assertNotNull(error);
        assertEquals("1100", error.getErrorCode());
        assertEquals("some-error", error.getMessageText());
        assertFalse(error.isSuccess());
    }
}
