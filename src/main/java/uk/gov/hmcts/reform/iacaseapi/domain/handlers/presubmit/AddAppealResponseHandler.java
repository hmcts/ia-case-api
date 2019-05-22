package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
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
public class AddAppealResponseHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public AddAppealResponseHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.ADD_APPEAL_RESPONSE;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        final Optional<Document> maybeDocument = caseDataMap
                .get(APPEAL_RESPONSE_DOCUMENT);

        final Document appealResponseDocument =
                maybeDocument.orElseThrow(() -> new IllegalStateException("appealResponseDocument is not present"));

        final String appealResponseDescription =
            caseDataMap
                .get(APPEAL_RESPONSE_DESCRIPTION, String.class)
                .orElse("");

        final Optional<List<IdValue<DocumentWithDescription>>> maybeAppealResponseEvidence =
                caseDataMap.get(APPEAL_RESPONSE_EVIDENCE);

        final List<IdValue<DocumentWithDescription>> appealResponseEvidence =
            maybeAppealResponseEvidence.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeRespondentDocuments =
            caseDataMap.get(RESPONDENT_DOCUMENTS);

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

        caseDataMap.write(RESPONDENT_DOCUMENTS, allRespondentDocuments);

        caseDataMap.write(APPEAL_RESPONSE_AVAILABLE, YES);

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
