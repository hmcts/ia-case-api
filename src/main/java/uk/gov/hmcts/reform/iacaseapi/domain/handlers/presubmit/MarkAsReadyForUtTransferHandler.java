package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
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
public class MarkAsReadyForUtTransferHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public MarkAsReadyForUtTransferHandler(
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
                && callback.getEvent() == Event.MARK_AS_READY_FOR_UT_TRANSFER;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final Document noticeOfDecisionDocument =
                asylumCase
                        .read(NOTICE_OF_DECISION_UT_TRANSFER_DOCUMENT, Document.class)
                        .orElseThrow(() -> new IllegalStateException("noticeOfDecisionUtTransferDocument is not present"));

        DocumentWithMetadata noticeOfDecisionDocumentWithMetadata =
                documentReceiver.receive(
                        noticeOfDecisionDocument,
                        "",
                        DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER
                );

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingTribunalDocuments =
                asylumCase.read(TRIBUNAL_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> existingTribunalDocuments =
                maybeExistingTribunalDocuments.orElse(emptyList());

        if (noticeOfDecisionDocumentWithMetadata != null) {
            List<IdValue<DocumentWithMetadata>> allTribunalDocuments =
                    documentsAppender.append(existingTribunalDocuments, Arrays.asList(noticeOfDecisionDocumentWithMetadata));
            asylumCase.write(TRIBUNAL_DOCUMENTS, allTribunalDocuments);
        }
        asylumCase.write(APPEAL_READY_FOR_UT_TRANSFER, YesOrNo.YES);
        asylumCase.write(APPEAL_READY_FOR_UT_TRANSFER_OUTCOME, "Transferred to the Upper Tribunal as an expedited related appeal");

        State previousState = callback
            .getCaseDetailsBefore()
            .map(CaseDetails::getState)
            .orElseThrow(() -> new IllegalStateException("cannot find previous case state"));

        asylumCase.write(STATE_BEFORE_END_APPEAL, previousState);
        asylumCase.clear(REINSTATE_APPEAL_REASON);
        asylumCase.clear(REINSTATED_DECISION_MAKER);
        asylumCase.clear(APPEAL_STATUS);
        asylumCase.clear(REINSTATE_APPEAL_DATE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
