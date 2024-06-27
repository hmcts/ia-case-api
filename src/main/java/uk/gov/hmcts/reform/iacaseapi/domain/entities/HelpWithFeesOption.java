package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HelpWithFeesOption {

    WANT_TO_APPLY("wantToApply"),
    ALREADY_APPLIED("alreadyApplied"),
    WILL_PAY_FOR_APPEAL("willPayForAppeal");

    @JsonValue
    private final String value;

    HelpWithFeesOption(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}