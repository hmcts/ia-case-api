package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PersonTest {
    @Mock
    CodeWithDescription mockCode;

    private Person person;

    @Before
    public void setUp() {
        person = new Person(
            mockCode,
            mockCode,
            "firstName",
            "surName",
            "firstName-surName",
            20, 10, 1980
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(person);
        assertNotNull(person.getNationality());
        assertNotNull(person.getGender());
        assertEquals(mockCode, person.getNationality());
        assertEquals(mockCode, person.getGender());
        assertEquals("firstName", person.getGivenName());
        assertEquals("surName", person.getFamilyName());
        assertEquals("firstName-surName", person.getFullName());
        assertEquals(20, person.getDayOfBirth());
        assertEquals(10, person.getMonthOfBirth());
        assertEquals(1980, person.getYearOfBirth());
    }

}
