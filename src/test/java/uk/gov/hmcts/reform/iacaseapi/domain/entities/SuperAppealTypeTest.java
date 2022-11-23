package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SuperAppealTypeTest {

    @Test
    void has_correct_asylum_appeal_types() {
        assertThat(SuperAppealType.from("revocationOfProtection").equals(Optional.of(SuperAppealType.RP)));
        assertThat(SuperAppealType.from("protection").equals(Optional.of(SuperAppealType.PA)));
        assertThat(SuperAppealType.from("refusalOfEu").equals(Optional.of(SuperAppealType.EA)));
        assertThat(SuperAppealType.from("refusalOfHumanRights").equals(Optional.of(SuperAppealType.HU)));
        assertThat(SuperAppealType.from("deprivation").equals(Optional.of(SuperAppealType.DC)));
        assertThat(SuperAppealType.from("ageAssessment").equals(Optional.of(SuperAppealType.AG)));
    }

    @Test
    void has_correct_asylum_appeal_types_description() {
        assertEquals("Revocation of a protection status", SuperAppealType.RP.getDescription());
        assertEquals("Refusal of protection claim", SuperAppealType.PA.getDescription());
        assertEquals("Refusal of application under the EEA regulations", SuperAppealType.EA.getDescription());
        assertEquals("Refusal of a human rights claim", SuperAppealType.HU.getDescription());
        assertEquals("Deprivation of citizenship", SuperAppealType.DC.getDescription());
        assertEquals("Age assessment appeal", SuperAppealType.AG.getDescription());
    }

    @Test
    void returns_optional_for_unknown_appeal_type() {
        assertThat(SuperAppealType.from("some_unknown_type")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, SuperAppealType.values().length);
    }
}

