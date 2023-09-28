package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InterpreterDetailsTest {

    @Test
    void test_buildInterpreterFullName() {
        String givenName = "Tester";
        String familyName = "McTesting";
        InterpreterDetails interpreterDetails = new InterpreterDetails("id1", "bookingRef1",
                givenName, familyName, "0771222222", "test1@email.com", "");

        assertTrue(interpreterDetails.buildInterpreterFullName().equals(givenName + " " + familyName));
    }

}
