package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class CreateCaseSummaryHandler implements PreSubmitCallbackHandler<AsylumCase> {

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
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CREATE_CASE_SUMMARY;
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

        final Document caseSummaryDocument =
            asylumCase
                .read(CASE_SUMMARY_DOCUMENT, Document.class)
                .orElseThrow(() -> new IllegalStateException("caseSummaryDocument is not present"));

        final String caseSummaryDescription =
            asylumCase
                .read(CASE_SUMMARY_DESCRIPTION, String.class)
                .orElse("");

        DocumentWithMetadata caseSummaryDocumentWithMetadata =
            documentReceiver.receive(
                caseSummaryDocument,
                caseSummaryDescription,
                DocumentTag.CASE_SUMMARY
            );

        Optional<YesOrNo> caseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS);

        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments =
            caseFlagSetAsideReheardExists.isPresent() && caseFlagSetAsideReheardExists.get() == YesOrNo.YES
                ? asylumCase.read(REHEARD_HEARING_DOCUMENTS)
                : asylumCase.read(HEARING_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> hearingDocuments =
            maybeHearingDocuments.orElse(emptyList());

        List<IdValue<DocumentWithMetadata>> allHearingDocuments =
            documentsAppender.append(
                hearingDocuments,
                singletonList(caseSummaryDocumentWithMetadata),
                DocumentTag.CASE_SUMMARY
            );

        if (caseFlagSetAsideReheardExists.isPresent() && caseFlagSetAsideReheardExists.get() == YesOrNo.YES) {
            asylumCase.write(REHEARD_HEARING_DOCUMENTS, allHearingDocuments);
        } else {
            asylumCase.write(HEARING_DOCUMENTS, allHearingDocuments);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
