package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MarkAsReadyForUtTransferHandlerTest {

    private final Document noticeOfDecision = new Document(
        "someurl",
        "somebinaryurl",
        "somefilename.pdf");

    private final DocumentWithMetadata noticeOfDecisionMetaData = new DocumentWithMetadata(
        noticeOfDecision,
        "some description",
        "21/07/2021",
        DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER,
        "some supplier"
    );

    private final DocumentWithMetadata someTribunalMeta = new DocumentWithMetadata(
            new Document("someurl", "somebinaryurl", "somefilename.pdf"),
            "some description",
            "21/07/2022",
            DocumentTag.APPEAL_SUBMISSION,
            "some supplier"
    );

    private List<IdValue<DocumentWithMetadata>> allTribunalDocuments;

    @Mock
    private Callback<AsylumCase> mockCallback;
    @Mock
    private CaseDetails<AsylumCase> mockCaseDetails;
    @Mock
    private CaseDetails<AsylumCase> previousCaseDetails;
    @Mock
    private AsylumCase mockAsylumCase;
    @Mock
    private DocumentsAppender mockDocumentsAppender;
    @Mock
    private DocumentReceiver mockDocumentReceiver;
    @Mock
    private DocumentWithMetadata appealForm1WithMetadata;
    private MarkAsReadyForUtTransferHandler markAsReadyForUtTransferHandler;
    private State previousState = State.APPEAL_SUBMITTED;

    @BeforeEach
    public void setUp() {
        markAsReadyForUtTransferHandler =
                new MarkAsReadyForUtTransferHandler(mockDocumentReceiver, mockDocumentsAppender);

        when(mockCallback.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockCallback.getCaseDetails().getCaseData()).thenReturn(mockAsylumCase);
        when(previousCaseDetails.getState()).thenReturn(previousState);
        when(mockCallback
            .getCaseDetailsBefore()).thenReturn(Optional.of(previousCaseDetails));
    }

    @Test
    void should_append_notice_of_decision_ut_to_tribunal_documents_and_set_field() {
        allTribunalDocuments = Arrays.asList(
                new IdValue<>("1", someTribunalMeta)
        );
        when(mockCallback.getEvent()).thenReturn(Event.MARK_AS_READY_FOR_UT_TRANSFER);
        when(mockAsylumCase.read(NOTICE_OF_DECISION_UT_TRANSFER_DOCUMENT, Document.class)).thenReturn(Optional.of(noticeOfDecision));
        when(mockDocumentReceiver.receive(noticeOfDecision, "", DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER))
                .thenReturn(noticeOfDecisionMetaData);
        when(mockAsylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(allTribunalDocuments));
        when(mockDocumentsAppender.prepend(allTribunalDocuments, Arrays.asList(noticeOfDecisionMetaData))).thenReturn(allTribunalDocuments);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = markAsReadyForUtTransferHandler.handle(ABOUT_TO_SUBMIT, mockCallback);

        assertThat(callbackResponse).isNotNull();
        verify(mockDocumentsAppender, times(1)).append(
                allTribunalDocuments,
                Arrays.asList(noticeOfDecisionMetaData)
        );
        verify(mockAsylumCase, times(1)).write(APPEAL_READY_FOR_UT_TRANSFER, YesOrNo.YES);
        verify(mockAsylumCase, times(1)).write(APPEAL_READY_FOR_UT_TRANSFER_OUTCOME, "Transferred to the Upper Tribunal as an expedited related appeal");
        verify(mockAsylumCase).write(STATE_BEFORE_END_APPEAL, previousState);
        verify(mockAsylumCase).clear(REINSTATE_APPEAL_REASON);
        verify(mockAsylumCase).clear(REINSTATED_DECISION_MAKER);
        verify(mockAsylumCase).clear(APPEAL_STATUS);
        verify(mockAsylumCase).clear(REINSTATE_APPEAL_DATE);
    }


    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(mockCallback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = markAsReadyForUtTransferHandler.canHandle(callbackStage, mockCallback);

                if (callbackStage == ABOUT_TO_SUBMIT
                        && mockCallback.getEvent() == Event.MARK_AS_READY_FOR_UT_TRANSFER
                ) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAsReadyForUtTransferHandler.canHandle(null, mockCallback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAsReadyForUtTransferHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAsReadyForUtTransferHandler.handle(null, mockCallback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAsReadyForUtTransferHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> markAsReadyForUtTransferHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, mockCallback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> markAsReadyForUtTransferHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, mockCallback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}