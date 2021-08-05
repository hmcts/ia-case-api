package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
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
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
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
    void should_successfully_handle_the_callback() {


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

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void should_successfully_handle_the_callback_in_reheard_case(boolean hoUanFeatureFlag) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(hoUanFeatureFlag);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        if (hoUanFeatureFlag) {
            when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        } else {
            when(homeOfficeApi.call(callback)).thenReturn(asylumCase);
        }
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
        if (hoUanFeatureFlag) {
            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        } else {
            verify(homeOfficeApi, times(1)).call(callback);
        }
        verify(notificationSender, times(1)).send(callback);

    }

    @Test
    void should_write_instruct_status_when_ho_notification_feature_on() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
    }

    @Test
    void should_not_call_home_office_notification_when_ho_validation_has_failed() {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)).thenReturn(Optional.of("OK"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("FAIL"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        //verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        verify(notificationSender, times(1)).send(callback);
    }

    @Test
    void should_not_call_home_office_notification_when_ho_validation_success_but_for_in_progress_case() {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
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

    @Test
    void should_not_write_instruct_status_when_ho_notification_feature_off() {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(AsylumCaseFieldDefinition.HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
    }

    @Test
    void should_not_write_instruct_status_when_ho_notification_feature_missing() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
    }

    @Test
    void should_not_call_ho_api_when_ooc_appeal() {

        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
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

                boolean canHandle = advancedFinalBundlingStitchingCallbackHandler.canHandle(callbackStage, callback);

                if (event == Event.ASYNC_STITCHING_COMPLETE
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
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
