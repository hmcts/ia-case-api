package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustodialSentenceDateTest {

    private final String custodialDate = "2022-11-09";

    private CustodialSentenceDate custodialSentenceDate;

    @Test
    void should_hold_onto_values() {
        custodialSentenceDate = new CustodialSentenceDate(custodialDate);
        assertThat(custodialSentenceDate.getCustodialDate()).isEqualTo(custodialDate);
    }

}