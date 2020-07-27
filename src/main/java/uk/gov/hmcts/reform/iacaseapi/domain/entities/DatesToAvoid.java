package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;

public class DatesToAvoid {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateToAvoid;
    private String dateToAvoidReason;

    public DatesToAvoid() {
        // noop -- for deserializer
    }

    public DatesToAvoid(LocalDate dateToAvoid, String dateToAvoidReason) {
        this.dateToAvoid = dateToAvoid;
        this.dateToAvoidReason = dateToAvoidReason;
    }

    public LocalDate getDateToAvoid() {
        return dateToAvoid;
    }

    public String getDateToAvoidReason() {
        return dateToAvoidReason;
    }

    public void setDateToAvoid(LocalDate dateToAvoid) {
        this.dateToAvoid = dateToAvoid;
    }

    public void setDateToAvoidReason(String dateToAvoidReason) {
        this.dateToAvoidReason = dateToAvoidReason;
    }
}
