package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_DECISION_AND_REASONS_DOC_UPLOAD;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CurrentFtpaDecisionResolver;

@Component
public class EditFtpaDecisionMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private final CurrentFtpaDecisionResolver currentFtpaDecisionResolver;
    static final String DOCUMENT_UPLOAD_PAGE_ID = "ftpaDecisionAndReasonsDocumentUploadPage";
    static final String NOTHING_CHANGED_ERROR =
        "You must change the decision or Decision and Reasons document to continue";
    static final String NOT_PDF_ERROR = "The Decision and Reasons document must be a PDF file";

    public EditFtpaDecisionMidEvent(
            CurrentFtpaDecisionResolver currentFtpaDecisionResolver
    ) {
        this.currentFtpaDecisionResolver = currentFtpaDecisionResolver;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.UPDATE_FTPA_DECISION
            && DOCUMENT_UPLOAD_PAGE_ID.equals(callback.getPageId());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (!(currentFtpaDecisionResolver.isDecisionChanged(asylumCase)) &&
                !(currentFtpaDecisionResolver.isDocumentUploaded(asylumCase))) {
            response.addError(NOTHING_CHANGED_ERROR);
            return response;
        }

        if (currentFtpaDecisionResolver.isDocumentUploaded(asylumCase)) {
            Optional<Document> uploadedDocument = asylumCase.read(FTPA_DECISION_AND_REASONS_DOC_UPLOAD, Document.class);

            if (uploadedDocument.isPresent()) {
                if (!uploadedDocument.get().getDocumentFilename().toLowerCase().endsWith(".pdf")) {
                    response.addError(NOT_PDF_ERROR);
                    return response;
                }

                asylumCase.write(FTPA_DECISION_AND_REASONS_DOC_UPLOAD, renamed(uploadedDocument.get(), asylumCase));
            }
        }

        return response;
    }

    private Document renamed(Document document, AsylumCase asylumCase) {
        return new Document(
            document.getDocumentUrl(),
            document.getDocumentBinaryUrl(),
            getDecisionAndReasonsFilename(asylumCase)
        );
    }

    private String getDecisionAndReasonsFilename(AsylumCase asylumCase) {
        String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Appeal reference number not present"));

        String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class)
            .orElseThrow(() -> new IllegalStateException("Appellant family name not present"));

        return appealReferenceNumber.replace("/", " ")
            + "-" + appellantFamilyName
            + "-Decision-and-reasons-UPDATED.pdf";
    }
}
