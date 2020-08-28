package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadAdditionalEvidenceHomeOfficeHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DocumentWithDescription additionalEvidenceDoc;
    @Mock private DocumentWithMetadata additionalEvidenceDocWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> respondentDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allRespondentDocuments;

    @Captor ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingAdditionalEvidenceDocumentsCaptor;

    UploadAdditionalEvidenceHomeOfficeHandler uploadAdditionalEvidenceHomeOfficeHandler;

    List<IdValue<DocumentWithDescription>> additionalEvidence;
    List<DocumentWithMetadata> additionalEvidenceWithMetadata;

    @BeforeEach
    void setUp() {

        additionalEvidence = Arrays.asList(new IdValue<>("1", additionalEvidenceDoc));
        additionalEvidenceWithMetadata = Arrays.asList(additionalEvidenceDocWithMetadata);

        uploadAdditionalEvidenceHomeOfficeHandler =
            new UploadAdditionalEvidenceHomeOfficeHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_append_new_evidence_to_existing_additional_evidence_documents_for_the_case() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(respondentDocuments));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_HOME_OFFICE)).thenReturn(Optional.of(additionalEvidence));

        when(documentReceiver.tryReceive(additionalEvidenceDoc, DocumentTag.ADDITIONAL_EVIDENCE))
            .thenReturn(Optional.of(additionalEvidenceDocWithMetadata));

        when(documentsAppender.append(respondentDocuments, additionalEvidenceWithMetadata))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(ADDITIONAL_EVIDENCE_HOME_OFFICE);

        verify(documentReceiver, times(1)).tryReceive(additionalEvidenceDoc, DocumentTag.ADDITIONAL_EVIDENCE);

        verify(documentsAppender, times(1)).append(respondentDocuments, additionalEvidenceWithMetadata);

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).clear(ADDITIONAL_EVIDENCE_HOME_OFFICE);
    }

    @Test
    void should_add_new_evidence_to_the_case_when_no_additional_evidence_documents_exist() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_EVIDENCE_HOME_OFFICE)).thenReturn(Optional.of(additionalEvidence));

        when(documentReceiver.tryReceive(additionalEvidenceDoc, DocumentTag.ADDITIONAL_EVIDENCE))
            .thenReturn(Optional.of(additionalEvidenceDocWithMetadata));

        when(documentsAppender.append(any(List.class), eq(additionalEvidenceWithMetadata)))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(ADDITIONAL_EVIDENCE_HOME_OFFICE);

        verify(documentReceiver, times(1)).tryReceive(additionalEvidenceDoc, DocumentTag.ADDITIONAL_EVIDENCE);

        verify(documentsAppender, times(1)).append(existingAdditionalEvidenceDocumentsCaptor.capture(), eq(additionalEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> actualExistingAdditionalDocuments =
            existingAdditionalEvidenceDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingAdditionalDocuments.size());

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).clear(ADDITIONAL_EVIDENCE_HOME_OFFICE);
    }

    @Test
    void should_throw_when_new_evidence_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(ADDITIONAL_EVIDENCE_HOME_OFFICE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("additionalEvidenceHomeOffice is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadAdditionalEvidenceHomeOfficeHandler.canHandle(callbackStage, callback);

                if (event == Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
