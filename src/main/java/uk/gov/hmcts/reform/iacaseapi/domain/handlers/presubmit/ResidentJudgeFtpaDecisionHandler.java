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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FtpaDisplayService;

@Component
public class ResidentJudgeFtpaDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String DLRM_SETASIDE_FEATURE_FLAG = "dlrm-setaside-feature-flag";

    public static final String FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION =
        "ftpaDecisionsAndReasonsDocumentDescription";
    private final DateProvider dateProvider;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final FtpaDisplayService ftpaDisplayService;
    private final FeatureToggler featureToggler;

    public ResidentJudgeFtpaDecisionHandler(
        DateProvider dateProvider,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        FtpaDisplayService ftpaDisplayService,
        FeatureToggler featureToggler
    ) {
        this.dateProvider = dateProvider;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.ftpaDisplayService = ftpaDisplayService;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
            || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION);
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

        List<DocumentWithMetadata> ftpaDecisionAndReasonsDocuments = new ArrayList<>();

        addFtpaDecisionAndReasonsDocument(asylumCase, ftpaApplicantType, ftpaDecisionAndReasonsDocuments);

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionNoticeDocument = asylumCase.read(
            valueOf(String.format("FTPA_%s_NOTICE_DOCUMENT", ftpaApplicantType.toUpperCase())));
        final List<IdValue<DocumentWithDescription>> existingFtpaDecisionNoticeDocuments = maybeFtpaDecisionNoticeDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaDecisionDocuments = asylumCase.read(
            valueOf(String.format("ALL_FTPA_%s_DECISION_DOCS", ftpaApplicantType.toUpperCase())));
        final List<IdValue<DocumentWithMetadata>> existingAllFtpaDecisionDocuments = maybeFtpaDecisionDocuments.orElse(Collections.emptyList());


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
            || ftpaDecisionOutcomeType.equals("reheardRule35")) {

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

        if (ftpaDecisionOutcomeType.equals("remadeRule31")|| ftpaDecisionOutcomeType.equals("remadeRule32") ) {

            String ftpaNewDecisionOfAppeal = asylumCase.read(
                    valueOf(String.format("FTPA_%s_DECISION_REMADE_RULE_32", ftpaApplicantType.toUpperCase())), String.class)
                .orElse("");

            if (!ftpaNewDecisionOfAppeal.isEmpty()) {

                asylumCase.write(
                    valueOf(String.format("FTPA_%s_RJ_NEW_DECISION_OF_APPEAL", ftpaApplicantType.toUpperCase())),
                    StringUtils.capitalize(ftpaNewDecisionOfAppeal));

                asylumCase.write(valueOf(String.format("IS_%s_FTPA_DECISION_VISIBLE_TO_ALL", ftpaApplicantType.toUpperCase())), YES);
            }

            asylumCase.write(FTPA_FINAL_DECISION_REMADE_RULE_32, ftpaNewDecisionOfAppeal);
        }
        asylumCase.write(
            valueOf(String.format("ALL_FTPA_%s_DECISION_DOCS", ftpaApplicantType.toUpperCase())),
            allFtpaDecisionDocuments);

        asylumCase.write(valueOf(String.format("FTPA_%s_DECISION_DATE", ftpaApplicantType.toUpperCase())),
            dateProvider.now().toString());
        asylumCase.write(valueOf(String.format("IS_FTPA_%s_DECIDED", ftpaApplicantType.toUpperCase())),
            YES);

        String currentDecision =
            asylumCase.read(
                    valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", ftpaApplicantType.toUpperCase())), String.class)
                .orElse("");

        String ftpaFirstDecision =
            asylumCase.read(FTPA_FIRST_DECISION)
                .orElse("").toString();

        ftpaDisplayService.handleFtpaDecisions(
            asylumCase,
            currentDecision,
            ftpaFirstDecision
        );

        ftpaDisplayService.setFtpaCaseFlag(
            asylumCase,
            featureToggler.getValue("reheard-feature", false),
            currentDecision
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addFtpaDecisionAndReasonsDocument(AsylumCase asylumCase, String ftpaApplicantType,
                                                   List<DocumentWithMetadata> ftpaDecisionAndReasonsDocuments) {
        YesOrNo isDlrmSetAsideEnabled
            = featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false) ? YES : NO;

        if (isDlrmSetAsideEnabled.equals(YES)) {
            final Document caseArgumentDocument =
                asylumCase
                    .read(
                        valueOf(String.format("FTPA_APPLICATION_%s_DOCUMENT",
                            ftpaApplicantType.toUpperCase())),
                        Document.class)
                    .orElseThrow(
                        () -> new IllegalStateException(String.format("FTPA_APPLICATION_%s_DOCUMENT is not present",
                            ftpaApplicantType.toUpperCase())));

            ftpaDecisionAndReasonsDocuments.add(
                documentReceiver
                    .receive(
                        caseArgumentDocument,
                        FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION,
                        DocumentTag.FTPA_DECISION_AND_REASONS
                    )
            );
        } else {
            final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionAndReasonsDocument = asylumCase.read(
                valueOf(String.format("FTPA_%s_DECISION_DOCUMENT", ftpaApplicantType.toUpperCase())));
            final List<IdValue<DocumentWithDescription>> existingFtpaDecisionAndReasonsDocuments = maybeFtpaDecisionAndReasonsDocument.orElse(Collections.emptyList());

            ftpaDecisionAndReasonsDocuments.addAll(
                documentReceiver
                    .tryReceiveAll(
                        existingFtpaDecisionAndReasonsDocuments,
                        DocumentTag.FTPA_DECISION_AND_REASONS
                    )
            );
        }
    }
}
