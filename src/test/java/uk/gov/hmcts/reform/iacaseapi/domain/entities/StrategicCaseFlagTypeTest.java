package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StrategicCaseFlagTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("rroAnonymisation", StrategicCaseFlagType.RRO_ANONYMISATION.toString());
        assertEquals("complexCase", StrategicCaseFlagType.COMPLEX_CASE.toString());
        assertEquals("detainedIndividual", StrategicCaseFlagType.DETAINED_INDIVIDUAL.toString());
        assertEquals("unacceptableCustomerBehaviour", StrategicCaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.toString());
        assertEquals("foreignNationalOffender", StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER.toString());
        assertEquals("unaccompaniedMinor", StrategicCaseFlagType.UNACCOMPANIED_MINOR.toString());
        assertEquals("unknown", StrategicCaseFlagType.UNKNOWN.toString());

        assertEquals("RRO (Restricted Reporting Order / Anonymisation)", StrategicCaseFlagType.RRO_ANONYMISATION.getReadableText());
        assertEquals("Complex Case", StrategicCaseFlagType.COMPLEX_CASE.getReadableText());
        assertEquals("Detained individual", StrategicCaseFlagType.DETAINED_INDIVIDUAL.getReadableText());
        assertEquals("Foreign national offender", StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER.getReadableText());
        assertEquals("Unaccompanied minor", StrategicCaseFlagType.UNACCOMPANIED_MINOR.getReadableText());
        assertEquals("Unacceptable/disruptive customer behaviour", StrategicCaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.getReadableText());
        assertEquals("Unknown", StrategicCaseFlagType.UNKNOWN.getReadableText());

        assertEquals("CF0012", StrategicCaseFlagType.RRO_ANONYMISATION.getFlagCode());
        assertEquals("CF0002", StrategicCaseFlagType.COMPLEX_CASE.getFlagCode());
        assertEquals("PF0019", StrategicCaseFlagType.DETAINED_INDIVIDUAL.getFlagCode());
        assertEquals("PF0012", StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER.getFlagCode());
        assertEquals("PF0013", StrategicCaseFlagType.UNACCOMPANIED_MINOR.getFlagCode());
        assertEquals("PF0007", StrategicCaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR.getFlagCode());
        assertEquals("Unknown", StrategicCaseFlagType.UNKNOWN.getFlagCode());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, StrategicCaseFlagType.values().length);
    }
}
