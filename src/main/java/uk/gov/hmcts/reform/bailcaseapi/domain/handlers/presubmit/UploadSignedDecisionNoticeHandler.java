package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_STATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@Component
public class UploadSignedDecisionNoticeHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final DateProvider dateProvider;

    public UploadSignedDecisionNoticeHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        DateProvider dateProvider
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Document maybeSignedDecisionNotice = bailCase.read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class)
            .orElseThrow(() -> new IllegalStateException("signedDecisionNotice is not present"));

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingTribunalDocuments =
            bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA);

        final List<IdValue<DocumentWithMetadata>> existingTribunalDocuments =
            maybeExistingTribunalDocuments.orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allTribunalDocuments = new ArrayList<>();

        documentReceiver.tryReceive(
                new DocumentWithDescription(maybeSignedDecisionNotice, ""), DocumentTag.SIGNED_DECISION_NOTICE)
            .ifPresent(signedDecisionNoticeDocumentWithMetadata ->
                           allTribunalDocuments.addAll(documentsAppender.append(
                               existingTribunalDocuments, List.of(signedDecisionNoticeDocumentWithMetadata)
                           )));

        allTribunalDocuments
            .removeIf(document -> document.getValue().getTag().equals(DocumentTag.BAIL_DECISION_UNSIGNED));

        bailCase.write(TRIBUNAL_DOCUMENTS_WITH_METADATA, allTribunalDocuments);
        bailCase.write(OUTCOME_DATE, dateProvider.nowWithTime().toString());
        bailCase.write(OUTCOME_STATE, State.DECISION_DECIDED);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
