package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ContactPreferenceTest {

    @Test
    public void has_correct_asylum_contact_preference() {
        assertThat(ContactPreference.from("wantsEmail").get()).isEqualByComparingTo(ContactPreference.WANTS_EMAIL);
        assertThat(ContactPreference.from("wantsSms").get()).isEqualByComparingTo(ContactPreference.WANTS_SMS);
    }

    @Test
    public void has_correct_asylum_contact_preference_description() {
        assertEquals("Email", ContactPreference.WANTS_EMAIL.getDescription());
        assertEquals("Text message", ContactPreference.WANTS_SMS.getDescription());
    }

    @Test
    public void returns_optional_for_unknown_contact_preference() {
        assertThat(ContactPreference.from("some_unknown_type")).isEmpty();
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ContactPreference.values().length);
    }
}
