package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartyIdGeneratorTest {

    @Test
    void should_generate_valid_uuid_as_party_id() {

        Pattern partyIdRegexPattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

        assertTrue(partyIdRegexPattern.matcher(PartyIdGenerator.generate()).matches());
    }

}