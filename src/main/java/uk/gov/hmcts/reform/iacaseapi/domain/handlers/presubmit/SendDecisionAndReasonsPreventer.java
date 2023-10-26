package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_AND_REASONS_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class SendDecisionAndReasonsPreventer implements PreSubmitCallbackHandler<AsylumCase> {

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.SEND_DECISION_AND_REASONS;
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

        YesOrNo decisionAndReasonsAvailable =
                asylumCase.read(DECISION_AND_REASONS_AVAILABLE, YesOrNo.class)
                        .orElseThrow(() -> new IllegalStateException("decisionAndReasonsAvailable must be present complete decision and reasons"));

        if (decisionAndReasonsAvailable.equals(YesOrNo.NO)) {

            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse =
                    new PreSubmitCallbackResponse<>(asylumCase);

            asylumCasePreSubmitCallbackResponse.addError("You must generate the Decision and reasons draft before completing the Decision and reasons");

            return asylumCasePreSubmitCallbackResponse;
        }

        if (decisionAndReasonsAvailable.equals(YesOrNo.YES)) {

            final Document finalDecisionAndReasonsDoc = asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class)
                    .orElseThrow(
                            () -> new IllegalStateException("finalDecisionAndReasonsDocument must be present"));

            if (!finalDecisionAndReasonsDoc.getDocumentFilename().endsWith(".pdf")) {
                throw new IllegalStateException("The Decision and reasons document must be a PDF file");
            }

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
