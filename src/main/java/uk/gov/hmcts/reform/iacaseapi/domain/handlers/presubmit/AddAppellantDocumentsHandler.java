package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class AddAppellantDocumentsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentsAppender documentsAppender;

    public AddAppellantDocumentsHandler(DocumentsAppender documentsAppender) {
        this.documentsAppender = documentsAppender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.SUBMIT_REASONS_FOR_APPEAL
                || callback.getEvent() == Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        List<DocumentWithMetadata> appellantDocuments = concat(
                getReasonsForAppealEvidence(asylumCase),
                getClarifyQuestionsEvidence(asylumCase)
        ).collect(Collectors.toList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingLegalRepDocuments =
                asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingLegalRepDocuments =
                maybeExistingLegalRepDocuments.orElse(emptyList());

        List<IdValue<DocumentWithMetadata>> allLegalRepDocuments =
                documentsAppender.append(existingLegalRepDocuments, appellantDocuments);

        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepDocuments);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Stream<DocumentWithMetadata> getReasonsForAppealEvidence(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<DocumentWithMetadata>>>read(REASONS_FOR_APPEAL_DOCUMENTS)
                .map(documents -> documents.stream().map(IdValue::getValue))
                .orElse(Stream.empty());
    }

    private Stream<DocumentWithMetadata> getClarifyQuestionsEvidence(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<ClarifyingQuestionAnswer>>>read(CLARIFYING_QUESTIONS_ANSWERS)
                .map(clarifyingQuestions -> clarifyingQuestions.stream().flatMap(evidenceOnEachQuestionToList()))
                .orElse(Stream.empty());
    }

    private Function<IdValue<ClarifyingQuestionAnswer>, Stream<? extends DocumentWithMetadata>> evidenceOnEachQuestionToList() {
        return clarifyingQuestion ->
                Optional.ofNullable(clarifyingQuestion.getValue().getSupportingEvidence())
                        .map(supportingEvidenceList -> supportingEvidenceList.stream()
                                .map(supportingEvidenceToDocumentWithMetadata(clarifyingQuestion))
                        )
                        .orElse(Stream.empty());
    }

    private Function<IdValue<Document>, DocumentWithMetadata> supportingEvidenceToDocumentWithMetadata(IdValue<ClarifyingQuestionAnswer> clarifyingQuestion) {
        return supportingEvidence ->
                new DocumentWithMetadata(
                        supportingEvidence.getValue(),
                        "Clarifying question evidence",
                        clarifyingQuestion.getValue().getDateResponded(),
                        DocumentTag.ADDITIONAL_EVIDENCE
                );
    }
}
