package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class FtpaAppellantHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public FtpaAppellantHandler(
        DateProvider dateProvider,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.dateProvider = dateProvider;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT;
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

        final Optional<List<IdValue<DocumentWithDescription>>>  maybeOutOfTimeDocuments = asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaAppellantOutOfTimeDocuments = maybeOutOfTimeDocuments.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>>  maybeDocument = asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaAppellantGrounds = maybeDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaAppellantEvidence = asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaAppellantEvidence = maybeFtpaAppellantEvidence.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaAppellantDocuments = asylumCase.read(FTPA_APPELLANT_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingFtpaAppellantDocuments = maybeFtpaAppellantDocuments.orElse(Collections.emptyList());

        final List<DocumentWithMetadata> ftpaAppellantDocuments = new ArrayList<>();

        ftpaAppellantDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaAppellantOutOfTimeDocuments,
                    DocumentTag.FTPA_APPELLANT
                )
        );

        ftpaAppellantDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaAppellantGrounds,
                    DocumentTag.FTPA_APPELLANT
                )
        );

        ftpaAppellantDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaAppellantEvidence,
                    DocumentTag.FTPA_APPELLANT
                )
        );

        List<IdValue<DocumentWithMetadata>> allAppellantDocuments =
            documentsAppender.append(
                existingFtpaAppellantDocuments,
                ftpaAppellantDocuments
            );

        asylumCase.write(FTPA_APPELLANT_DOCUMENTS, allAppellantDocuments);

        asylumCase.write(FTPA_APPELLANT_APPLICATION_DATE, dateProvider.now().toString());

        asylumCase.write(FTPA_APPELLANT_SUBMITTED, YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
