package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum HearingCentre {

    BIRMINGHAM("birmingham"),
    BRADFORD("bradford"),
    GLASGOW("glasgow"),
    HATTON_CROSS("hattonCross"),
    MANCHESTER("manchester"),
    NEWCASTLE("newcastle"),
    NEWPORT("newport"),
    TAYLOR_HOUSE("taylorHouse"),
    YARLSWOOD("yarlsWood");

    @JsonValue private final String value;

    HearingCentre(String value) {
        this.value = value;
    }

    public static Optional<HearingCentre> from(String value) {
        return stream(values()).filter(v -> v.getValue().equals(value)).findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
