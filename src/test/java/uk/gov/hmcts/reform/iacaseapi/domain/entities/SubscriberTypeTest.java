package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SubscriberTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("appellant", SubscriberType.APPELLANT.toString());
        assertEquals("supporter", SubscriberType.SUPPORTER.toString());
    }

    @Test
    public void has_correct_subscriber_types() {
        assertThat(SubscriberType.from("appellant").get()).isEqualByComparingTo(SubscriberType.APPELLANT);
        assertThat(SubscriberType.from("supporter").get()).isEqualByComparingTo(SubscriberType.SUPPORTER);
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, SubscriberType.values().length);
    }
}
