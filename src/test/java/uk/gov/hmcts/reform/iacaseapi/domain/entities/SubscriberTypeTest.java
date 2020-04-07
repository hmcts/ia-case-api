package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.junit.Test;

public class SubscriberTypeTest {

    @Test
    public void has_correct_asylum_subscriber_types() {
        assertThat(SubscriberType.from("appellant").get(), is(SubscriberType.APPELLANT));
        assertThat(SubscriberType.from("supporter").get(), is(SubscriberType.SUPPORTER));
    }

    @Test
    public void returns_optional_for_unknown_subscriber_type() {
        assertThat(SubscriberType.from("some_unknown_type"), is(Optional.empty()));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, SubscriberType.values().length);
    }
}
