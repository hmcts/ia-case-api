package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALL_SET_ASIDE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_32;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class UpdateTribunalDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final Appender<DecisionAndReasons> decisionAndReasonsAppender;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final FeatureToggler featureToggler;

    public UpdateTribunalDecisionHandler(
            DateProvider dateProvider,
            Appender<DecisionAndReasons> decisionAndReasonsAppender,
            DocumentReceiver documentReceiver,
            DocumentsAppender documentsAppender,
            FeatureToggler featureToggler
    ) {
        this.dateProvider = dateProvider;
        this.decisionAndReasonsAppender = decisionAndReasonsAppender;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isDecisionRule31(asylumCase)) {
            DynamicList updateTribunalDecisionValue = asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("typesOfUpdateTribunalDecision is not present"));

            asylumCase.write(UPDATED_APPEAL_DECISION, StringUtils.capitalize(updateTribunalDecisionValue.getValue().getCode()));

            final DecisionAndReasons newDecisionAndReasons =
                    DecisionAndReasons.builder()
                            .updatedDecisionDate(dateProvider.now().toString())
                            .build();

            if (asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
                    .map(flag -> flag.equals(YesOrNo.YES)).orElse(false)) {

                Document correctedDecisionAndReasonsDoc = asylumCase
                        .read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class)
                        .orElseThrow(() -> new IllegalStateException("decisionAndReasonDocsUpload is not present"));

                String summariseChanges = asylumCase
                        .read(SUMMARISE_TRIBUNAL_DECISION_AND_REASONS_DOCUMENT, String.class)
                        .orElseThrow(() -> new IllegalStateException("summariseTribunalDecisionAndReasonsDocument is not present"));

                newDecisionAndReasons.setDocumentAndReasonsDocument(correctedDecisionAndReasonsDoc);
                newDecisionAndReasons.setDateDocumentAndReasonsDocumentUploaded(dateProvider.now().toString());
                newDecisionAndReasons.setSummariseChanges(summariseChanges);
            }

            Optional<List<IdValue<DecisionAndReasons>>> maybeExistingDecisionAndReasons =
                    asylumCase.read(CORRECTED_DECISION_AND_REASONS);
            List<IdValue<DecisionAndReasons>> allCorrectedDecisions =
                    decisionAndReasonsAppender.append(newDecisionAndReasons, maybeExistingDecisionAndReasons.orElse(emptyList()));

            asylumCase.write(CORRECTED_DECISION_AND_REASONS, allCorrectedDecisions);

            final Optional<Document> decisionAndReasonsDoc = asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class);

            final Optional<List<IdValue<DocumentWithMetadata>>> maybeDecisionAndReasonsDocuments = asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS);

            final List<IdValue<DocumentWithMetadata>> existingDecisionAndReasonsDocuments  = maybeDecisionAndReasonsDocuments.orElse(Collections.emptyList());


            if (decisionAndReasonsDoc.isPresent()) {
                DocumentWithMetadata updatedDecisionAndReasonsDocument = documentReceiver.receive(
                    decisionAndReasonsDoc.get(),
                    "",
                    DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF
                );

                List<IdValue<DocumentWithMetadata>> newUpdateTribunalDecisionDocs = documentsAppender.append(
                    existingDecisionAndReasonsDocuments,
                    singletonList(updatedDecisionAndReasonsDocument)
                );

                asylumCase.write(FINAL_DECISION_AND_REASONS_DOCUMENTS, newUpdateTribunalDecisionDocs);
            }

            asylumCase.write(UPDATE_TRIBUNAL_DECISION_DATE, dateProvider.now().toString());

            YesOrNo isDecisionAndReasonDocumentBeingUpdated = asylumCase.read(AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
                .orElse(NO);

            if (isDecisionAndReasonDocumentBeingUpdated.equals(NO)) {
                if (decisionAndReasonsDoc.isPresent()) {
                    asylumCase.clear(DECISION_AND_REASON_DOCS_UPLOAD);
                    asylumCase.clear(SUMMARISE_TRIBUNAL_DECISION_AND_REASONS_DOCUMENT);
                }
            }
        } else if (isDecisionRule32(asylumCase)) {

            List<DocumentWithMetadata> ftpaSetAsideDocuments = new ArrayList<>();

            final Document rule32Document =
                asylumCase
                    .read(RULE_32_NOTICE_DOCUMENT,Document.class)
                    .orElseThrow(
                        () -> new IllegalStateException("Rule 32 notice document is not present"));

            ftpaSetAsideDocuments.add(
                documentReceiver
                    .receive(
                        rule32Document,
                        "",
                        DocumentTag.FTPA_SET_ASIDE
                        )
            );

            final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaSetAsideDocuments = asylumCase.read(ALL_SET_ASIDE_DOCS);
            final List<IdValue<DocumentWithMetadata>> existingAllFtpaSetAsideDocuments = maybeFtpaSetAsideDocuments.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allFtpaSetAsideDocuments =
                documentsAppender.append(
                    existingAllFtpaSetAsideDocuments,
                    ftpaSetAsideDocuments
                );

            asylumCase.write(ALL_SET_ASIDE_DOCS,allFtpaSetAsideDocuments);

            asylumCase.write(UPDATE_TRIBUNAL_DECISION_DATE_RULE_32, dateProvider.now().toString());
            asylumCase.write(REASON_REHEARING_RULE_32, "Set aside and to be reheard under rule 32");
            setFtpaReheardCaseFlag(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDecisionRule31(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)
                .map(type -> type.equals(UNDER_RULE_31)).orElse(false);
    }

    private boolean isDecisionRule32(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)
                .map(type -> type.equals(UNDER_RULE_32)).orElse(false);
    }

    private void setFtpaReheardCaseFlag(AsylumCase asylumCase) {
        boolean isReheardAppealEnabled = featureToggler.getValue("reheard-feature", false);
        asylumCase.write(AsylumCaseFieldDefinition.IS_REHEARD_APPEAL_ENABLED,
                isReheardAppealEnabled ? YesOrNo.YES : YesOrNo.NO);

        if (isReheardAppealEnabled) {
            asylumCase.write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YES);
            asylumCase.write(STITCHING_STATUS,"");
        }
    }
}