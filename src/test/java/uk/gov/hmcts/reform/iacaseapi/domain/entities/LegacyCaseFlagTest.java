package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LegacyCaseFlagTest {

    private final CaseFlagType caseFlagType = CaseFlagType.ANONYMITY;
    private final String legacyCaseFlagAdditionalInformation = "Anonymity";

    @Test
    void should_hold_onto_values() {
        LegacyCaseFlag legacyCaseFlag = new LegacyCaseFlag(
            caseFlagType,
            legacyCaseFlagAdditionalInformation
        );
        assertNotNull(legacyCaseFlag.getLegacyCaseFlagType());
        assertNotNull(legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
        assertEquals(caseFlagType, legacyCaseFlag.getLegacyCaseFlagType());
        assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
    }

    @Test
    void should_check_noargs() {
        LegacyCaseFlag legacyCaseFlag1 = new LegacyCaseFlag();
        assertNotNull(legacyCaseFlag1);
        assertNull(legacyCaseFlag1.getLegacyCaseFlagType());
        assertNull(legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());

        legacyCaseFlag1.setLegacyCaseFlagType(caseFlagType);
        legacyCaseFlag1.setLegacyCaseFlagAdditionalInformation(legacyCaseFlagAdditionalInformation);
        assertEquals(caseFlagType, legacyCaseFlag1.getLegacyCaseFlagType());
        assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());
    }

}
