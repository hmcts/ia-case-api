package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_RESPONSE_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPEAL_RESPONSE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPEAL_RESPONSE_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPEAL_RESPONSE_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadHomeOfficeAppealResponseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadHomeOfficeAppealResponseHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
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
               && callback.getEvent() == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE;
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

        final Optional<Document> maybeDocument = asylumCase
                .read(HOME_OFFICE_APPEAL_RESPONSE_DOCUMENT);

        final Document appealResponseDocument =
                maybeDocument.orElseThrow(() -> new IllegalStateException("appealResponseDocument is not present"));

        final String appealResponseDescription =
            asylumCase
                .read(HOME_OFFICE_APPEAL_RESPONSE_DESCRIPTION, String.class)
                .orElse("");

        final Optional<List<IdValue<DocumentWithDescription>>> maybeAppealResponseEvidence =
                asylumCase.read(HOME_OFFICE_APPEAL_RESPONSE_EVIDENCE);

        final List<IdValue<DocumentWithDescription>> appealResponseEvidence =
            maybeAppealResponseEvidence.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeRespondentDocuments =
            asylumCase.read(RESPONDENT_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> respondentDocuments = maybeRespondentDocuments.orElse(Collections.emptyList());

        final List<DocumentWithMetadata> appealResponseDocuments = new ArrayList<>();

        appealResponseDocuments.add(
            documentReceiver
                .receive(
                    appealResponseDocument,
                    appealResponseDescription,
                    DocumentTag.APPEAL_RESPONSE
                )
        );

        appealResponseDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    appealResponseEvidence,
                    DocumentTag.APPEAL_RESPONSE
                )
        );

        List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
            documentsAppender.append(
                respondentDocuments,
                appealResponseDocuments,
                DocumentTag.APPEAL_RESPONSE
            );

        asylumCase.write(RESPONDENT_DOCUMENTS, allRespondentDocuments);

        asylumCase.write(APPEAL_RESPONSE_AVAILABLE, YES);

        asylumCase.write(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE, NO);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
