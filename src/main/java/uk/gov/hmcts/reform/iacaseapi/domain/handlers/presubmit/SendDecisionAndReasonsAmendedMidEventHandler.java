package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class SendDecisionAndReasonsAmendedMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final String decisionAndReasonsFinalPdfFilename;

    public SendDecisionAndReasonsAmendedMidEventHandler (
        @Value("${decisionAndReasonsAmendedPdf.fileName}") String decisionAndReasonsAmendedPdfFilename
    ){
        this.decisionAndReasonsFinalPdfFilename = decisionAndReasonsAmendedPdfFilename;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                && callback.getPageId().equals("decisionAndReasonsDocumentUploadPage");
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Optional<Document> decisionAndReasonsDoc = asylumCase.read(DECISION_AND_REASON_DOC_UPLOAD, Document.class);

        if (decisionAndReasonsDoc.isEmpty()) {
            throw new IllegalStateException("amendedDecisionAndReasonsDocument must be present");
        }

        if (!decisionAndReasonsDoc.get().getDocumentFilename().endsWith(".pdf")) {
            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse =
                    new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The Decision and reasons document must be a PDF file");
            return asylumCasePreSubmitCallbackResponse;
        }

        Document previousDecisionAndReasonsDoc = decisionAndReasonsDoc.get();

        Document amendedDecisionAndReasonsDoc = new Document(previousDecisionAndReasonsDoc.getDocumentUrl(), previousDecisionAndReasonsDoc.getDocumentBinaryUrl(),getDecisionAndReasonsFilename(asylumCase));

        asylumCase.write(DECISION_AND_REASON_DOC_UPLOAD, amendedDecisionAndReasonsDoc);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getDecisionAndReasonsFilename(AsylumCase asylumCase) {

        String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Appeal reference number not present"));

        String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class)
            .orElseThrow(() -> new IllegalStateException("appellant family name not present"));

        return appealReferenceNumber.replace("/", " ")
               + "-"
               + appellantFamilyName
               + "-"
               + decisionAndReasonsFinalPdfFilename
               + "-"
               +"AMENDED.pdf";
    }

}
