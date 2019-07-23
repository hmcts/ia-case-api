package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AsylumFieldNameFixer;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DataFixer;

@Configuration
public class AsylumCaseDataConfiguration {

    @Bean
    public DataFixer respondentsAgreedScheduleOfIssuesDescription_to_appellantsAgreedScheduleOfIssuesDescription() {
        return new AsylumFieldNameFixer(
            AsylumCaseFieldDefinition.RESPONDENTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION,
            AsylumCaseFieldDefinition.APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION);
    }

    @Bean
    public DataFixer respondentsDisputedScheduleOfIssuesDescription_to_appellantsDisputedScheduleOfIssuesDescription() {
        return new AsylumFieldNameFixer(
            AsylumCaseFieldDefinition.RESPONDENTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION,
            AsylumCaseFieldDefinition.APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION);
    }

    @Bean
    public DataFixer decisionAndReasonsDocuments_to_draftDecisionAndReasonsDocuments() {
        return new AsylumFieldNameFixer(
            AsylumCaseFieldDefinition.DECISION_AND_REASONS_DOCUMENTS,
            AsylumCaseFieldDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS);
    }
}
