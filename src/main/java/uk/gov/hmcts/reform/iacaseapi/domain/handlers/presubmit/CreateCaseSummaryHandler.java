package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class CreateCaseSummaryHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public CreateCaseSummaryHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CREATE_CASE_SUMMARY;
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

        final Document caseSummaryDocument =
            caseDataMap
                .get(CASE_SUMMARY_DOCUMENT, Document.class)
                .orElseThrow(() -> new IllegalStateException("caseSummaryDocument is not present"));

        final String caseSummaryDescription =
            caseDataMap
                .get(CASE_SUMMARY_DESCRIPTION, String.class)
                .orElse("");

        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments =
                caseDataMap.get(HEARING_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> hearingDocuments =
            maybeHearingDocuments.orElse(emptyList());

        DocumentWithMetadata caseSummaryDocumentWithMetadata =
            documentReceiver.receive(
                caseSummaryDocument,
                caseSummaryDescription,
                DocumentTag.CASE_SUMMARY
            );

        List<IdValue<DocumentWithMetadata>> allHearingDocuments =
            documentsAppender.append(
                hearingDocuments,
                singletonList(caseSummaryDocumentWithMetadata),
                DocumentTag.CASE_SUMMARY
            );

        caseDataMap.write(HEARING_DOCUMENTS, allHearingDocuments);

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
