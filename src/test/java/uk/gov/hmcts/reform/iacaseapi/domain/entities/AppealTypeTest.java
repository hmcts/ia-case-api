package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AppealTypeTest {

    @Test
    void has_correct_asylum_appeal_types() {
        assertEquals(AppealType.from("revocationOfProtection").get(), AppealType.RP);
        assertEquals(AppealType.from("protection").get(), AppealType.PA);
        assertEquals(AppealType.from("refusalOfEu").get(), AppealType.EA);
        assertEquals(AppealType.from("refusalOfHumanRights").get(), AppealType.HU);
        assertEquals(AppealType.from("deprivation").get(), AppealType.DC);
    }

    @Test
    void returns_optional_for_unknown_appeal_type() {
        assertSame(AppealType.from("some_unknown_type"), Optional.empty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, AppealType.values().length);
    }
}
