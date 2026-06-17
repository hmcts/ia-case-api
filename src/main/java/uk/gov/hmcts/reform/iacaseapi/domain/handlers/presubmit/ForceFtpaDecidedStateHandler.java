package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_LIST_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;


@Component
public class ForceFtpaDecidedStateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String APPELLANT_TYPE = "appellant";
    private static final String RESPONDENT_TYPE = "respondent";

    private final Appender<FtpaApplications> ftpaAppender;

    public ForceFtpaDecidedStateHandler(Appender<FtpaApplications> ftpaAppender) {
        this.ftpaAppender = ftpaAppender;
    }


    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.FORCE_FTPA_DECIDED_STATE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String applicantType = asylumCase.read(FTPA_APPLICANT_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is missing"));

        if (APPELLANT_TYPE.equals(applicantType)) {
            handleAppellantFtpaDecision(asylumCase);
        } else if (RESPONDENT_TYPE.equals(applicantType)) {
            handleRespondentFtpaDecision(asylumCase);
        } else {
            throw new IllegalStateException("Unsupported ftpaApplicantType: " + applicantType);
        }

        asylumCase.write(IS_FTPA_LIST_VISIBLE, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void handleAppellantFtpaDecision(AsylumCase asylumCase) {
        asylumCase.write(IS_FTPA_APPELLANT_DECIDED, YesOrNo.YES);

        String decisionOutcomeType = asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaAppellantRjDecisionOutcomeType is missing"));

        createFtpaApplication(asylumCase, APPELLANT, decisionOutcomeType);
    }

    private void handleRespondentFtpaDecision(AsylumCase asylumCase) {
        asylumCase.write(IS_FTPA_RESPONDENT_DECIDED, YesOrNo.YES);

        String ftpaDecisionDate = asylumCase.read(FTPA_APPELLANT_DECISION_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionDate is missing"));
        asylumCase.write(FTPA_RESPONDENT_DECISION_DATE, ftpaDecisionDate);

        String decisionOutcomeType = asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaRespondentRjDecisionOutcomeType is missing"));

        createFtpaApplication(asylumCase, Parties.RESPONDENT, decisionOutcomeType);
    }

    private void createFtpaApplication(AsylumCase asylumCase, Parties applicant, String decisionOutcomeType) {
        String ftpaDecisionDate = asylumCase.read(FTPA_APPELLANT_DECISION_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionDate is missing"));

        FtpaApplications newFtpaApplication = FtpaApplications.builder()
            .ftpaApplicant(applicant.toString())
            .ftpaApplicationDate(ftpaDecisionDate)
            .ftpaDecisionOutcomeType(decisionOutcomeType)
            .build();

        Optional<List<IdValue<FtpaApplications>>> maybeExistingFtpaApplications =
            asylumCase.read(FTPA_LIST);

        List<IdValue<FtpaApplications>> allFtpaApplications =
            ftpaAppender.append(newFtpaApplication, maybeExistingFtpaApplications.orElse(emptyList()));

        asylumCase.write(FTPA_LIST, allFtpaApplications);
    }

}
