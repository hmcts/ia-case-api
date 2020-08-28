package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;



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

    List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    AdvancedFinalBundlingStitchingCallbackHandler advancedFinalBundlingStitchingCallbackHandler;

    @BeforeEach
    void setUp() {

        advancedFinalBundlingStitchingCallbackHandler =
            new AdvancedFinalBundlingStitchingCallbackHandler(documentReceiver, documentsAppender, notificationSender);
    }

    @Test
    void should_successfully_handle_the_callback() {

        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(notificationSender.send(callback)).thenReturn(asylumCase);
        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.of(stitchedDocument), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));
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

    @Test
    void should_throw_when_case_bundle_is_not_present() {

        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.of(stitchedDocument), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_empty() {

        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.of(stitchedDocument), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));

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

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedFinalBundlingStitchingCallbackHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
