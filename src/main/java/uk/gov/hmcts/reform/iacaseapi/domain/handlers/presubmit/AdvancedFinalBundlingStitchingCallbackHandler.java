package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isNotificationTurnedOff;


@Component
@Slf4j
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

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
            && callback.getCaseDetails().getState() != State.FTPA_DECIDED;
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

        YesOrNo isHearingBundleUpdated = asylumCase
            .read(AsylumCaseFieldDefinition.IS_HEARING_BUNDLE_UPDATED, YesOrNo.class).orElse(YesOrNo.NO);
        asylumCase.clear(AsylumCaseFieldDefinition.IS_HEARING_BUNDLE_UPDATED);
        Optional<List<IdValue<Bundle>>> maybeCaseBundles = asylumCase.read(AsylumCaseFieldDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .toList();

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stictchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final Bundle hearingBundle = caseBundles.getFirst();

        final Optional<Document> stitchedDocument = hearingBundle.getStitchedDocument();

        if (stitchedDocument.isPresent()) {
            saveHearingBundleDocument(asylumCase, stitchedDocument, isHearingBundleUpdated);
        }

        final String stitchStatus = hearingBundle.getStitchStatus().orElse("");

        asylumCase.write(AsylumCaseFieldDefinition.STITCHING_STATUS, stitchStatus);

        AsylumCase asylumCaseWithNotificationMarker = isNotificationTurnedOff(asylumCase)
            ? asylumCase : notificationSender.send(callback);
        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }

    private void saveHearingBundleDocument(AsylumCase asylumCase, Optional<Document> stitchedDocument,
                                           YesOrNo isHearingBundleUpdated) {

        Optional<YesOrNo> maybeCaseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class);

        boolean isReheardCase = maybeCaseFlagSetAsideReheardExists.isPresent()
            && maybeCaseFlagSetAsideReheardExists.get() == YesOrNo.YES;

        final List<IdValue<DocumentWithMetadata>> hearingDocuments = fetchHearingDocuments(asylumCase, isReheardCase);

        List<DocumentWithMetadata> hearingBundleDocuments = new ArrayList<>();
        DocumentWithMetadata hearingBundle = documentReceiver
            .receive(
                stitchedDocument.orElse(null),
                "",
                isHearingBundleUpdated == YesOrNo.YES ?
                    DocumentTag.UPDATED_HEARING_BUNDLE : DocumentTag.HEARING_BUNDLE
            );
        String currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime().toString();
        hearingBundle.setDateTimeUploaded(currentDateTime);
        hearingBundleDocuments.add(hearingBundle);
        List<IdValue<DocumentWithMetadata>> allHearingDocuments;
        if (isHearingBundleUpdated == YesOrNo.YES) {
            allHearingDocuments =
                documentsAppender.append(
                    hearingDocuments,
                    hearingBundleDocuments
                );
        } else {
            allHearingDocuments =
                documentsAppender.append(
                    hearingDocuments,
                    hearingBundleDocuments,
                    DocumentTag.HEARING_BUNDLE
                );
        }
        handleReheardDocumentsWrite(asylumCase, isReheardCase, allHearingDocuments);
    }

    public static void handleReheardDocumentsWrite(AsylumCase asylumCase, boolean isReheardCase, List<IdValue<DocumentWithMetadata>> allHearingDocuments) {
        if (isReheardCase) {
            Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
                asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
            List<IdValue<ReheardHearingDocuments>> existingReheardDocuments = maybeExistingReheardDocuments.orElse(emptyList());
            if (!existingReheardDocuments.isEmpty()) {
                existingReheardDocuments.getFirst().getValue().setReheardHearingDocs(allHearingDocuments);
            } else {
                Appender<ReheardHearingDocuments> documentsCollectionAppender =
                    new Appender<>();
                ReheardHearingDocuments reheardHearingDocuments = new ReheardHearingDocuments(allHearingDocuments);
                existingReheardDocuments = documentsCollectionAppender.append(reheardHearingDocuments, existingReheardDocuments);
            }
            asylumCase.write(REHEARD_HEARING_DOCUMENTS_COLLECTION, existingReheardDocuments);

        } else {
            asylumCase.write(HEARING_DOCUMENTS, allHearingDocuments);
        }
    }

    private List<IdValue<DocumentWithMetadata>> fetchHearingDocuments(AsylumCase asylumCase,
                                                                      boolean isReheardCase) {
        if (isReheardCase) {
            Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
                asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
            List<IdValue<ReheardHearingDocuments>> existingReheardDocuments = maybeExistingReheardDocuments.orElse(emptyList());

            return (!existingReheardDocuments.isEmpty())
                ? existingReheardDocuments.getFirst().getValue().getReheardHearingDocs()
                : emptyList();
        }
        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments =
            asylumCase.read(HEARING_DOCUMENTS);
        return maybeHearingDocuments.orElse(emptyList());
    }
}
