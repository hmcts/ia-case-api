package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import static java.util.Arrays.stream;

public enum SourceOfAppeal {

    PAPER_FORM("paperForm"),
    TRANSFERRED_FROM_UPPER_TRIBUNAL("transferredFromUpperTribunal"),
    REHYDRATED_APPEAL("rehydratedAppeal");

    @JsonValue
    private String value;

    SourceOfAppeal(String value) {
        this.value = value;
    }

    public static SourceOfAppeal from(String value) {
        return stream(values())
                .filter(v -> v.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(value + " is not a valid source of appeal"));
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
