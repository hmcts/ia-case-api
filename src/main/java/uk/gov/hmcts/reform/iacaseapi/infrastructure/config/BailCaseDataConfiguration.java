package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.service.BailFieldCaseNameFixer;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DataFixer;

@Configuration
public class BailCaseDataConfiguration {

    @Bean
    public DataFixer<BailCase> caseNameAppender() {
        return new BailFieldCaseNameFixer(
            BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL,
            BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES,
            BailCaseFieldDefinition.APPLICANT_FAMILY_NAME);
    }

}
