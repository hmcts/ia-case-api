package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.*;

@Configuration
public class AsylumCaseDataConfiguration {

    @Bean
    public DataFixer<AsylumCase> respondentsAgreedScheduleOfIssuesDescription_to_appellantsAgreedScheduleOfIssuesDescription() {
        return new AsylumFieldNameFixer(
            AsylumCaseFieldDefinition.RESPONDENTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION,
            AsylumCaseFieldDefinition.APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION);
    }

    @Bean
    public DataFixer<AsylumCase> respondentsDisputedScheduleOfIssuesDescription_to_appellantsDisputedScheduleOfIssuesDescription() {
        return new AsylumFieldNameFixer(
            AsylumCaseFieldDefinition.RESPONDENTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION,
            AsylumCaseFieldDefinition.APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION);
    }

    @Bean
    public DataFixer<AsylumCase> decisionAndReasonsDocuments_to_draftDecisionAndReasonsDocuments() {
        return new AsylumFieldNameFixer(
            AsylumCaseFieldDefinition.DECISION_AND_REASONS_DOCUMENTS,
            AsylumCaseFieldDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS);
    }

    @Bean
    public DataFixer<AsylumCase> hearingAttendeesAndDurationInitializer() {
        return new AsylumCaseValueInitializerFixer<>(
            AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED,
            YesOrNo.NO);
    }

    @Bean
    public DataFixer<AsylumCase> caseNameAppender(FeatureToggler featureToggler) {
        return new AsylumFieldCaseNameFixer(
            AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL,
            AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES,
            AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, featureToggler);
    }

    @Bean
    public DataFixer<AsylumCase> hmctsCaseCategoryAppender() {
        return new AsylumFieldCaseCategoryFixer(
            AsylumCaseFieldDefinition.HMCTS_CASE_CATEGORY,
            AsylumCaseFieldDefinition.APPEAL_TYPE);
    }
}
