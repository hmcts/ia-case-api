package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.Assert;
import org.junit.Test;

public class LegacyCaseFlagTest {

    private final CaseFlagType caseFlagType = CaseFlagType.ANONYMITY;
    private final String legacyCaseFlagAdditionalInformation = "Anonymity";

    @Test
    public void should_hold_onto_values() {
        LegacyCaseFlag legacyCaseFlag = new LegacyCaseFlag(
            caseFlagType,
            legacyCaseFlagAdditionalInformation
        );
        Assert.assertNotNull(legacyCaseFlag.getLegacyCaseFlagType());
        Assert.assertNotNull(legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
        Assert.assertEquals(caseFlagType, legacyCaseFlag.getLegacyCaseFlagType());
        Assert.assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
    }

    @Test
    public void should_check_noargs() {
        LegacyCaseFlag legacyCaseFlag1 = new LegacyCaseFlag(null, null);
        Assert.assertNotNull(legacyCaseFlag1);
        Assert.assertNull(legacyCaseFlag1.getLegacyCaseFlagType());
        Assert.assertNull(legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());

        legacyCaseFlag1.setLegacyCaseFlagType(caseFlagType);
        legacyCaseFlag1.setLegacyCaseFlagAdditionalInformation(legacyCaseFlagAdditionalInformation);
        Assert.assertEquals(caseFlagType, legacyCaseFlag1.getLegacyCaseFlagType());
        Assert.assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());
    }

}
