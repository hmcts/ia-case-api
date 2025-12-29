package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class NationalityFieldValueTest {

    private NationalityFieldValue nationalityFieldValue;

    @Test
    void has_correct_values() {
        nationalityFieldValue = new NationalityFieldValue("ZZ");
        assertEquals("ZZ", nationalityFieldValue.getCode());
    }
}

