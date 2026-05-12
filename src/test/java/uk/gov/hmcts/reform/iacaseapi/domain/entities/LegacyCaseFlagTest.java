package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LegacyCaseFlagTest {

    private final CaseFlagType caseFlagType = CaseFlagType.ANONYMITY;
    private final String legacyCaseFlagAdditionalInformation = "Anonymity";

    @Test
    public void should_hold_onto_values() {
        LegacyCaseFlag legacyCaseFlag = new LegacyCaseFlag(
            caseFlagType,
            legacyCaseFlagAdditionalInformation
        );
        Assertions.assertNotNull(legacyCaseFlag.getLegacyCaseFlagType());
        Assertions.assertNotNull(legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
        Assertions.assertEquals(caseFlagType, legacyCaseFlag.getLegacyCaseFlagType());
        Assertions.assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
    }

    @Test
    public void should_check_noargs() {
        LegacyCaseFlag legacyCaseFlag1 = new LegacyCaseFlag();
        Assertions.assertNotNull(legacyCaseFlag1);
        Assertions.assertNull(legacyCaseFlag1.getLegacyCaseFlagType());
        Assertions.assertNull(legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());

        legacyCaseFlag1.setLegacyCaseFlagType(caseFlagType);
        legacyCaseFlag1.setLegacyCaseFlagAdditionalInformation(legacyCaseFlagAdditionalInformation);
        Assertions.assertEquals(caseFlagType, legacyCaseFlag1.getLegacyCaseFlagType());
        Assertions.assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());
    }

}
