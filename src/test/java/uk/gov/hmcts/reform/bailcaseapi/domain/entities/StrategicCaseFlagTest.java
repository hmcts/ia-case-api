package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_FCS;

class StrategicCaseFlagTest {

    private final String appellantName = "some-appellant-name";
    private StrategicCaseFlag strategicCaseFlag;

    private final CaseFlagDetail flagDetail = mock(CaseFlagDetail.class);
    private final List<CaseFlagDetail> details = singletonList(flagDetail);

    @ParameterizedTest
    @ValueSource(strings = {ROLE_ON_CASE_APPLICANT, ROLE_ON_CASE_FCS})
    void should_hold_onto_values(String value) {
        strategicCaseFlag = new StrategicCaseFlag(appellantName, value);
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((value));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo((Collections.emptyList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {ROLE_ON_CASE_APPLICANT, ROLE_ON_CASE_FCS})
    void should_hold_onto_details_values(String value) {
        strategicCaseFlag = new StrategicCaseFlag(appellantName, value, details);
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((value));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo(details);
    }
}
