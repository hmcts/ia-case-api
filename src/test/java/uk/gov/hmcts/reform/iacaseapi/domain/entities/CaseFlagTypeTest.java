package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class CaseFlagTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("anonymity", CaseFlagType.ANONYMITY.toString());
        assertEquals("complexCase", CaseFlagType.COMPLEX_CASE.toString());
        assertEquals("deport", CaseFlagType.DEPORT.toString());
        assertEquals("detainedImmigrationAppeal", CaseFlagType.DETAINED_IMMIGRATION_APPEAL.toString());
        assertEquals("foreignNationalOffender", CaseFlagType.FOREIGN_NATIONAL_OFFENDER.toString());
        assertEquals("potentiallyViolentPerson", CaseFlagType.POTENTIALLY_VIOLENT_PERSON.toString());
        assertEquals("unacceptableCustomerBehaviour", CaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.toString());
        assertEquals("unaccompaniedMinor", CaseFlagType.UNACCOMPANIED_MINOR.toString());
        assertEquals("setAsideReheard", CaseFlagType.SET_ASIDE_REHEARD.toString());
        assertEquals("unknown", CaseFlagType.UNKNOWN.toString());

        assertEquals("Anonymity", CaseFlagType.ANONYMITY.getReadableText());
        assertEquals("Complex case", CaseFlagType.COMPLEX_CASE.getReadableText());
        assertEquals("Detained immigration appeal", CaseFlagType.DETAINED_IMMIGRATION_APPEAL.getReadableText());
        assertEquals("Foreign national offender", CaseFlagType.FOREIGN_NATIONAL_OFFENDER.getReadableText());
        assertEquals("Potentially violent person", CaseFlagType.POTENTIALLY_VIOLENT_PERSON.getReadableText());
        assertEquals("Unacceptable customer behaviour", CaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.getReadableText());
        assertEquals("Unaccompanied minor", CaseFlagType.UNACCOMPANIED_MINOR.getReadableText());
        assertEquals("Set aside - Reheard", CaseFlagType.SET_ASIDE_REHEARD.getReadableText());
        assertEquals("Unknown", CaseFlagType.UNKNOWN.getReadableText());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(10, CaseFlagType.values().length);
    }
}
