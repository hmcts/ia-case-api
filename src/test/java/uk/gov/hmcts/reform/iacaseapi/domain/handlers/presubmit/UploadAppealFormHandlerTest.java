package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.APPEAL_FORM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.HO_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@MockitoSettings(strictness=Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadAppealFormHandlerTest{

    private final Document someDoc = new Document(
        "someurl",
        "somebinaryurl",
        "somefilename.pdf");

    private final DocumentWithMetadata someTribunalDocument = new DocumentWithMetadata(
        someDoc,
        "somedescription",
        "21/07/2021",
        APPEAL_FORM,
        "somesupplier"
    );

    private final DocumentWithMetadata homeOfficeDecisionLetter = new DocumentWithMetadata(
        someDoc,
        "thehomeofficedecisionletter",
        "21/07/2021",
        DocumentTag.HO_DECISION_LETTER,
        "thehomeoffice"
    );

    private final Document someAppealdoc1 = new Document(
            "someurl",
            "somebinaryurl",
            "somefilename.pdf");

    private final DocumentWithDescription someAppealForm1 = new DocumentWithDescription(
            someAppealdoc1,
            "somedescription");

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails <AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DocumentWithDescription noticeOfDecision1;
    @Mock
    private DocumentWithMetadata noticeOfDecision1WithMetadata;

    private List<IdValue<DocumentWithMetadata>>allTribunalDocuments;
    private DocumentWithDescription appealForm1;
    private DocumentWithMetadata appealForm1WithMetadata;
    private UploadAppealFormHandler uploadAppealFormHandler;

    @BeforeEach
    public void setUp(){

        uploadAppealFormHandler=
        new UploadAppealFormHandler(documentReceiver,documentsAppender);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value=Event.class,names={"START_APPEAL"})
    void should_append_home_office_decision_letter_to_legal_rep_documents_if_not_present(Event event){

        when(callback.getEvent()).thenReturn(event);

        allTribunalDocuments = Arrays.asList(
            new IdValue<>("1",someTribunalDocument)
        );

        List<IdValue<DocumentWithDescription>> noticeOfDecisionDocument = Arrays.asList(
            new IdValue<>("1",noticeOfDecision1)
        );

        List<DocumentWithMetadata> noticeOfDecisionWithMetadata = Arrays.asList(
            noticeOfDecision1WithMetadata
        );

        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY)).thenReturn(Optional.of("somename"));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));
        when(asylumCase.read(UPLOAD_THE_APPEAL_FORM_DOCS)).thenReturn(Optional.of(someAppealForm1));
        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(allTribunalDocuments));
//        when(FilenameUtils.getExtension(noticeOfDecisionDocument.get(0).getValue().getDocument().get().getDocumentFilename())).thenReturn(".pdf");

        when(documentReceiver.tryReceive(noticeOfDecision1,APPEAL_FORM))
        .thenReturn(Optional.of(noticeOfDecision1WithMetadata));

        when(documentsAppender.prepend(allTribunalDocuments,noticeOfDecisionWithMetadata))
        .thenReturn(allTribunalDocuments);

        PreSubmitCallbackResponse<AsylumCase>callbackResponse=uploadAppealFormHandler.handle(ABOUT_TO_SUBMIT,callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender,times(1)).prepend(
            allTribunalDocuments,
            noticeOfDecisionWithMetadata
        );
    }


    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                when(callback.getEvent()).thenReturn(event);

                boolean canHandle = uploadAppealFormHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.START_APPEAL
                ) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }

        reset(callback);
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