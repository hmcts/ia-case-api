package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadRespondentEvidenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadRespondentEvidenceHandler(
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
               && callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<DocumentWithMetadata> respondentEvidence =
            asylumCase
                .getRespondentEvidence()
                .orElseThrow(() -> new IllegalStateException("respondentEvidence is not present"))
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.RESPONDENT_EVIDENCE))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        final List<IdValue<DocumentWithMetadata>> existingRespondentDocuments =
            asylumCase
                .getRespondentDocuments()
                .orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
            documentsAppender.append(existingRespondentDocuments, respondentEvidence);

        asylumCase.setRespondentDocuments(allRespondentDocuments);

        asylumCase.clearRespondentEvidence();

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
