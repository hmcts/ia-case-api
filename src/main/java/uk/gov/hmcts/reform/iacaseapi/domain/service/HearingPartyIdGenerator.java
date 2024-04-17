package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.UUID;

public class HearingPartyIdGenerator {

    private HearingPartyIdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
