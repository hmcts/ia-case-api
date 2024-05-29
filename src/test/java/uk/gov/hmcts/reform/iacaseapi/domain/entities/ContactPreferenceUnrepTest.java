package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ContactPreferenceUnrepTest {

    @Test
    void has_correct_asylum_contact_preference_unrep_values() {
        assertThat(ContactPreferenceUnrep.from("wantsEmail").get()).isEqualByComparingTo(ContactPreferenceUnrep.WANTS_EMAIL);
        assertThat(ContactPreferenceUnrep.from("wantsSms").get()).isEqualByComparingTo(ContactPreferenceUnrep.WANTS_SMS);
        assertThat(ContactPreferenceUnrep.from("wantsPost").get()).isEqualByComparingTo(ContactPreferenceUnrep.WANTS_POST);
    }

    @Test
    void returns_optional_for_unknown_contact_preference_unrep() {
        assertThat(ContactPreferenceUnrep.from("unknown")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, ContactPreferenceUnrep.values().length);
    }
}