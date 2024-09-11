package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isNotificationTurnedOff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.*;


@Component
@Slf4j
public class UpperTribunalStitchingCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationSender<AsylumCase> notificationSender;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UpperTribunalStitchingCallbackHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        NotificationSender<AsylumCase> notificationSender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.notificationSender = notificationSender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
               && callback.getCaseDetails().getState() == State.FTPA_DECIDED;
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

        Optional<List<IdValue<Bundle>>> maybeCaseBundles = asylumCase.read(AsylumCaseFieldDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList());

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        final Bundle upperTribunalBundle = caseBundles.get(0);

        final Optional<Document> stitchedDocument = upperTribunalBundle.getStitchedDocument();

        if (stitchedDocument.isPresent()) {
            saveUpperTribunalBundleDocument(asylumCase, stitchedDocument, UPPER_TRIBUNAL_DOCUMENTS);
        }

        final String stitchStatus = upperTribunalBundle.getStitchStatus().orElse("");

        //asylumCase.write(AsylumCaseFieldDefinition.STITCHING_STATUS, stitchStatus);

        AsylumCase asylumCaseWithNotificationMarker = isNotificationTurnedOff(asylumCase)
                ? asylumCase : notificationSender.send(callback);

        asylumCaseWithNotificationMarker.write(STITCHING_STATUS_UPPER_TRIBUNAL, "FAILED");

        log.info("The upper tribunal stitching status in ia case api is "
                + asylumCaseWithNotificationMarker.read(STITCHING_STATUS_UPPER_TRIBUNAL) + " on case id "
                + callback.getCaseDetails().getId());

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }

    private void saveUpperTribunalBundleDocument(AsylumCase asylumCase, Optional<Document> stitchedDocument, AsylumCaseFieldDefinition field) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeUpperTribunalDocuments =
            asylumCase.read(field);

        final List<IdValue<DocumentWithMetadata>> upperTribunalDocuments =
            maybeUpperTribunalDocuments.orElse(emptyList());

        List<DocumentWithMetadata> upperTribunalBundleDocuments = new ArrayList<>();

        upperTribunalBundleDocuments.add(
            documentReceiver
                .receive(
                    stitchedDocument.orElse(null),
                    "",
                    DocumentTag.UPPER_TRIBUNAL_BUNDLE
                )
        );

        List<IdValue<DocumentWithMetadata>> allUpperTribunalDocuments =
            documentsAppender.append(
                upperTribunalDocuments,
                upperTribunalBundleDocuments,
                DocumentTag.UPPER_TRIBUNAL_BUNDLE
            );

        asylumCase.write(field, allUpperTribunalDocuments);
    }
}
