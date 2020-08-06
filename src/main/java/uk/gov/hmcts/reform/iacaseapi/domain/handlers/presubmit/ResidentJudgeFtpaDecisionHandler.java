package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class ResidentJudgeFtpaDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public ResidentJudgeFtpaDecisionHandler(
        DateProvider dateProvider,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.dateProvider = dateProvider;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION;
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
            valueOf(String.format("FTPA_%s_DECISION_DOCUMENT", ftpaApplicantType.toUpperCase())));
        final List<IdValue<DocumentWithDescription>> existingFtpaDecisionAndReasonsDocuments = maybeFtpaDecisionAndReasonsDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionNoticeDocument = asylumCase.read(
            valueOf(String.format("FTPA_%s_NOTICE_DOCUMENT", ftpaApplicantType.toUpperCase())));
        final List<IdValue<DocumentWithDescription>> existingFtpaDecisionNoticeDocuments = maybeFtpaDecisionNoticeDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaDecisionDocuments = asylumCase.read(
            valueOf(String.format("ALL_FTPA_%s_DECISION_DOCS", ftpaApplicantType.toUpperCase())));
        final List<IdValue<DocumentWithMetadata>> existingAllFtpaDecisionDocuments = maybeFtpaDecisionDocuments.orElse(Collections.emptyList());

        List<DocumentWithMetadata> ftpaDecisionAndReasonsDocuments = new ArrayList<>();
        ftpaDecisionAndReasonsDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    existingFtpaDecisionAndReasonsDocuments,
                    DocumentTag.FTPA_DECISION_AND_REASONS
                )
        );

        ftpaDecisionAndReasonsDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    existingFtpaDecisionNoticeDocuments,
                    DocumentTag.FTPA_DECISION_AND_REASONS
                )
        );

        List<IdValue<DocumentWithMetadata>> allFtpaDecisionDocuments =
            documentsAppender.append(
                existingAllFtpaDecisionDocuments,
                ftpaDecisionAndReasonsDocuments
            );

        String ftpaDecisionOutcomeType = asylumCase.read(
            valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", ftpaApplicantType.toUpperCase())), String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));
        if (ftpaDecisionOutcomeType.equals("granted") || ftpaDecisionOutcomeType.equals("partiallyGranted")
            || ftpaDecisionOutcomeType.equals("reheardRule32") || ftpaDecisionOutcomeType.equals("reheardRule35")) {

            asylumCase.write(valueOf(String.format("IS_%s_FTPA_DECISION_VISIBLE_TO_ALL", ftpaApplicantType.toUpperCase())), YES);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_GROUNDS_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantType.toUpperCase())), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantType.toUpperCase())), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_EVIDENCE_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantType.toUpperCase())), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantType.toUpperCase())), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantType.toUpperCase())), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantType.toUpperCase())), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_EXPLANATION_VISIBLE_IN_DECIDED", ftpaApplicantType.toUpperCase())), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED", ftpaApplicantType.toUpperCase())), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantType.toUpperCase())), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantType.toUpperCase())), NO);
        }

        if (ftpaDecisionOutcomeType.equals("remadeRule32")) {

            String ftpaNewDecisionOfAppeal = asylumCase.read(
                valueOf(String.format("FTPA_%s_DECISION_REMADE_RULE_32", ftpaApplicantType.toUpperCase())), String.class)
                .orElse("");

            if (!ftpaNewDecisionOfAppeal.isEmpty()) {

                asylumCase.write(
                    valueOf(String.format("FTPA_%s_RJ_NEW_DECISION_OF_APPEAL", ftpaApplicantType.toUpperCase())),
                    StringUtils.capitalize(ftpaNewDecisionOfAppeal));
            }
        }
        asylumCase.write(
            valueOf(String.format("ALL_FTPA_%s_DECISION_DOCS", ftpaApplicantType.toUpperCase())),
            allFtpaDecisionDocuments);

        asylumCase.write(valueOf(String.format("FTPA_%s_DECISION_DATE", ftpaApplicantType.toUpperCase())),
            dateProvider.now().toString());
        asylumCase.write(valueOf(String.format("IS_FTPA_%s_DECIDED", ftpaApplicantType.toUpperCase())),
            YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
