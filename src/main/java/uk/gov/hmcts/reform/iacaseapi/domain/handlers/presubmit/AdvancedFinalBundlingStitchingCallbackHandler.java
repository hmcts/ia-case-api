package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;


@Component
public class AdvancedFinalBundlingStitchingCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationSender<AsylumCase> notificationSender;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;


    public AdvancedFinalBundlingStitchingCallbackHandler(
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
               && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE;
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


        Optional<List<IdValue<Bundle>>> maybeCaseBundles  = asylumCase.read(AsylumCaseFieldDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList());

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stictchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final Bundle hearingBundle = caseBundles.get(0);

        final Optional<Document> stitchedDocument = hearingBundle.getStitchedDocument();

        Optional<YesOrNo> maybeCaseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS,YesOrNo.class);

        boolean isReheardCase = maybeCaseFlagSetAsideReheardExists.isPresent()
                && maybeCaseFlagSetAsideReheardExists.get() == YesOrNo.YES;

        if (stitchedDocument.isPresent()) {
            saveHearingBundleDocument(asylumCase, stitchedDocument,isReheardCase ? REHEARD_HEARING_DOCUMENTS : HEARING_DOCUMENTS);
        }

        final String stitchStatus = hearingBundle.getStitchStatus().orElse("");

        asylumCase.write(AsylumCaseFieldDefinition.STITCHING_STATUS, stitchStatus);

        AsylumCase asylumCaseWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);

    }

    private void saveHearingBundleDocument(AsylumCase asylumCase, Optional<Document> stitchedDocument, AsylumCaseFieldDefinition field) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments =
            asylumCase.read(field);

        final List<IdValue<DocumentWithMetadata>> hearingDocuments =
            maybeHearingDocuments.orElse(emptyList());

        List<DocumentWithMetadata> hearingBundleDocuments = new ArrayList<>();

        hearingBundleDocuments.add(
            documentReceiver
                .receive(
                    stitchedDocument.orElse(null),
                    "",
                    DocumentTag.HEARING_BUNDLE
                )
        );

        List<IdValue<DocumentWithMetadata>> allHearingDocuments =
            documentsAppender.append(
                hearingDocuments,
                hearingBundleDocuments,
                DocumentTag.HEARING_BUNDLE
            );

        asylumCase.write(field, allHearingDocuments);
    }
}
