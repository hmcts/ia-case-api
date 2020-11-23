package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertThat(AppealType.from("revocationOfProtection").equals(AppealType.RP));
        assertThat(AppealType.from("protection").equals(AppealType.PA));
        assertThat(AppealType.from("refusalOfEu").equals(AppealType.EA));
        assertThat(AppealType.from("refusalOfHumanRights").equals(AppealType.HU));
        assertThat(AppealType.from("deprivation").equals(AppealType.DC));
    }

    @Test
    public void has_correct_asylum_appeal_types_description() {
        assertEquals("Revocation of a protection status", AppealType.RP.getDescription());
        assertEquals("Refusal of protection claim", AppealType.PA.getDescription());
        assertEquals("Refusal of application under the EEA regulations", AppealType.EA.getDescription());
        assertEquals("Refusal of a human rights claim", AppealType.HU.getDescription());
        assertEquals("Deprivation of citizenship", AppealType.DC.getDescription());
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertThat(AppealType.from("some_unknown_type")).isEmpty();
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, AppealType.values().length);
    }
}
