package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class OutOfCountryDecisionTypeTest {

    @Test
    void has_correct_asylum_out_of_country_decision_types() {
        assertEquals(OutOfCountryDecisionType.from("refusalOfHumanRights"), Optional.of(REFUSAL_OF_HUMAN_RIGHTS));
        assertEquals(OutOfCountryDecisionType.from("refusalOfProtection"),Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        assertEquals(OutOfCountryDecisionType.from("removalOfClient"),Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        assertEquals(OutOfCountryDecisionType.from("refusalOfEntry"),Optional.of(OutOfCountryDecisionType.REFUSAL_OF_ENTRY));

    }

    @Test
    void has_correct_asylum_out_of_country_decision_types_description() {
        assertEquals("A decision to refuse a human rights claim for entry clearance", REFUSAL_OF_HUMAN_RIGHTS.getDescription());
        assertEquals("A decision to refuse a human rights or protection claim, or deprive you of British citizenship, where you can only apply after your client has left the country", OutOfCountryDecisionType.REFUSAL_OF_PROTECTION.getDescription());
        assertEquals("A decision to remove your client under the Immigration (European Economic Area) Regulations 2016", OutOfCountryDecisionType.REMOVAL_OF_CLIENT.getDescription());
        assertEquals("A decision to refuse entry to the UK under the Immigration (European Economic Area) Regulations 2016", OutOfCountryDecisionType.REFUSAL_OF_ENTRY.getDescription());
    }

    @Test
    void returns_optional_for_unknown_out_of_country_decision_type() {
        assertThat(OutOfCountryDecisionType.from("some_unknown_type")).isEmpty();
    }

    @Test
    void should_format_type_correctly() {
        OutOfCountryDecisionType outOfCountryDecisionType = REFUSAL_OF_HUMAN_RIGHTS;
        assertEquals("refusalOfHumanRights: A decision to refuse a human rights claim for entry clearance",outOfCountryDecisionType.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, OutOfCountryDecisionType.values().length);
    }
}
