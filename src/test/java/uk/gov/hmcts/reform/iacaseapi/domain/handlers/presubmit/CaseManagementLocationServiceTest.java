package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
public class CaseManagementLocationServiceTest {

    private final CaseManagementLocationService service = new CaseManagementLocationService();

    @ParameterizedTest
    @CsvSource({
        "Birmingham, BIRMINGHAM",
        "Glasgow, GLASGOW",
        "Bradford, BRADFORD",
        "Hatton Cross, HATTON_CROSS",
        "Manchester, MANCHESTER",
        "Newport, NEWPORT",
        "Taylor House, TAYLOR_HOUSE",
        "Newcastle,"
    })
    public void given_staffLocationName_then_return_caseManagementLocation(
        String staffLocationName, BaseLocation baseLocation) {

        CaseManagementLocation actual = service.getCaseManagementLocation(staffLocationName);

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation()).isEqualTo(baseLocation);
    }

}
