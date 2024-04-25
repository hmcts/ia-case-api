package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
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
class UploadAppealFormHandlerTest {

    private final Document someDoc = new Document(
        "someurl",
        "somebinaryurl",
        "somefilename.pdf");

    private final DocumentWithDescription someAppealFormDocument = new DocumentWithDescription(
        someDoc,
        "somedescription");

    private final DocumentWithMetadata someTribunalMeta = new DocumentWithMetadata(
        someDoc,
        "some description",
        "21/07/2021",
        DocumentTag.APPEAL_SUBMISSION,
        "some supplier"
    );

    private List<IdValue<DocumentWithDescription>> allAppealFormDocuments;
    private List<IdValue<DocumentWithMetadata>> allTribunalDocuments;

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
    @Mock
    private DocumentWithMetadata appealForm1WithMetadata;
    private UploadAppealFormHandler uploadAppealFormHandler;

    @BeforeEach
    public void setUp() {
        uploadAppealFormHandler =
                new UploadAppealFormHandler(documentReceiver,documentsAppender);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    @Test
    void should_append_appeal_forms_to_tribunal_documents() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        allAppealFormDocuments = Arrays.asList(
            new IdValue<>("1", someAppealFormDocument)
        );

        allTribunalDocuments = Arrays.asList(
            new IdValue<>("1", someTribunalMeta)
        );

        List<DocumentWithMetadata> docsWithMetadata = Arrays.asList(appealForm1WithMetadata);

        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY)).thenReturn(Optional.of("somename"));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));

        when(asylumCase.read(UPLOAD_THE_APPEAL_FORM_DOCS)).thenReturn(Optional.of(allAppealFormDocuments));
        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(allTribunalDocuments));

        when(documentReceiver.tryReceive(someAppealFormDocument, DocumentTag.APPEAL_FORM))
            .thenReturn(Optional.of(appealForm1WithMetadata));

        when(documentsAppender.prepend(allTribunalDocuments, docsWithMetadata)).thenReturn(allTribunalDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = uploadAppealFormHandler.handle(ABOUT_TO_SUBMIT,callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).prepend(
            allTribunalDocuments,
            docsWithMetadata
        );
    }

    @Test
    void should_not_append_appeal_forms_for_ejp() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        allAppealFormDocuments = Arrays.asList(
                new IdValue<>("1", someAppealFormDocument)
        );

        allTribunalDocuments = Arrays.asList(
                new IdValue<>("1", someTribunalMeta)
        );

        List<DocumentWithMetadata> docsWithMetadata = Arrays.asList(appealForm1WithMetadata);

        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY)).thenReturn(Optional.of("somename"));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));

        when(asylumCase.read(UPLOAD_THE_APPEAL_FORM_DOCS)).thenReturn(Optional.of(allAppealFormDocuments));
        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(allTribunalDocuments));

        when(documentReceiver.tryReceive(someAppealFormDocument, DocumentTag.APPEAL_FORM))
                .thenReturn(Optional.of(appealForm1WithMetadata));

        when(documentsAppender.prepend(allTribunalDocuments, docsWithMetadata)).thenReturn(allTribunalDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = uploadAppealFormHandler.handle(ABOUT_TO_SUBMIT,callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, never()).prepend(
                allTribunalDocuments,
                docsWithMetadata
        );
    }


    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadAppealFormHandler.canHandle(callbackStage, callback);

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

        assertThatThrownBy(() -> uploadAppealFormHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAppealFormHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAppealFormHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAppealFormHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadAppealFormHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> uploadAppealFormHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}