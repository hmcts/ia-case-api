package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.COURT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_REMITTAL_DECISION_DOC;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class MarkAppealAsRemittedUploadDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public MarkAppealAsRemittedUploadDecisionHandler() {
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.MARK_APPEAL_AS_REMITTED);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Document mayBeDecisionDocument = asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class)
            .orElseThrow(() -> new IllegalStateException("uploadRemittalDecisionDoc is not present"));

        Document decisionDocumnent = new Document(mayBeDecisionDocument.getDocumentUrl(),
            mayBeDecisionDocument.getDocumentBinaryUrl(), getRemittalDecisionFilename(asylumCase));

        asylumCase.write(UPLOAD_REMITTAL_DECISION_DOC, decisionDocumnent);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }


    private String getRemittalDecisionFilename(AsylumCase asylumCase) {

        String courtReferenceNumber = asylumCase.read(COURT_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Court reference number not present"));

        return courtReferenceNumber
            + "-Decision-to-remit.pdf";
    }
}
