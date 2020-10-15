package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;

@RunWith(JUnitParamsRunner.class)
public class CaseManagementLocationServiceTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    private final CaseManagementLocationService service = new CaseManagementLocationService();

    @Test
    @Parameters({
        "Birmingham, BIRMINGHAM",
        "Glasgow, GLASGOW",
        "Bradford, BRADFORD",
        "Hatton Cross, HATTON_CROSS",
        "Manchester, MANCHESTER",
        "Newport, NEWPORT",
        "Taylor House, TAYLOR_HOUSE",
        "Newcastle, null"
    })
    public void given_staffLocationName_then_return_caseManagementLocation(
        String staffLocationName, @Nullable BaseLocation baseLocation) {

        CaseManagementLocation actual = service.getCaseManagementLocation(staffLocationName);

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation()).isEqualTo(baseLocation);
    }

}