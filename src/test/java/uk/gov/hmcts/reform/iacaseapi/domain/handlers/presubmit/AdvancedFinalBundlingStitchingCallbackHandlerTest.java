package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.RP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AdvancedFinalBundlingStitchingCallbackHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private NotificationSender<AsylumCase> notificationSender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document stitchedDocument;
    @Mock private List<IdValue<DocumentWithMetadata>> maybeHearingDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allHearingDocuments;
    @Mock private DocumentWithMetadata stitchedDocumentWithMetadata;
    @Mock private HomeOfficeApi<AsylumCase> homeOfficeApi;
    @Mock private FeatureToggler featureToggler;

    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    private AdvancedFinalBundlingStitchingCallbackHandler advancedFinalBundlingStitchingCallbackHandler;

    @BeforeEach
    public void setUp() {
        advancedFinalBundlingStitchingCallbackHandler =
            new AdvancedFinalBundlingStitchingCallbackHandler(documentReceiver, documentsAppender, notificationSender, featureToggler, homeOfficeApi);

        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(notificationSender.send(callback)).thenReturn(asylumCase);
        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"),
            Optional.of(stitchedDocument), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));
    }

    @Test
    void should_be_handled_last() {
        assertEquals(DispatchPriority.LAST, advancedFinalBundlingStitchingCallbackHandler.getDispatchPriority());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_successfully_handle_the_callback(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(maybeHearingDocuments));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);

        when(documentsAppender.append(
            anyList(),
            anyList(),
            eq(DocumentTag.HEARING_BUNDLE)
        )).thenReturn(allHearingDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, allHearingDocuments);
        verify(asylumCase, times(1)).read(HEARING_DOCUMENTS);
        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.HEARING_BUNDLE);
        verify(documentsAppender).append(anyList(), anyList(), eq(DocumentTag.HEARING_BUNDLE));
        verify(asylumCase, times(1)).clear(IS_HEARING_BUNDLE_UPDATED);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_not_remove_existing_bundle_when_updated(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(maybeHearingDocuments));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.UPDATED_HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);

        when(asylumCase.read(AsylumCaseFieldDefinition.IS_HEARING_BUNDLE_UPDATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        when(documentsAppender.append(
            anyList(),
            anyList()
        )).thenReturn(allHearingDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, allHearingDocuments);
        verify(asylumCase, times(1)).read(HEARING_DOCUMENTS);
        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.UPDATED_HEARING_BUNDLE);
        verify(documentsAppender).append(anyList(), anyList());
        verify(asylumCase).clear(IS_HEARING_BUNDLE_UPDATED);
        advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void should_successfully_handle_the_callback_in_reheard_case(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class), Optional.of(YesOrNo.YES));

        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS)).thenReturn(Optional.of(maybeHearingDocuments));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);

        when(documentsAppender.append(
            anyList(),
            anyList(),
            eq(DocumentTag.HEARING_BUNDLE)
        )).thenReturn(allHearingDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS, allHearingDocuments);
        verify(asylumCase, times(1)).read(REHEARD_HEARING_DOCUMENTS);
        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.HEARING_BUNDLE);
        verify(documentsAppender).append(anyList(), anyList(), eq(DocumentTag.HEARING_BUNDLE));
        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void should_not_remove_existing_reheard_bundle_when_updated(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class), Optional.of(YesOrNo.YES));

        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS)).thenReturn(Optional.of(maybeHearingDocuments));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.UPDATED_HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);

        when(documentsAppender.append(
            anyList(),
            anyList()
        )).thenReturn(allHearingDocuments);

        when(asylumCase.read(AsylumCaseFieldDefinition.IS_HEARING_BUNDLE_UPDATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS, allHearingDocuments);
        verify(asylumCase, times(1)).read(REHEARD_HEARING_DOCUMENTS);
        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.UPDATED_HEARING_BUNDLE);
        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
        verify(documentsAppender).append(anyList(), anyList());
        verify(asylumCase).clear(IS_HEARING_BUNDLE_UPDATED);
        advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void should_write_instruct_status_when_ho_notification_feature_on(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_not_call_home_office_notification_when_ho_validation_has_failed(AppealType appealType) {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("FAIL"));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        //verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
        verify(asylumCase, times(1)).clear(IS_HEARING_BUNDLE_UPDATED);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_not_call_home_office_notification_when_ho_validation_success_but_for_in_progress_case(AppealType appealType) {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_not_write_instruct_status_when_ho_notification_feature_off(AppealType appealType) {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(false);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(AsylumCaseFieldDefinition.HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_not_write_instruct_status_when_ho_notification_feature_missing(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_not_call_ho_api_when_ooc_appeal(AppealType appealType) {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_successfully_handle_the_callback_in_remitted_reheard_case(AppealType appealType) {

        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(allHearingDocuments);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
                new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        final List<IdValue<ReheardHearingDocuments>> listOfReheardDocs = Lists.newArrayList(reheardHearingDocuments);

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION)).thenReturn(Optional.of(listOfReheardDocs));
        when(documentReceiver
                .receive(
                        stitchedDocument,
                        "",
                        DocumentTag.HEARING_BUNDLE
                )).thenReturn(stitchedDocumentWithMetadata);

        when(documentsAppender.append(
                anyList(),
                anyList(),
                eq(DocumentTag.HEARING_BUNDLE)
        )).thenReturn(allHearingDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS_COLLECTION, listOfReheardDocs);
        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.HEARING_BUNDLE);
        verify(documentsAppender).append(anyList(), anyList(), eq(DocumentTag.HEARING_BUNDLE));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "EU" })
    void should_successfully_handle_the_callback_in_remitted_reheard_case_when_collection_empty(AppealType appealType) {

        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(allHearingDocuments);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
            new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        final List<IdValue<ReheardHearingDocuments>> listOfReheardDocs = Lists.newArrayList(reheardHearingDocuments);

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION)).thenReturn(Optional.empty());
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);

        when(documentsAppender.append(
            anyList(),
            anyList(),
            eq(DocumentTag.HEARING_BUNDLE)
        )).thenReturn(allHearingDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase, times(1)).write(eq(REHEARD_HEARING_DOCUMENTS_COLLECTION), refEq(listOfReheardDocs));
        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.HEARING_BUNDLE);
        verify(documentsAppender).append(anyList(), anyList(), eq(DocumentTag.HEARING_BUNDLE));
    }

    @Test
    void handler_should_not_send_notification_when_is_notification_turned_off_() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(RP));
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(documentReceiver
            .receive(
                stitchedDocument,
                "",
                DocumentTag.HEARING_BUNDLE
            )).thenReturn(stitchedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> response =
                advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(notificationSender, times(0)).send(callback);
    }

    @Test
    void should_throw_when_case_bundle_is_not_present() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_empty() {

        caseBundles.clear();

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("case bundles size is not 1 and is : 0")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                for (State state : State.values()) {

                    when(callback.getCaseDetails().getState()).thenReturn(state);

                    boolean canHandle = advancedFinalBundlingStitchingCallbackHandler.canHandle(callbackStage, callback);

                    if (event == Event.ASYNC_STITCHING_COMPLETE
                        && callbackStage == ABOUT_TO_SUBMIT
                        && state != State.FTPA_DECIDED
                    ) {
                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }
            }
        }

        reset(callback);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> advancedFinalBundlingStitchingCallbackHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> advancedFinalBundlingStitchingCallbackHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
