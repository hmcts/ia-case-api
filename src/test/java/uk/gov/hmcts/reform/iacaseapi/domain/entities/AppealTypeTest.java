package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AppealTypeTest {

    @Test
    void has_correct_asylum_appeal_types() {
        assertThat(AppealType.from("revocationOfProtection").equals(Optional.of(AppealType.RP)));
        assertThat(AppealType.from("protection").equals(Optional.of(AppealType.PA)));
        assertThat(AppealType.from("refusalOfEu").equals(Optional.of(AppealType.EA)));
        assertThat(AppealType.from("refusalOfHumanRights").equals(Optional.of(AppealType.HU)));
        assertThat(AppealType.from("deprivation").equals(Optional.of(AppealType.DC)));
        assertThat(AppealType.from("euSettlementScheme").equals(Optional.of(AppealType.EU)));
        assertThat(AppealType.from("ageAssessment").equals(Optional.of(AppealType.AG)));        
    }

    @Test
    void has_correct_asylum_appeal_types_description() {
        assertEquals("Revocation of a protection status", AppealType.RP.getDescription());
        assertEquals("Refusal of protection claim", AppealType.PA.getDescription());
        assertEquals("Refusal of application under the EEA regulations", AppealType.EA.getDescription());
        assertEquals("Refusal of a human rights claim", AppealType.HU.getDescription());
        assertEquals("Deprivation of citizenship", AppealType.DC.getDescription());
        assertEquals("EU Settlement Scheme", AppealType.EU.getDescription());
        assertEquals("Age assessment appeal", AppealType.AG.getDescription());        
    }

    @Test
    void returns_optional_for_unknown_appeal_type() {
        assertThat(AppealType.from("some_unknown_type")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, AppealType.values().length);
    }
}
