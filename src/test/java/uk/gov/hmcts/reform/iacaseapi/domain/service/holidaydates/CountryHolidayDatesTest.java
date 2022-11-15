package uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

public class CountryHolidayDatesTest {

    private final Class classToTest = CountryHolidayDates.class;

    @Test
    void isWellImplemented() {
        assertPojoMethodsFor(classToTest)
            .testing(Method.GETTER)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }
}
