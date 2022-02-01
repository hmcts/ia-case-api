package uk.gov.hmcts.reform.bailcaseapi.domain.entities;


import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class BailCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Need more tests as we add more fields
    @Test
    void reads_string() throws IOException {

        String caseData = "{\"appellantGivenNames\":\"John Smith\"}";

        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);
        Optional<String> readApplicantName = bailCase.read(BailCaseFieldDefinition.APPELLANT_GIVEN_NAMES);

        assertThat(readApplicantName.get()).isEqualTo("John Smith");
    }

    @Test
    void writes_simple_type() {

        BailCase bailCase = new BailCase();

        bailCase.write(BailCaseFieldDefinition.APPELLANT_GIVEN_NAMES, "test-name");

        assertThat(bailCase.read(BailCaseFieldDefinition.APPELLANT_GIVEN_NAMES, String.class).get())
            .isEqualTo("test-name");
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"appellantGivenNames\":\"John Smith\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        bailCase.clear(BailCaseFieldDefinition.APPELLANT_GIVEN_NAMES);

        assertThat(bailCase.read(BailCaseFieldDefinition.APPELLANT_GIVEN_NAMES, String.class)).isEmpty();
    }
}
