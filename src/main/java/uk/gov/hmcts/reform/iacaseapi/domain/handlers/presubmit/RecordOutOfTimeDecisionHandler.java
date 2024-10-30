package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.OutOfTimeDecisionDetailsAppender;

@Component
public class RecordOutOfTimeDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private DocumentReceiver documentReceiver;
    private DocumentsAppender documentsAppender;
    private UserDetails userDetails;
    private UserDetailsHelper userDetailsHelper;
    private OutOfTimeDecisionDetailsAppender outOfTimeDecisionDetailsAppender;

    public RecordOutOfTimeDecisionHandler(
        OutOfTimeDecisionDetailsAppender outOfTimeDecisionDetailsAppender,
        UserDetailsHelper userDetailsHelper,
        UserDetails userDetails,
        DocumentsAppender documentsAppender,
        DocumentReceiver documentReceiver
    ) {
        this.outOfTimeDecisionDetailsAppender = outOfTimeDecisionDetailsAppender;
        this.userDetailsHelper = userDetailsHelper;
        this.userDetails = userDetails;
        this.documentsAppender = documentsAppender;
        this.documentReceiver = documentReceiver;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION;
    }


    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo recordedOutOfTimeDecision = asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class).orElse(NO);


        final Document outOfTimeDecisionDocument = asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT, Document.class)
            .orElseThrow(() -> new IllegalStateException("Out of time decision document is not present"));

        Optional<List<IdValue<DocumentWithMetadata>>> maybeTribunalDocuments =
            asylumCase.read(TRIBUNAL_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> tribunalDocuments =
            maybeTribunalDocuments.orElse(emptyList());

        DocumentWithMetadata tribunalDocumentWithMetadata =
            documentReceiver.receive(
                outOfTimeDecisionDocument,
                "",
                DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT
            );

        List<IdValue<DocumentWithMetadata>> allTribunalDocuments =
            documentsAppender.append(
                tribunalDocuments,
                singletonList(tribunalDocumentWithMetadata)
            );

        if (isInternalNonDetainedCase(asylumCase) && !isOutOfTimeDecisionRejected(asylumCase)) {

            DocumentWithMetadata newLetterGenerationAttachmentForBundlingWithMetadata =
                documentReceiver.receive(
                    outOfTimeDecisionDocument,
                    "",
                    DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER
                );

            List<IdValue<DocumentWithMetadata>> newLetterGenerationAttachmentForBundling =
                documentsAppender.append(
                    tribunalDocuments,
                    singletonList(newLetterGenerationAttachmentForBundlingWithMetadata)
                );

            asylumCase.write(LETTER_NOTIFICATION_DOCUMENTS, newLetterGenerationAttachmentForBundling);

        }

        asylumCase.write(TRIBUNAL_DOCUMENTS, allTribunalDocuments);

        if (recordedOutOfTimeDecision == YES) {

            List<IdValue<OutOfTimeDecisionDetails>> allOutOfTimeDecisionDetails =
                outOfTimeDecisionDetailsAppender.getAllOutOfTimeDecisionDetails();

            asylumCase.write(PREVIOUS_OUT_OF_TIME_DECISION_DETAILS, allOutOfTimeDecisionDetails);
            outOfTimeDecisionDetailsAppender.setAllOutOfTimeDecisionDetails(null);
        }

        asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, YES);
        asylumCase.write(OUT_OF_TIME_DECISION_MAKER,
            userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isOutOfTimeDecisionRejected(AsylumCase asylumCase) {
        return asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
            .map(OutOfTimeDecisionType -> OutOfTimeDecisionType.equals(OutOfTimeDecisionType.REJECTED))
            .orElse(false);
    }

    private boolean isInternalNonDetainedCase(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && !isAppellantInDetention(asylumCase);
    }
}
