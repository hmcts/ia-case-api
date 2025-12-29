package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.UUID;

public class PartyIdGenerator {

    private PartyIdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
