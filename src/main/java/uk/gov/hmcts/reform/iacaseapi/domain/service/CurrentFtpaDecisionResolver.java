package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Component
public class CurrentFtpaDecisionResolver {

    static final String DECISION_UPDATED = "ftpaDecisionUpdated";

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
        return asylumCase.read(getCurrentOutcomeField(asylumCase, applicantType), String.class);
    }

    public AsylumCaseFieldDefinition getCurrentOutcomeField(AsylumCase asylumCase, String applicantType) {
        boolean appellant = isAppellant(applicantType);

        AsylumCaseFieldDefinition residentJudgeField =
            appellant ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;

        if (asylumCase.read(residentJudgeField, String.class).isPresent()) {
            return residentJudgeField;
        }

        return appellant ? FTPA_APPELLANT_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
    }

    public boolean isDecisionChanged(AsylumCase asylumCase) {
        return asylumCase.read(FTPA_DECISION_UPDATED_CONFIRMATION, String.class)
            .map(DECISION_UPDATED::equals)
            .orElse(false);
    }

    public boolean isDocumentUploaded(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_FTPA_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
            .map(answer -> answer == YesOrNo.YES)
            .orElse(false);
    }

    public void clearUpdateFtpaDecisionFields(AsylumCase asylumCase) {
        asylumCase.clear(UPDATE_FTPA_DECISION_LIST);
        asylumCase.clear(UPDATE_FTPA_CURRENT_DECISION_TEXT);
        asylumCase.clear(FTPA_DECISION_UPDATED_CONFIRMATION);
        asylumCase.clear(UPDATE_FTPA_DECISION_OUTCOME_TYPE);
        asylumCase.clear(UPDATE_FTPA_DECISION_AND_REASONS_FINAL_CHECK);
        asylumCase.clear(FTPA_DECISION_AND_REASONS_DOC_UPLOAD);
        asylumCase.clear(SUMMARISE_FTPA_DECISION_AND_REASONS_DOCUMENT);
    }

    private boolean isAppellant(String applicantType) {
        return APPELLANT.toString().equals(applicantType);
    }
}
