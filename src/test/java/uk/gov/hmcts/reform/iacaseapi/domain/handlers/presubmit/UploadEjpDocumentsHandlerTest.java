package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
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
class UploadEjpDocumentsHandlerTest {

    private final String refNumber = "reference-number";
    private final String appellantGivenName = "appellant-given-name";
    private final String utTransferOrderSuffix = "UT-transfer-order";
    private final String iaut2AppealFormSuffix = "IAUT-2-appeal-form";

    private final Document existingDocumentOne = new Document(
            "someurl_existing_one",
            "someurl_existing_binaryurl_one",
            "someurl_existing_filename_one.pdf");
    private final Document someUtDocumentOne = new Document(
        "someurl_ut_one",
        "someurl_ut_binaryurl_one",
        "someurl_ut_filename_one.pdf");

    private final Document someUtDocumentTwo = new Document(
            "someurl_ut_two",
            "someurl_ut_binaryurl_two",
            "someurl_ut_filename_two.pdf");

    private final Document ejpAppealFormDocumentOne = new Document(
            "someurl_ejp_one",
            "someurl_ejp_binaryurl_one",
            "someurl_ejp_filename_one.pdf");

    private final Document ejpAppealFormDocumentTwo = new Document(
            "someurl_ejp_two",
            "someurl_ejp_binaryurl_two",
            "someurl_ejp_filename_two.pdf");

    private final DocumentWithMetadata someUtDocumentMetadataOne = new DocumentWithMetadata(
            someUtDocumentOne,
            "",
            "30/10/2323",
            DocumentTag.INTERNAL_EJP_DOCUMENT
    );

    private final DocumentWithMetadata someUtDocumentMetadataTwo = new DocumentWithMetadata(
            someUtDocumentTwo,
            "",
            "30/10/2323",
            DocumentTag.INTERNAL_EJP_DOCUMENT
    );

    private final DocumentWithMetadata ejpAppealFormDocumentMetadataOne = new DocumentWithMetadata(
            ejpAppealFormDocumentOne,
            "",
            "30/10/2323",
            DocumentTag.INTERNAL_EJP_DOCUMENT
    );

    private final DocumentWithMetadata ejpAppealFormDocumentMetadataTwo = new DocumentWithMetadata(
            ejpAppealFormDocumentTwo,
            "",
            "30/10/2323",
            DocumentTag.INTERNAL_EJP_DOCUMENT
    );

    private final DocumentWithMetadata someTribunalMeta = new DocumentWithMetadata(
            existingDocumentOne,
            "some description",
        "21/07/2021",
            DocumentTag.APPEAL_SUBMISSION,
            "some supplier"
    );

    private List<IdValue<Document>> allUtTransferDocuments;
    private List<IdValue<Document>> allEjpAppealFormDocuments;
    private List<IdValue<DocumentWithMetadata>> existingTribunalDocuments;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    private UploadEjpDocumentsHandler uploadEjpDocumentsHandler;

    @BeforeEach
    public void setUp() {
        uploadEjpDocumentsHandler =
                new UploadEjpDocumentsHandler(documentReceiver,documentsAppender);

        allUtTransferDocuments = Arrays.asList(
                new IdValue<>("1", someUtDocumentOne),
                new IdValue<>("2", someUtDocumentTwo)
        );

        allEjpAppealFormDocuments = Arrays.asList(
                new IdValue<>("1", ejpAppealFormDocumentOne),
                new IdValue<>("2", ejpAppealFormDocumentTwo)
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenName));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(refNumber));
        when(asylumCase.read(UT_TRANSFER_DOC)).thenReturn(Optional.of(allUtTransferDocuments));
        when(asylumCase.read(UPLOAD_EJP_APPEAL_FORM_DOCS)).thenReturn(Optional.of(allEjpAppealFormDocuments));
    }

    @Test
    void should_append_ejp_documents_to_tribunal_documents() {
        existingTribunalDocuments = List.of(
                new IdValue<>("1", someTribunalMeta)
        );

        List<IdValue<DocumentWithMetadata>> completeTribunalDocuments = Arrays.asList(
                new IdValue<>("1", someTribunalMeta),
                new IdValue<>("2", someUtDocumentMetadataOne),
                new IdValue<>("3", someUtDocumentMetadataTwo),
                new IdValue<>("4", ejpAppealFormDocumentMetadataOne),
                new IdValue<>("5", ejpAppealFormDocumentMetadataTwo)
        );

        List<DocumentWithMetadata> docsWithMetadata =
                Arrays.asList(someUtDocumentMetadataOne, someUtDocumentMetadataTwo,
                        ejpAppealFormDocumentMetadataOne, ejpAppealFormDocumentMetadataTwo);

        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(existingTribunalDocuments));

        when(documentReceiver.receive(someUtDocumentOne, "", DocumentTag.INTERNAL_EJP_DOCUMENT))
                .thenReturn(someUtDocumentMetadataOne);
        when(documentReceiver.receive(someUtDocumentTwo, "", DocumentTag.INTERNAL_EJP_DOCUMENT))
                .thenReturn(someUtDocumentMetadataTwo);
        when(documentReceiver.receive(ejpAppealFormDocumentOne, "", DocumentTag.INTERNAL_EJP_DOCUMENT))
                .thenReturn(ejpAppealFormDocumentMetadataOne);
        when(documentReceiver.receive(ejpAppealFormDocumentTwo, "", DocumentTag.INTERNAL_EJP_DOCUMENT))
                .thenReturn(ejpAppealFormDocumentMetadataTwo);

        when(documentsAppender.prepend(existingTribunalDocuments, docsWithMetadata)).thenReturn(completeTribunalDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = uploadEjpDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).prepend(
                existingTribunalDocuments, docsWithMetadata
        );

        assertEquals(completeTribunalDocuments.get(1).getValue().getDocument().getDocumentFilename(),
                refNumber + "-" + appellantGivenName + "-" + utTransferOrderSuffix + "1.pdf");
        assertEquals(completeTribunalDocuments.get(2).getValue().getDocument().getDocumentFilename(),
                refNumber + "-" + appellantGivenName + "-" + utTransferOrderSuffix + "2.pdf");
        assertEquals(completeTribunalDocuments.get(3).getValue().getDocument().getDocumentFilename(),
                refNumber + "-" + appellantGivenName + "-" + iaut2AppealFormSuffix + "1.pdf");
        assertEquals(completeTribunalDocuments.get(4).getValue().getDocument().getDocumentFilename(),
                refNumber + "-" + appellantGivenName + "-" + iaut2AppealFormSuffix + "2.pdf");
    }

    @Test
    void should_throw_for_missing_ut_transfer_order() {

        when(asylumCase.read(UT_TRANSFER_DOC)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("utTransferDoc is not present");
    }

    @Test
    void should_throw_for_missing_ejp_appeal_form() {

        when(asylumCase.read(UPLOAD_EJP_APPEAL_FORM_DOCS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("uploadEjpAppealFormDocs is not present");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadEjpDocumentsHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.SUBMIT_APPEAL
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

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadEjpDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> uploadEjpDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}