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
        LegacyCaseFlag legacyCaseFlag1 = new LegacyCaseFlag();
        legacyCaseFlag1.setLegacyCaseFlagType(caseFlagType);
        legacyCaseFlag1.setLegacyCaseFlagAdditionalInformation(legacyCaseFlagAdditionalInformation);
        Assert.assertNotNull(legacyCaseFlag1.getLegacyCaseFlagType());
        Assert.assertNotNull(legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());
        Assert.assertEquals(caseFlagType, legacyCaseFlag1.getLegacyCaseFlagType());
        Assert.assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag1.getLegacyCaseFlagAdditionalInformation());
    }

}
