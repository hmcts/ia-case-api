package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadRespondentEvidenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentsAppender documentsAppender;

    public UploadRespondentEvidenceHandler(
        DocumentsAppender documentsAppender
    ) {
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE;
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

        final List<IdValue<DocumentWithMetadata>> respondentDocuments =
            asylumCase
                .getRespondentDocuments()
                .orElse(Collections.emptyList());

        final List<IdValue<DocumentWithDescription>> respondentEvidence =
            asylumCase
                .getRespondentEvidence()
                .orElseThrow(() -> new IllegalStateException("respondentEvidence is not present"));

        List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
            documentsAppender.append(respondentDocuments, respondentEvidence);

        asylumCase.setRespondentDocuments(allRespondentDocuments);

        asylumCase.clearRespondentEvidence();

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
