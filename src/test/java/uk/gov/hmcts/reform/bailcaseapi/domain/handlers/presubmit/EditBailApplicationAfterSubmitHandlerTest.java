package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class EditBailApplicationAfterSubmitHandlerTest {

    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private DocumentWithMetadata existingB1DocumentDocumentWithMetaData;
    @Mock
    private DocumentWithMetadata existingBailEvidenceDocumentWithMetaData;
    @Mock
    private DocumentWithMetadata existingBailSubmissionDocumentWithMetaData;
    @Mock
    private DocumentWithMetadata existingOtherApplicantDocumentWithMetaData;

    private EditBailApplicationAfterSubmitHandler editBailApplicationAfterSubmitHandler;

    @BeforeEach
    public void setUp() {
        editBailApplicationAfterSubmitHandler =
            new EditBailApplicationAfterSubmitHandler(
                documentReceiver,
                documentsAppender
            );
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(existingB1DocumentDocumentWithMetaData.getTag()).thenReturn(DocumentTag.B1_DOCUMENT);
        when(existingBailEvidenceDocumentWithMetaData.getTag()).thenReturn(DocumentTag.BAIL_EVIDENCE);
        when(existingBailSubmissionDocumentWithMetaData.getTag()).thenReturn(DocumentTag.BAIL_SUBMISSION);
        when(existingOtherApplicantDocumentWithMetaData.getTag()).thenReturn(DocumentTag.APPLICATION_SUBMISSION);
    }

    @Test
    void should_only_remove_applicant_documents_added_during_start_application() {

        List<IdValue<DocumentWithMetadata>> applicantDocumentWithMetadataList =
            List.of(
                new IdValue<>("1", existingB1DocumentDocumentWithMetaData),
                new IdValue<>("2", existingBailEvidenceDocumentWithMetaData),
                new IdValue<>("3", existingBailSubmissionDocumentWithMetaData),
                new IdValue<>("4", existingOtherApplicantDocumentWithMetaData)
            );

        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(applicantDocumentWithMetadataList));

        List<IdValue<DocumentWithMetadata>> updatedApplicantDocumentWithMetadataList =
            applicantDocumentWithMetadataList
                .stream()
                .filter(documentWithMetaData ->
                            !documentWithMetaData.getValue().getTag().equals(DocumentTag.B1_DOCUMENT)
                            && !documentWithMetaData.getValue().getTag().equals(DocumentTag.BAIL_EVIDENCE)
                            && !documentWithMetaData.getValue().getTag().equals(DocumentTag.BAIL_SUBMISSION))
                .collect(Collectors.toList());

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            editBailApplicationAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1))
            .write(APPLICANT_DOCUMENTS_WITH_METADATA, updatedApplicantDocumentWithMetadataList);

        assertEquals(1, updatedApplicantDocumentWithMetadataList.size());

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> editBailApplicationAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> editBailApplicationAfterSubmitHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> editBailApplicationAfterSubmitHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editBailApplicationAfterSubmitHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editBailApplicationAfterSubmitHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT,null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
