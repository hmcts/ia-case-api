package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
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
