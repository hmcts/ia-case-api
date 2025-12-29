package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.BailFieldCaseNameFixer;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DataFixer;

@Configuration
public class BailCaseDataConfiguration {

    @Bean
    public DataFixer caseNameAppender() {
        return new BailFieldCaseNameFixer(
            BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL,
            BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES,
            BailCaseFieldDefinition.APPLICANT_FAMILY_NAME);
    }

}
