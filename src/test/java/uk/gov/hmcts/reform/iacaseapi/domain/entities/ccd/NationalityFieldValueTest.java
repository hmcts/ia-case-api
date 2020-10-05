package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NationalityFieldValueTest {

    private NationalityFieldValue nationalityFieldValue;

    @Test
    public void has_correct_values() {

        nationalityFieldValue = new NationalityFieldValue("ZZ");
        assertEquals("ZZ", nationalityFieldValue.getCode());
    }


}
