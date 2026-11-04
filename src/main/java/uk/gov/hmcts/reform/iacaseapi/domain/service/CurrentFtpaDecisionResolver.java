package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@Component
public class CurrentFtpaDecisionResolver {

    public Optional<String> getApplicantType(AsylumCase asylumCase) {
        return asylumCase.read(FTPA_APPLICANT_TYPE, String.class)
            .filter(type -> type.equals(APPELLANT.toString()) || type.equals(RESPONDENT.toString()));
    }

    public boolean isDecided(AsylumCase asylumCase, String applicantType) {
        return asylumCase
            .read(isAppellant(applicantType) ? IS_FTPA_APPELLANT_DECIDED : IS_FTPA_RESPONDENT_DECIDED, YesOrNo.class)
            .map(decided -> decided == YesOrNo.YES)
            .orElse(false);
    }

    public Optional<String> getCurrentOutcome(AsylumCase asylumCase, String applicantType) {
        boolean appellant = isAppellant(applicantType);

        Optional<String> residentJudgeOutcome = asylumCase.read(
            appellant ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class);

        if (residentJudgeOutcome.isPresent()) {
            return residentJudgeOutcome;
        }

        return asylumCase.read(
            appellant ? FTPA_APPELLANT_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class);
    }

    private boolean isAppellant(String applicantType) {
        return APPELLANT.toString().equals(applicantType);
    }
}
