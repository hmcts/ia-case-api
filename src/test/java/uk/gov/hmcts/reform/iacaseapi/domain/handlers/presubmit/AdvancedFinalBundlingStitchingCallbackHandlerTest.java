package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.RP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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

    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    private AdvancedFinalBundlingStitchingCallbackHandler advancedFinalBundlingStitchingCallbackHandler;

    @BeforeEach
    public void setUp() {
        advancedFinalBundlingStitchingCallbackHandler =
            new AdvancedFinalBundlingStitchingCallbackHandler(documentReceiver, documentsAppender, notificationSender);

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
        verify(asylumCase, times(1)).clear(IS_HEARING_BUNDLE_UPDATED);
    }

    @Test
    void should_not_remove_existing_bundle_when_updated() {
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

    @Test
    void should_successfully_handle_the_callback_in_remitted_reheard_case() {

        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(allHearingDocuments);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
                new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        final List<IdValue<ReheardHearingDocuments>> listOfReheardDocs = Lists.newArrayList(reheardHearingDocuments);

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
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

    @Test
    void should_successfully_handle_the_callback_in_remitted_reheard_case_when_collection_empty() {

        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(allHearingDocuments);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
            new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        final List<IdValue<ReheardHearingDocuments>> listOfReheardDocs = Lists.newArrayList(reheardHearingDocuments);

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
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
