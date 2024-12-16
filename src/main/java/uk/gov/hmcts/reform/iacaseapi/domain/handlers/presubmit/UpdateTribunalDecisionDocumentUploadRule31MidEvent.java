package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateTribunalDecisionDocumentUploadRule31MidEvent implements PreSubmitCallbackHandler<AsylumCase> {

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

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        final Optional<Document> decisionAndReasonsDoc = asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class);

        YesOrNo isDecisionAndReasonDocumentBeingUpdated = asylumCase.read(AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
            .orElse(NO);
        Optional<DynamicList> optionalTypesOfUpdateTribunalDecision = asylumCase.read(AsylumCaseFieldDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class);

        if (isDecisionAndReasonDocumentBeingUpdated.equals(YES)) {

            if (decisionAndReasonsDoc.isPresent()) {
                if (!decisionAndReasonsDoc.get().getDocumentFilename().endsWith(".pdf")) {
                    response.addError("The Decision and reasons document must be a PDF file");
                    return response;
                }

                Document previousDecisionAndReasonsDoc = decisionAndReasonsDoc.get();

                Document updatedDecisionAndReasonsDoc = new Document(previousDecisionAndReasonsDoc.getDocumentUrl(), previousDecisionAndReasonsDoc.getDocumentBinaryUrl(), getDecisionAndReasonsFilename(asylumCase));

                asylumCase.write(DECISION_AND_REASON_DOCS_UPLOAD, updatedDecisionAndReasonsDoc);
            }
            return new PreSubmitCallbackResponse<>(asylumCase);

        } else if (isDecisionAndReasonDocumentBeingUpdated.equals(NO) &&
            optionalTypesOfUpdateTribunalDecision.isPresent() &&
            optionalTypesOfUpdateTribunalDecision.get().getValue().getLabel().equals("No")) {
            response.addError("You must update the decision or the Decision and Reasons document to continue.");
        }

        return response;
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
                + "Decision-and-reasons"
                + "-"
                + "UPDATED.pdf";
    }
}