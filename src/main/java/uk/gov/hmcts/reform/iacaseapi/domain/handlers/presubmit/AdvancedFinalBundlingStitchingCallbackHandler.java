package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;


@Component
@Slf4j
public class AdvancedFinalBundlingStitchingCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationSender<AsylumCase> notificationSender;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final FeatureToggler featureToggler;
    private final HomeOfficeApi<AsylumCase> homeOfficeApi;
    private static final String HO_NOTIFICATION_FEATURE = "home-office-notification-feature";

    public AdvancedFinalBundlingStitchingCallbackHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        NotificationSender<AsylumCase> notificationSender,
        FeatureToggler featureToggler,
        HomeOfficeApi<AsylumCase> homeOfficeApi
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.notificationSender = notificationSender;
        this.featureToggler = featureToggler;
        this.homeOfficeApi = homeOfficeApi;
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


        Optional<List<IdValue<Bundle>>> maybeCaseBundles = asylumCase.read(AsylumCaseFieldDefinition.CASE_BUNDLES);

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

        Optional<YesOrNo> maybeCaseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class);

        boolean isReheardCase = maybeCaseFlagSetAsideReheardExists.isPresent()
                                && maybeCaseFlagSetAsideReheardExists.get() == YesOrNo.YES;

        if (stitchedDocument.isPresent()) {
            saveHearingBundleDocument(asylumCase, stitchedDocument, isReheardCase ? REHEARD_HEARING_DOCUMENTS : HEARING_DOCUMENTS);
        }

        final String stitchStatus = hearingBundle.getStitchStatus().orElse("");

        asylumCase.write(AsylumCaseFieldDefinition.STITCHING_STATUS, stitchStatus);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        if (asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
                .map(value -> value.equals(YesOrNo.YES))
                .orElse(true)
                && HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType)) {

            handleHomeOfficeNotification(callback, asylumCase);
        }

        AsylumCase asylumCaseWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }

    private void handleHomeOfficeNotification(Callback<AsylumCase> callback, AsylumCase asylumCase) {

        if (featureToggler.getValue(HO_NOTIFICATION_FEATURE, false)) {

            final String homeOfficeSearchStatus = asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)
                .orElse("");

            final YesOrNo homeOfficeNotificationsEligible = asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)
                .orElse(YesOrNo.NO);

            if ("SUCCESS".equalsIgnoreCase(homeOfficeSearchStatus)
                && homeOfficeNotificationsEligible == YesOrNo.YES) {

                AsylumCase asylumCaseWithHomeOfficeData =
                        featureToggler.getValue("home-office-uan-feature", false)
                                ? homeOfficeApi.aboutToSubmit(callback) : homeOfficeApi.call(callback);

                asylumCase.write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS,
                    asylumCaseWithHomeOfficeData.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class).orElse(""));
            } else {
                final long caseId = callback.getCaseDetails().getId();
                final String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse("");

                log.warn("Home Office notification was not invoked due to unsuccessful validation search - "
                         + "caseId: {}, "
                         + "homeOfficeReferenceNumber: {}, "
                         + "homeOfficeSearchStatus: {}, "
                         + "homeOfficeNotificationsEligible: {} ",
                    caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus, homeOfficeNotificationsEligible);
            }
        }
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
