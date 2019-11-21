package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOADED_HOME_OFFICE_APPEAL_RESPONSE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UploadHomeOfficeAppealResponsePreparer implements PreSubmitCallbackHandler<AsylumCase> {


    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && Arrays.asList(Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE, Event.ADD_APPEAL_RESPONSE).contains(callback.getEvent());
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

        final Optional<YesOrNo> uploadAppealResponseActionAvailable = asylumCase.read(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE);

        if (callback.getEvent() == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE && uploadAppealResponseActionAvailable.isPresent() && uploadAppealResponseActionAvailable.get() == YesOrNo.NO) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You cannot upload more documents until your response has been reviewed");
            return asylumCasePreSubmitCallbackResponse;
        }

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingRespondentDocuments =
            asylumCase.read(RESPONDENT_DOCUMENTS);


        final List<String> existingDocuments = maybeExistingRespondentDocuments.orElse(emptyList()).stream()
            .map(IdValue::getValue)
            .filter(documentWithMetadata -> documentWithMetadata.getTag() == DocumentTag.APPEAL_RESPONSE)
            .map(documentWithMetadata -> documentWithMetadata.getDocument().getDocumentFilename())
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (String url : existingDocuments) {
            sb.append("- ").append(url).append("\r\n");
        }

        asylumCase.write(UPLOADED_HOME_OFFICE_APPEAL_RESPONSE_DOCS, sb.length() != 0 ? sb.toString() : "- None");

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
