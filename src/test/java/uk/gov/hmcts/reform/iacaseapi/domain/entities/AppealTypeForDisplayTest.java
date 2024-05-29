package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AppealTypeForDisplayTest {

    @Test
    void has_correct_asylum_appeal_types() {
        assertThat(AppealTypeForDisplay.from("revocationOfProtection").equals(Optional.of(AppealTypeForDisplay.RP)));
        assertThat(AppealTypeForDisplay.from("protection").equals(Optional.of(AppealTypeForDisplay.PA)));
        assertThat(AppealTypeForDisplay.from("refusalOfEu").equals(Optional.of(AppealTypeForDisplay.EA)));
        assertThat(AppealTypeForDisplay.from("refusalOfHumanRights").equals(Optional.of(AppealTypeForDisplay.HU)));
        assertThat(AppealTypeForDisplay.from("deprivation").equals(Optional.of(AppealTypeForDisplay.DC)));
        assertThat(AppealTypeForDisplay.from("euSettlementScheme").equals(Optional.of(AppealTypeForDisplay.EU)));
    }

    @Test
    void has_correct_asylum_appeal_types_description() {
        assertEquals("Revocation of a protection status", AppealTypeForDisplay.RP.getDescription());
        assertEquals("Refusal of protection claim", AppealTypeForDisplay.PA.getDescription());
        assertEquals("Refusal of application under the EEA regulations", AppealTypeForDisplay.EA.getDescription());
        assertEquals("Refusal of a human rights claim", AppealTypeForDisplay.HU.getDescription());
        assertEquals("Deprivation of citizenship", AppealTypeForDisplay.DC.getDescription());
        assertEquals("EU Settlement Scheme", AppealTypeForDisplay.EU.getDescription());
    }

    @Test
    void returns_optional_for_unknown_appeal_type() {
        assertThat(AppealTypeForDisplay.from("some_unknown_type")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, AppealTypeForDisplay.values().length);
    }
}
