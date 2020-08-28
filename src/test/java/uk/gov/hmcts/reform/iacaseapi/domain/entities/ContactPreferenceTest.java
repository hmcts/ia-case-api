package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContactPreferenceTest {

    @Test
    void has_correct_asylum_contact_preference() {
        assertSame(ContactPreference.from("wantsEmail").get(), ContactPreference.WANTS_EMAIL);
        assertSame(ContactPreference.from("wantsSms").get(), ContactPreference.WANTS_SMS);
    }

    @Test
    void returns_optional_for_unknown_contact_preference() {
        assertSame(ContactPreference.from("some_unknown_type"), Optional.empty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ContactPreference.values().length);
    }
}
