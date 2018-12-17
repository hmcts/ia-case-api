package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
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
public class AddAppealResponseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public AddAppealResponseHandler(
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
               && callback.getEvent() == Event.ADD_APPEAL_RESPONSE;
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

        final Document appealResponseDocument =
            asylumCase
                .getAppealResponseDocument()
                .orElseThrow(() -> new IllegalStateException("appealResponseDocument is not present"));

        final String appealResponseDescription =
            asylumCase
                .getAppealResponseDescription()
                .orElse("");

        final List<IdValue<DocumentWithDescription>> appealResponseEvidence =
            asylumCase
                .getAppealResponseEvidence()
                .orElse(Collections.emptyList());

        final List<IdValue<DocumentWithMetadata>> respondentDocuments =
            asylumCase
                .getRespondentDocuments()
                .orElse(Collections.emptyList());

        List<DocumentWithMetadata> appealResponseDocuments = new ArrayList<>();

        documentReceiver
            .receive(
                appealResponseDocument,
                appealResponseDescription,
                DocumentTag.APPEAL_RESPONSE
            )
            .ifPresent(appealResponseDocuments::add);

        documentReceiver
            .receiveAll(
                appealResponseEvidence,
                DocumentTag.APPEAL_RESPONSE
            )
            .forEach(appealResponseDocuments::add);

        List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
            documentsAppender.append(
                respondentDocuments,
                appealResponseDocuments,
                DocumentTag.APPEAL_RESPONSE
            );

        asylumCase.setRespondentDocuments(allRespondentDocuments);

        asylumCase.setAppealResponseAvailable(YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
