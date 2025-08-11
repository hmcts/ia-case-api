gitpackage uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

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
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.OutOfTimeDecisionDetailsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordOutOfTimeDecisionHandlerTest {

    @Mock private DocumentsAppender documentsAppender;
    @Mock private DocumentReceiver documentReceiver;

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;

    @Mock private Document document;
    @Mock private UserDetails userDetails;
    @Mock private UserDetailsHelper userDetailsHelper;

    @Mock private Document outOfTimeDecisionDocument;
    @Mock private DocumentWithMetadata outOfTimeDecisionDocumentWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingTribunalDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allTribunalDocuments;

    @Mock private OutOfTimeDecisionDetailsAppender outOfTimeDecisionDetailsAppender;

    private RecordOutOfTimeDecisionHandler recordOutOfTimeDecisionHandler;

    @BeforeEach
    void setUp() {

        recordOutOfTimeDecisionHandler =
            new RecordOutOfTimeDecisionHandler(
                outOfTimeDecisionDetailsAppender, userDetailsHelper,
                userDetails, documentsAppender, documentReceiver);
    }

    @Test
    void should_not_write_to_case_data_if_no_previous_out_of_time_decision_details_are_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);
        when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT, Document.class)).thenReturn(Optional.of(outOfTimeDecisionDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordOutOfTimeDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(callbackResponse.getData(), asylumCase);

        verify(asylumCase, times(1)).write(RECORDED_OUT_OF_TIME_DECISION, YES);
        verify(asylumCase, times(1))
            .write(OUT_OF_TIME_DECISION_MAKER, "Tribunal Caseworker");
    }

    @Test
    void should_append_write_previous_out_of_time_decision_details() {

        OutOfTimeDecisionDetails outOfTimeDecisionDetails =
            new OutOfTimeDecisionDetails(OutOfTimeDecisionType.APPROVED.name(),
                UserRoleLabel.TRIBUNAL_CASEWORKER.name(), document);
        List<IdValue<OutOfTimeDecisionDetails>> previousOutOfTimeDecisionDetails =
            Arrays.asList(new IdValue<>("1", outOfTimeDecisionDetails));

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);
        when(outOfTimeDecisionDetailsAppender.getAllOutOfTimeDecisionDetails()).thenReturn(previousOutOfTimeDecisionDetails);
        when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT, Document.class)).thenReturn(Optional.of(outOfTimeDecisionDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordOutOfTimeDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(callbackResponse.getData(), asylumCase);

        verify(asylumCase, times(1))
            .write(PREVIOUS_OUT_OF_TIME_DECISION_DETAILS, outOfTimeDecisionDetailsAppender.getAllOutOfTimeDecisionDetails());
        verify(asylumCase, times(1))
            .write(OUT_OF_TIME_DECISION_MAKER, "Tribunal Caseworker");
    }

    @Test
    void should_append_out_of_time_decision_document_to_all_legal_rep_documents() {

        List<DocumentWithMetadata> outOfTimeDecisionDocumentsWithMetadata =
            Arrays.asList(
                outOfTimeDecisionDocumentWithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);
        when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT, Document.class)).thenReturn(Optional.of(outOfTimeDecisionDocument));
        when(asylumCase.read(TRIBUNAL_DOCUMENTS))
            .thenReturn(Optional.of(existingTribunalDocuments));

        when(documentReceiver.receive(outOfTimeDecisionDocument, "", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT))
            .thenReturn(outOfTimeDecisionDocumentWithMetadata);

        when(documentsAppender
            .append(existingTribunalDocuments, outOfTimeDecisionDocumentsWithMetadata))
            .thenReturn(allTribunalDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordOutOfTimeDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(documentReceiver, times(1))
            .receive(outOfTimeDecisionDocument, "", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT);
        verify(documentsAppender, times(1))
            .append(existingTribunalDocuments, Arrays.asList(outOfTimeDecisionDocumentWithMetadata));

        verify(asylumCase, times(1)).read(OUT_OF_TIME_DECISION_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).write(TRIBUNAL_DOCUMENTS, allTribunalDocuments);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordOutOfTimeDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordOutOfTimeDecisionHandler.canHandle(callbackStage, callback);

                if ((event == Event.RECORD_OUT_OF_TIME_DECISION)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordOutOfTimeDecisionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordOutOfTimeDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_create_end_appeal_notice_pdf_and_append_to_letter_notifications_documents_for_internal_non_detained() {

        List<DocumentWithMetadata> outOfTimeDecisionDocumentsWithMetadata =
            Arrays.asList(
                outOfTimeDecisionDocumentWithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)).thenReturn(Optional.of(OutOfTimeDecisionType.IN_TIME));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        when(asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT, Document.class)).thenReturn(Optional.of(outOfTimeDecisionDocument));
        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(existingTribunalDocuments));

        when(documentReceiver.receive(outOfTimeDecisionDocument, "", DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER))
            .thenReturn(outOfTimeDecisionDocumentWithMetadata);

        when(documentsAppender
            .append(existingTribunalDocuments, outOfTimeDecisionDocumentsWithMetadata))
            .thenReturn(allTribunalDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordOutOfTimeDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
    }



}
