//package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import static org.powermock.api.mockito.PowerMockito.when;
//import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
//import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
//import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
//
//import java.util.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
//import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
//import uk.gov.hmcts.reform.iacaseapi.domain.service.*;
//
//@MockitoSettings(strictness = Strictness.LENIENT)
//@ExtendWith(MockitoExtension.class)
//@SuppressWarnings("unchecked")
//class UpperTribunalStitchingCallbackHandlerTest {
//
//    @Mock private DocumentReceiver documentReceiver;
//    @Mock private DocumentsAppender documentsAppender;
//    @Mock private NotificationSender<AsylumCase> notificationSender;
//    @Mock private Callback<AsylumCase> callback;
//    @Mock private CaseDetails<AsylumCase> caseDetails;
//    @Mock private AsylumCase asylumCase;
//    @Mock private Document stitchedDocument;
//    @Mock private List<IdValue<DocumentWithMetadata>> maybeUpperTribunalDocuments;
//    @Mock private List<IdValue<DocumentWithMetadata>> allUpperTribunalDocuments;
//    @Mock private DocumentWithMetadata stitchedDocumentWithMetadata;
//
//    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
//    private UpperTribunalStitchingCallbackHandler upperTribunalStitchingCallbackHandler;
//
//    @BeforeEach
//    public void setUp() {
//
//        upperTribunalStitchingCallbackHandler =
//            new UpperTribunalStitchingCallbackHandler(documentReceiver, documentsAppender, notificationSender);
//
//        when(callback.getCaseDetails()).thenReturn(caseDetails);
//        when(caseDetails.getCaseData()).thenReturn(asylumCase);
//        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
//        when(callback.getCaseDetails().getState()).thenReturn(State.FTPA_DECIDED);
//
//        when(notificationSender.send(callback)).thenReturn(asylumCase);
//        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));
//
//        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"),
//            Optional.of(stitchedDocument), YesOrNo.YES, YesOrNo.YES, "fileName");
//        caseBundles.add(new IdValue<>("1", bundle));
//    }
//
//    @Test
//    void should_successfully_handle_the_callback() {
//
//        when(asylumCase.read(UPPER_TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(maybeUpperTribunalDocuments));
//        when(documentReceiver
//            .receive(
//                stitchedDocument,
//                "",
//                DocumentTag.UPPER_TRIBUNAL_BUNDLE
//            )).thenReturn(stitchedDocumentWithMetadata);
//
//        when(documentsAppender.append(
//            anyList(),
//            anyList(),
//            eq(DocumentTag.UPPER_TRIBUNAL_BUNDLE)
//        )).thenReturn(allUpperTribunalDocuments);
//
//        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
//            upperTribunalStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);
//
//        assertNotNull(callbackResponse);
//        assertEquals(asylumCase, callbackResponse.getData());
//
//        verify(asylumCase, times(1)).write(STITCHING_STATUS_UPPER_TRIBUNAL, "NEW");
//        verify(asylumCase, times(1)).write(UPPER_TRIBUNAL_DOCUMENTS, allUpperTribunalDocuments);
//        verify(asylumCase, times(1)).read(UPPER_TRIBUNAL_DOCUMENTS);
//        verify(documentReceiver).receive(stitchedDocument, "", DocumentTag.UPPER_TRIBUNAL_BUNDLE);
//        verify(documentsAppender).append(anyList(), anyList(), eq(DocumentTag.UPPER_TRIBUNAL_BUNDLE));
//    }
//
//    @Test
//    void handler_should_not_send_notification_when_is_notification_turned_off_() {
//        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
//
//        PreSubmitCallbackResponse<AsylumCase> response =
//                upperTribunalStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);
//
//        assertThat(response).isNotNull();
//        verify(notificationSender, times(0)).send(callback);
//    }
//
//    @Test
//    void should_throw_when_case_bundle_is_not_present() {
//
//        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> upperTribunalStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
//            .hasMessage("caseBundle is not present")
//            .isExactlyInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    void should_throw_when_case_bundle_is_empty() {
//
//        caseBundles.clear();
//
//        assertThatThrownBy(() -> upperTribunalStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
//            .hasMessage("case bundles size is not 1 and is : 0")
//            .isExactlyInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    void handling_should_throw_if_cannot_actually_handle() {
//
//        assertThatThrownBy(() -> upperTribunalStitchingCallbackHandler.handle(ABOUT_TO_START, callback))
//            .hasMessage("Cannot handle callback")
//            .isExactlyInstanceOf(IllegalStateException.class);
//
//        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
//        assertThatThrownBy(() -> upperTribunalStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
//            .hasMessage("Cannot handle callback")
//            .isExactlyInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    void it_can_handle_callback() {
//
//        for (Event event : Event.values()) {
//
//            when(callback.getEvent()).thenReturn(event);
//
//            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
//
//                for (State state : State.values()) {
//
//                    when(callback.getCaseDetails().getState()).thenReturn(state);
//
//                    boolean canHandle = upperTribunalStitchingCallbackHandler.canHandle(callbackStage, callback);
//
//                    if (event == Event.ASYNC_STITCHING_COMPLETE
//                        && callbackStage == ABOUT_TO_SUBMIT
//                        && state == State.FTPA_DECIDED
//                    ) {
//                        assertTrue(canHandle);
//                    } else {
//                        assertFalse(canHandle);
//                    }
//                }
//            }
//        }
//
//        reset(callback);
//    }
//
//    @Test
//    void should_not_allow_null_arguments() {
//
//        assertThatThrownBy(() -> upperTribunalStitchingCallbackHandler.canHandle(null, callback))
//            .hasMessage("callbackStage must not be null")
//            .isExactlyInstanceOf(NullPointerException.class);
//
//        assertThatThrownBy(
//            () -> upperTribunalStitchingCallbackHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
//            .hasMessage("callback must not be null")
//            .isExactlyInstanceOf(NullPointerException.class);
//
//        assertThatThrownBy(() -> upperTribunalStitchingCallbackHandler.handle(null, callback))
//            .hasMessage("callbackStage must not be null")
//            .isExactlyInstanceOf(NullPointerException.class);
//
//        assertThatThrownBy(
//            () -> upperTribunalStitchingCallbackHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
//            .hasMessage("callback must not be null")
//            .isExactlyInstanceOf(NullPointerException.class);
//    }
//}
