package uk.gov.hmcts.reform.bailcaseapi.domain.entities;


import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

public class BailCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Need more tests as we add more fields
    @Test
    void reads_string() throws IOException {

        String caseData = "{\"isAdmin\":\"Yes\"}";

        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);
        Optional<YesOrNo> readApplicantName = bailCase.read(BailCaseFieldDefinition.IS_ADMIN);

        assertThat(readApplicantName.get()).isEqualTo(YES);
    }

    @Test
    void writes_simple_type() {

        BailCase bailCase = new BailCase();

        bailCase.write(BailCaseFieldDefinition.IS_ADMIN, YES);

        assertThat(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, YesOrNo.class).get())
            .isEqualTo(YES);
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"isAdmin\":\"Yes\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        bailCase.clear(BailCaseFieldDefinition.IS_ADMIN);

        assertThat(bailCase.read(BailCaseFieldDefinition.IS_ADMIN, String.class)).isEmpty();
    }

    @Test
    void remove_entry_by_bail_case_def() throws IOException {

        String caseData = "{\"applicantFamilyName\":\"Doe\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        bailCase.remove(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME);

        assertThat(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).isEmpty();
    }

    @Test
    void remove_entry_by_key_string() throws IOException {

        String caseData = "{\"applicantMobileNumber1\":\"01234567891\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        bailCase.removeByString("applicantMobileNumber1");

        assertThat(bailCase.read(BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER, String.class)).isEmpty();
    }
}
