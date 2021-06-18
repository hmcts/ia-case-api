package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadDecisionLetterHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadDecisionLetterHandler(
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
            && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<DocumentWithDescription>>> maybeNoticeOfDecision =
            asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);

        List<DocumentWithMetadata> noticeOfDecision =
            maybeNoticeOfDecision
                .orElseThrow(() -> new IllegalStateException("upload notice decision is not present"))
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.HO_DECISION_LETTER))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());


        Optional<JourneyType> journeyTypeOptional = asylumCase.read(JOURNEY_TYPE, JourneyType.class);
        boolean isAipJourney = journeyTypeOptional.map(journeyType -> journeyType == JourneyType.AIP).orElse(false);

        AsylumCaseFieldDefinition documentsFieldToUse = isAipJourney ? APPELLANT_DOCUMENTS : LEGAL_REPRESENTATIVE_DOCUMENTS;

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingLegalRepDocuments =
            asylumCase.read(documentsFieldToUse);

        final List<IdValue<DocumentWithMetadata>> existingLegalRepDocuments =
            maybeExistingLegalRepDocuments.orElse(emptyList());

        List<IdValue<DocumentWithMetadata>> allLegalRepDocuments =
            documentsAppender.append(existingLegalRepDocuments, noticeOfDecision);

        asylumCase.write(documentsFieldToUse, allLegalRepDocuments);

        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
