package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class CaseFlagTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("anonymity", CaseFlagType.ANONYMITY.toString());
        assertEquals("complexCase", CaseFlagType.COMPLEX_CASE.toString());
        assertEquals("detainedImmigrationAppeal", CaseFlagType.DETAINED_IMMIGRATION_APPEAL.toString());
        assertEquals("foreignNationalOffender", CaseFlagType.FOREIGN_NATIONAL_OFFENDER.toString());
        assertEquals("potentiallyViolentPerson", CaseFlagType.POTENTIALLY_VIOLENT_PERSON.toString());
        assertEquals("unacceptableCustomerBehaviour", CaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.toString());
        assertEquals("unaccompaniedMinor", CaseFlagType.UNACCOMPANIED_MINOR.toString());
        assertEquals("unknown", CaseFlagType.UNKNOWN.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, CaseFlagType.values().length);
    }
}
