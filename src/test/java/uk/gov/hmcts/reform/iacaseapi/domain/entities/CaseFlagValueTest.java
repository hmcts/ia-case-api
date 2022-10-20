package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

class CaseFlagValueTest {

    private String name = "some-name";
    private String status = "some-status";
    private String flagCode = "some-code";
    private String dateTimeCreated = "some-date";
    private YesOrNo hearingRelevant = YesOrNo.YES;
    @Mock
    private List<CaseFlagPath> caseFlagPath;

    private CaseFlagValue caseFlagValue;

    @BeforeEach
    public void setUp() {
        caseFlagValue = new CaseFlagValue(name, status, flagCode, dateTimeCreated, hearingRelevant, caseFlagPath);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(caseFlagValue.getName()).isEqualTo(name);
        assertThat(caseFlagValue.getStatus()).isEqualTo(status);
        assertThat(caseFlagValue.getFlagCode()).isEqualTo(flagCode);
        assertThat(caseFlagValue.getDateTimeCreated()).isEqualTo(dateTimeCreated);
        assertThat(caseFlagValue.getHearingRelevant()).isEqualTo(hearingRelevant);
        assertThat(caseFlagValue.getCaseFlagPath()).isEqualTo(caseFlagPath);
    }

}