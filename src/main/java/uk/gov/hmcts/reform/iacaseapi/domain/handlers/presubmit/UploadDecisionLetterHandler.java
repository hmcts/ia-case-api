package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadDecisionLetterHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadDecisionLetterHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
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
               && callback.getCaseDetails().getState() != State.APPEAL_STARTED;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean legalRepDocumentsContainHoDecisionLetter = false;

        Optional<List<IdValue<DocumentWithDescription>>> maybeNoticeOfDecision =
            asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);

        List<DocumentWithMetadata> noticeOfDecision =
            maybeNoticeOfDecision
                .orElse(emptyList())
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.HO_DECISION_LETTER))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingLegalRepDocuments =
            asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> existingLegalRepDocuments =
            maybeExistingLegalRepDocuments.orElse(emptyList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingLegalRepDocuments =
                asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> existingLegalRepDocuments =
                maybeExistingLegalRepDocuments.orElse(emptyList());

        if (!noticeOfDecision.isEmpty()) {
            existingLegalRepDocuments = existingLegalRepDocuments.stream().filter(
                            doc -> doc.getValue().getTag() != DocumentTag.HO_DECISION_LETTER)
                    .toList();

            List<IdValue<DocumentWithMetadata>> allLegalRepDocuments =
                    documentsAppender.prepend(existingLegalRepDocuments, noticeOfDecision);

            asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepDocuments);
        }

        if (legalRepDocumentsContainHoDecisionLetter) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        if (!noticeOfDecision.isEmpty()) {
            List<IdValue<DocumentWithMetadata>> allLegalRepDocuments =
                documentsAppender.prepend(existingLegalRepDocuments, noticeOfDecision);
            asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepDocuments);
        }


        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
