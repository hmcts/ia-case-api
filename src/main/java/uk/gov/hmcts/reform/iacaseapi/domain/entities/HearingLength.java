package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import java.util.Optional;

public enum HearingLength {

    LENGTH_30_MINUTES(30),
    LENGTH_1_HOUR(60),
    LENGTH_1_HOUR_30_MINUTES(90),
    LENGTH_2_HOURS(120),
    LENGTH_2_HOURS_30_MINUTES(150),
    LENGTH_3_HOURS(180),
    LENGTH_3_HOURS_30_MINUTES(210),
    LENGTH_4_HOURS(240),
    LENGTH_4_HOURS_30_MINUTES(270),
    LENGTH_5_HOURS(300),
    LENGTH_5_HOURS_30_MINUTES(330),
    LENGTH_6_HOURS(360);

    private final int value;

    HearingLength(int value) {
        this.value = value;
    }

    public static Optional<HearingLength> from(
        int value
    ) {
        return stream(values())
            .filter(v -> v.getValue() == value)
            .findFirst();
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
