package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ContactPreferenceTest {

    @Test
    void has_correct_asylum_contact_preference() {
        assertThat(ContactPreference.from("telephone").get()).isEqualByComparingTo(ContactPreference.TELEPHONE);
        assertThat(ContactPreference.from("mobile").get()).isEqualByComparingTo(ContactPreference.MOBILE);
        assertThat(ContactPreference.from("email").get()).isEqualByComparingTo(ContactPreference.EMAIL);
    }

    @Test
    void returns_optional_for_unknown_contact_preference() {
        assertThat(ContactPreference.from("unknown")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, ContactPreference.values().length);
    }
}
