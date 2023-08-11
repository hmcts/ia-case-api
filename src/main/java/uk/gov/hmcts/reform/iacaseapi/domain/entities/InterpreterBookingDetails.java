package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class InterpreterBookingDetails {

    private InterpreterBookingStatus bookingStatus;
    private String details;

    public InterpreterBookingDetails(InterpreterBookingStatus bookingStatus, String details) {
        this.bookingStatus = bookingStatus;
        this.details = details;
    }

    public InterpreterBookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public String getDetails() {
        return details;
    }

}
