package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CaseFlagTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("anonymity", CaseFlagType.ANONYMITY.toString());
        assertEquals("complexCase", CaseFlagType.COMPLEX_CASE.toString());
        assertEquals("deport", CaseFlagType.DEPORT.toString());
        assertEquals("detainedImmigrationAppeal", CaseFlagType.DETAINED_IMMIGRATION_APPEAL.toString());
        assertEquals("foreignNationalOffender", CaseFlagType.FOREIGN_NATIONAL_OFFENDER.toString());
        assertEquals("potentiallyViolentPerson", CaseFlagType.POTENTIALLY_VIOLENT_PERSON.toString());
        assertEquals("unacceptableCustomerBehaviour", CaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.toString());
        assertEquals("unaccompaniedMinor", CaseFlagType.UNACCOMPANIED_MINOR.toString());
        assertEquals("unknown", CaseFlagType.UNKNOWN.toString());

        assertEquals("Anonymity", CaseFlagType.ANONYMITY.getReadableText());
        assertEquals("Complex case", CaseFlagType.COMPLEX_CASE.getReadableText());
        assertEquals("Detained immigration appeal", CaseFlagType.DETAINED_IMMIGRATION_APPEAL.getReadableText());
        assertEquals("Foreign national offender", CaseFlagType.FOREIGN_NATIONAL_OFFENDER.getReadableText());
        assertEquals("Potentially violent person", CaseFlagType.POTENTIALLY_VIOLENT_PERSON.getReadableText());
        assertEquals("Unacceptable customer behaviour", CaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.getReadableText());
        assertEquals("Unaccompanied minor", CaseFlagType.UNACCOMPANIED_MINOR.getReadableText());
        assertEquals("Unknown", CaseFlagType.UNKNOWN.getReadableText());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(9, CaseFlagType.values().length);
    }
}
