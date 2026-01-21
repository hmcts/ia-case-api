package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class InterpreterBookingStatusTest {

    @Test
    void has_correct_interpreter_booking_statuses() {
        assertThat(InterpreterBookingStatus.from("notRequested").equals(Optional.of(InterpreterBookingStatus.NOT_REQUESTED)));
        assertThat(InterpreterBookingStatus.from("requested").equals(Optional.of(InterpreterBookingStatus.REQUESTED)));
        assertThat(InterpreterBookingStatus.from("booked").equals(Optional.of(InterpreterBookingStatus.BOOKED)));
        assertThat(InterpreterBookingStatus.from("cancelled").equals(Optional.of(InterpreterBookingStatus.CANCELLED)));
    }

    @Test
    void returns_optional_for_unknown_interpreter_booking_status() {
        assertThat(InterpreterBookingStatus.from("unknown")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, InterpreterBookingStatus.values().length);
    }
}
