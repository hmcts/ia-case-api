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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
               && (callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE || callback.getEvent() == Event.UPLOAD_HOME_OFFICE_BUNDLE);
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

        Optional<List<IdValue<DocumentWithDescription>>> maybeRespondentEvidence =
                asylumCase.read(RESPONDENT_EVIDENCE).isPresent() ? asylumCase.read(RESPONDENT_EVIDENCE) : asylumCase.read(HOME_OFFICE_BUNDLE);

        List<DocumentWithMetadata> respondentEvidence =
            maybeRespondentEvidence
                .orElseThrow(() -> new IllegalStateException("respondentEvidence is not present"))
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.RESPONDENT_EVIDENCE))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingRespondentDocuments =
                asylumCase.read(RESPONDENT_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingRespondentDocuments =
                maybeExistingRespondentDocuments.orElse(emptyList());

        List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
            documentsAppender.append(existingRespondentDocuments, respondentEvidence);

        asylumCase.write(RESPONDENT_DOCUMENTS, allRespondentDocuments);

        asylumCase.clear(RESPONDENT_EVIDENCE);

        asylumCase.write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
