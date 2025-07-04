package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RemissionOption {

    ASYLUM_SUPPORT_FROM_HOME_OFFICE("asylumSupportFromHo"),
    FEE_WAIVER_FROM_HOME_OFFICE("feeWaiverFromHo"),
    UNDER_18_GET_SUPPORT("under18GetSupportFromLocalAuthority"),
    PARENT_GET_SUPPORT("parentGetSupportFromLocalAuthority"),
    NO_REMISSION("noneOfTheseStatements"),
    I_WANT_TO_GET_HELP_WITH_FEES("iWantToGetHelpWithFees");


    @JsonValue
    private final String value;

    RemissionOption(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}