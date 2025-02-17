package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
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

        final String ftpaApplicantUpperCase = ftpaApplicantType.toUpperCase();

        List<DocumentWithMetadata> ftpaDecisionAndReasonsDocuments = new ArrayList<>();

        addFtpaDecisionAndReasonsDocument(asylumCase, ftpaApplicantType, ftpaDecisionAndReasonsDocuments);

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionNoticeDocument = asylumCase.read(
                valueOf(String.format("FTPA_%s_NOTICE_DOCUMENT", ftpaApplicantUpperCase)));
        final List<IdValue<DocumentWithDescription>> existingFtpaDecisionNoticeDocuments = maybeFtpaDecisionNoticeDocument.orElse(Collections.emptyList());

        ftpaDecisionAndReasonsDocuments.addAll(
                documentReceiver
                        .tryReceiveAll(
                                existingFtpaDecisionNoticeDocuments,
                                DocumentTag.FTPA_DECISION_AND_REASONS
                        )
        );

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaDecisionDocuments = asylumCase.read(
                valueOf(String.format("ALL_FTPA_%s_DECISION_DOCS", ftpaApplicantUpperCase)));
        final List<IdValue<DocumentWithMetadata>> existingAllFtpaDecisionDocuments = maybeFtpaDecisionDocuments.orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allFtpaDecisionDocuments =
                documentsAppender.append(
                        existingAllFtpaDecisionDocuments,
                        ftpaDecisionAndReasonsDocuments
                );

        asylumCase.write(
                valueOf(String.format("ALL_FTPA_%s_DECISION_DOCS", ftpaApplicantUpperCase)),
                allFtpaDecisionDocuments);

        String ftpaDecisionOutcomeType = asylumCase.read(
                        valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", ftpaApplicantUpperCase)), String.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));

        boolean isDlrmSetAside
                = featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false);

        if (isDlrmSetAside && ftpaDecisionOutcomeType.equals("reheardRule35")) {

            List<DocumentWithMetadata> ftpaSetAsideDocuments = new ArrayList<>();

            List<DocumentWithMetadata> ftpaSetAsideParsedDocuments = new ArrayList<>();

            addFtpaSetAsideDocuments(asylumCase, ftpaApplicantType, ftpaSetAsideDocuments,ftpaSetAsideParsedDocuments);

            final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaSetAsideDocuments = asylumCase.read(ALL_SET_ASIDE_DOCS);
            final List<IdValue<DocumentWithMetadata>> existingAllFtpaSetAsideDocuments = maybeFtpaSetAsideDocuments.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allFtpaSetAsideDocuments =
                documentsAppender.append(
                    existingAllFtpaSetAsideDocuments,
                    ftpaSetAsideParsedDocuments
                );

            asylumCase.write(ALL_SET_ASIDE_DOCS,allFtpaSetAsideDocuments);

        }

        if (ftpaDecisionOutcomeType.equals("granted") || ftpaDecisionOutcomeType.equals("partiallyGranted")
            || ftpaDecisionOutcomeType.equals("reheardRule32") || ftpaDecisionOutcomeType.equals("reheardRule35")) {

            asylumCase.write(valueOf(String.format("IS_%s_FTPA_DECISION_VISIBLE_TO_ALL", ftpaApplicantUpperCase)), YES);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_GROUNDS_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantUpperCase)), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantUpperCase)), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_EVIDENCE_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantUpperCase)), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantUpperCase)), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantUpperCase)), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantUpperCase)), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_EXPLANATION_VISIBLE_IN_DECIDED", ftpaApplicantUpperCase)), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED", ftpaApplicantUpperCase)), NO);

            asylumCase.write(valueOf(String.format("IS_FTPA_%s_DOCS_VISIBLE_IN_DECIDED", ftpaApplicantUpperCase)), YES);
            asylumCase.write(valueOf(String.format("IS_FTPA_%s_DOCS_VISIBLE_IN_SUBMITTED", ftpaApplicantUpperCase)), NO);
        }

        if (ftpaDecisionOutcomeType.equals("remadeRule31") || ftpaDecisionOutcomeType.equals("remadeRule32")) {

            String ftpaNewDecisionOfAppeal = asylumCase.read(
                    valueOf(String.format("FTPA_%s_DECISION_REMADE_RULE_32", ftpaApplicantUpperCase)), String.class)
                .orElse("");

            if (!ftpaNewDecisionOfAppeal.isEmpty()) {

                asylumCase.write(
                    valueOf(String.format("FTPA_%s_RJ_NEW_DECISION_OF_APPEAL", ftpaApplicantUpperCase)),
                    StringUtils.capitalize(ftpaNewDecisionOfAppeal));

                asylumCase.write(valueOf(String.format("IS_%s_FTPA_DECISION_VISIBLE_TO_ALL", ftpaApplicantUpperCase)), YES);
            }

            asylumCase.write(FTPA_FINAL_DECISION_REMADE_RULE_32, ftpaNewDecisionOfAppeal);
        }

        if (isDlrmSetAside && ftpaDecisionOutcomeType.equals("reheardRule35")) {

            asylumCase.write(
                valueOf(String.format("FTPA_%s_REASON_REHEARING", ftpaApplicantUpperCase)),
                "Set aside and to be reheard under rule 35");

        }

        asylumCase.write(valueOf(String.format("FTPA_%s_DECISION_DATE", ftpaApplicantUpperCase)), dateProvider.now().toString());
        asylumCase.write(valueOf(String.format("IS_FTPA_%s_DECIDED", ftpaApplicantUpperCase)),
            YES);

        String currentDecision =
            asylumCase.read(
                    valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", ftpaApplicantUpperCase)), String.class)
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

        ftpaDisplayService.setFtpaCaseDlrmFlag(
            asylumCase,
            featureToggler.getValue("dlrm-setaside-feature-flag", false)
        );

        addToFtpaList(asylumCase, ftpaApplicantType);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addFtpaDecisionAndReasonsDocument(AsylumCase asylumCase, String ftpaApplicantType,
                                                   List<DocumentWithMetadata> ftpaDecisionAndReasonsDocuments) {
        boolean isDlrmSetAside
            = featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false);

        if (isDlrmSetAside) {
            String ftpaAppellantRjDecisionOutcomeType = asylumCase
                .read(
                    valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE",
                        ftpaApplicantType.toUpperCase())),
                    String.class)
                .orElseThrow(
                    () -> new IllegalStateException(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE is not present",
                        ftpaApplicantType.toUpperCase())));

            if (ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
                || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())
                || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REFUSED.toString())
                || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.APPLICATION_NOT_ADMITTED.toString()
            )) {
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
            }


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

    private void addFtpaSetAsideDocuments(AsylumCase asylumCase, String ftpaApplicantType,
                                          List<DocumentWithMetadata> ftpaSetAsideDocuments,List<DocumentWithMetadata> ftpaSetAsideParsedDocuments) {

        final Document rule35Document =
            asylumCase
                .read(
                    valueOf(String.format("FTPA_R35_%s_DOCUMENT", ftpaApplicantType.toUpperCase())),
                    Document.class)
                .orElseThrow(
                    () -> new IllegalStateException(String.format("FTPA_R35_%s_DOCUMENT",
                   ftpaApplicantType.toUpperCase())));


        ftpaSetAsideDocuments.add(
            documentReceiver
                .receive(
                    rule35Document,
                    "",
                    DocumentTag.FTPA_SET_ASIDE
                )
        );


        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaSetAsideNoticeDocuments = asylumCase.read(
            valueOf(String.format("FTPA_%s_R35_NOTICE_DOCUMENT", ftpaApplicantType.toUpperCase())));
        final List<IdValue<DocumentWithDescription>> existingAllFtpaSetAsideNoticeDocuments = maybeFtpaSetAsideNoticeDocuments.orElse(Collections.emptyList());

        ftpaSetAsideDocuments.addAll(
                documentReceiver
                        .tryReceiveAll(
                                existingAllFtpaSetAsideNoticeDocuments,
                                DocumentTag.FTPA_SET_ASIDE
                        ));


        ftpaSetAsideParsedDocuments.addAll(ftpaSetAsideDocuments.stream().map(x -> documentReceiver.receive(x.getDocument(), "", DocumentTag.FTPA_SET_ASIDE)).collect(Collectors.toList()));



    }

    private void addToFtpaList(AsylumCase asylumCase, String ftpaApplicantType) {
        boolean isDlrmSetAside = featureToggler.getValue("dlrm-setaside-feature-flag", false);

        if (isDlrmSetAside) {
            Optional<List<IdValue<FtpaApplications>>> maybeExistingFtpaApplictions =
                asylumCase.read(FTPA_LIST);

            List<IdValue<FtpaApplications>> existingFtpaApplictions = maybeExistingFtpaApplictions.orElse(emptyList());

            if (!existingFtpaApplictions.isEmpty()) {
                FtpaApplications ftpaApplication = existingFtpaApplictions.stream()
                    .filter(ftpaApp -> ftpaApp.getValue().getFtpaApplicant().equals(ftpaApplicantType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(ftpaApplicantType + " application is not present in FTPA list"))
                    .getValue();

                ftpaDisplayService.mapFtpaDecision(false, asylumCase, ftpaApplicantType.toUpperCase(), ftpaApplication);

                asylumCase.write(FTPA_LIST, existingFtpaApplictions);
            }

            asylumCase.write(IS_FTPA_LIST_VISIBLE, YES);
        }
    }
}
