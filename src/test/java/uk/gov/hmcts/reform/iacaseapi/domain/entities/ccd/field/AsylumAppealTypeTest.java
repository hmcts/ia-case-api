package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.junit.Test;

public class AsylumAppealTypeTest {

    @Test
    public void has_correct_asylum_appeal_types() {
        assertThat(AsylumAppealType.from("revocationOfProtection").get(), is(AsylumAppealType.RP));
        assertThat(AsylumAppealType.from("protection").get(), is(AsylumAppealType.PA));
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertThat(AsylumAppealType.from("some_unknown_type"), is(Optional.empty()));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, AsylumAppealType.values().length);
    }
}