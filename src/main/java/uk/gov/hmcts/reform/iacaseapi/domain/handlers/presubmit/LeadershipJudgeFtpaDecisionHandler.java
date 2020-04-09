package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class LeadershipJudgeFtpaDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public LeadershipJudgeFtpaDecisionHandler(
        DateProvider dateProvider,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.dateProvider = dateProvider;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String ftpaApplicantType =
            asylumCase
                .read(FTPA_APPLICANT_TYPE, String.class)
                .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionAndReasonsDocument = asylumCase.read(
            ftpaApplicantType.equals("appellant") == true ? FTPA_APPELLANT_DECISION_DOCUMENT : FTPA_RESPONDENT_DECISION_DOCUMENT);
        final List<IdValue<DocumentWithDescription>> existingFtpaDecisionAndReasonsDocuments = maybeFtpaDecisionAndReasonsDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaDecisionDocuments = asylumCase.read(
            ftpaApplicantType.equals("appellant") == true ? ALL_FTPA_APPELLANT_DECISION_DOCS : ALL_FTPA_RESPONDENT_DECISION_DOCS);
        final List<IdValue<DocumentWithMetadata>> existingFtpaDecisionDocuments = maybeFtpaDecisionDocuments.orElse(Collections.emptyList());

        List<DocumentWithMetadata> ftpaDecisionAndReasonsDocuments = new ArrayList<>();
        ftpaDecisionAndReasonsDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    existingFtpaDecisionAndReasonsDocuments,
                    DocumentTag.FTPA_DECISION_AND_REASONS
                )
        );

        List<IdValue<DocumentWithMetadata>> allFtpaDecisionDocuments =
            documentsAppender.append(
                existingFtpaDecisionDocuments,
                ftpaDecisionAndReasonsDocuments
            );

        String ftpaDecisionOutcomeType = asylumCase.read(ftpaApplicantType.equals("appellant") == true ? FTPA_APPELLANT_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));
        if (ftpaApplicantType.equals("appellant")
            && (ftpaDecisionOutcomeType.equals("granted") || ftpaDecisionOutcomeType.equals("partiallyGranted"))
        ) {

            asylumCase.write(IS_APPELLANT_FTPA_DECISION_VISIBLE_TO_RESPONDENT, YesOrNo.YES);

        } else if (ftpaApplicantType.equals("respondent")
                   && (ftpaDecisionOutcomeType.equals("granted") || ftpaDecisionOutcomeType.equals("partiallyGranted"))
        ) {

            asylumCase.write(IS_RESPONDENT_FTPA_DECISION_VISIBLE_TO_APPELLANT, YesOrNo.YES);
        }

        asylumCase.write(
            ftpaApplicantType.equals("appellant") == true ? ALL_FTPA_APPELLANT_DECISION_DOCS : ALL_FTPA_RESPONDENT_DECISION_DOCS,
            allFtpaDecisionDocuments);
        asylumCase.write(ftpaApplicantType.equals("appellant") == true ? FTPA_APPELLANT_DECISION_DATE : FTPA_RESPONDENT_DECISION_DATE,
            dateProvider.now().toString());
        asylumCase.write(ftpaApplicantType.equals("appellant") == true ? IS_FTPA_APPELLANT_DECIDED : IS_FTPA_RESPONDENT_DECIDED,
            YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
