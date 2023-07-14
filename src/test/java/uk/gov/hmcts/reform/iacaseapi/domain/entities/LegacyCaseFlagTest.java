package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.Assert;
import org.junit.Test;

public class LegacyCaseFlagTest {

    private final CaseFlagType caseFlagType = CaseFlagType.ANONYMITY;
    private final String legacyCaseFlagAdditionalInformation = "Anonymity";

    private LegacyCaseFlag legacyCaseFlag = new LegacyCaseFlag(
        caseFlagType,
        legacyCaseFlagAdditionalInformation
    );

    @Test
    public void should_hold_onto_values() {
        Assert.assertNotNull(legacyCaseFlag.getLegacyCaseFlagType());
        Assert.assertNotNull(legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
        Assert.assertEquals(caseFlagType, legacyCaseFlag.getLegacyCaseFlagType());
        Assert.assertEquals(legacyCaseFlagAdditionalInformation, legacyCaseFlag.getLegacyCaseFlagAdditionalInformation());
    }
    
}
