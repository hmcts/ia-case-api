package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.junit.Test;

public class AppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertThat(AppealType.from("revocationOfProtection").get(), is(AppealType.RP));
        assertThat(AppealType.from("protection").get(), is(AppealType.PA));
        assertThat(AppealType.from("refusalOfEu").get(), is(AppealType.EA));
        assertThat(AppealType.from("refusalOfHumanRights").get(), is(AppealType.HU));
        assertThat(AppealType.from("deprivation").get(), is(AppealType.DC));
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
        assertThat(AppealType.from("some_unknown_type"), is(Optional.empty()));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, AppealType.values().length);
    }
}
