package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT;

@Component
public class SendDecisionAndReasonsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final DocumentReceiver documentReceiver;

    public SendDecisionAndReasonsMidEventHandler(DocumentReceiver documentReceiver) {
        this.documentReceiver = documentReceiver;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.SEND_DECISION_AND_REASONS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Document finalDecisionAndReasonsDoc = asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class)
                .orElseThrow(() -> new IllegalStateException("finalDecisionAndReasonsDocument must be present"));

        if (!finalDecisionAndReasonsDoc.getDocumentFilename().endsWith(".pdf")) {
            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse =
                    new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The Decision and reasons document must be a PDF file");
            return asylumCasePreSubmitCallbackResponse;
        }
        documentReceiver.receive(
            finalDecisionAndReasonsDoc,
            "",
            DocumentTag.FINAL_DECISION_AND_REASONS_DOCUMENT
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}