package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;

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
class UploadAddendumEvidenceLegalRepHomeOfficeHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DocumentWithDescription additionalEvidence1;
    @Mock private DocumentWithDescription additionalEvidence2;
    @Mock private DocumentWithMetadata additionalEvidence1WithMetadata;
    @Mock private DocumentWithMetadata additionalEvidence2WithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingAddendumEvidenceDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allAddendumEvidenceDocuments;

    @Captor ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingAdditionalEvidenceDocumentsCaptor;

    UploadAddendumEvidenceLegalRepHomeOfficeHandler uploadAddendumEvidenceLegalRepHomeOfficeHandler;

    @BeforeEach
    void setUp() {

        uploadAddendumEvidenceLegalRepHomeOfficeHandler =
            new UploadAddendumEvidenceLegalRepHomeOfficeHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_append_new_evidence_to_existing_additional_evidence_documents_for_the_case_ho() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        String party = "The respondent";

        addAddendumEvidenceToExistingEvidences(party);
    }

    @Test
    void should_append_new_evidence_to_existing_additional_evidence_documents_for_the_case_legal_rep() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        String party = "The appellant";

        addAddendumEvidenceToExistingEvidences(party);
    }

    @Test
    void should_add_new_evidence_to_the_case_when_no_additional_evidence_documents_exist_ho() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        String party = "The respondent";

        addAddendumEvidenceAsNewList(party);
    }

    @Test
    void should_add_new_evidence_to_the_case_when_no_additional_evidence_documents_exist_legal_rep() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        String party = "The appellant";

        addAddendumEvidenceAsNewList(party);
    }

    @Test
    void should_throw_when_new_evidence_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(ADDENDUM_EVIDENCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("additionalEvidence is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadAddendumEvidenceLegalRepHomeOfficeHandler.canHandle(callbackStage, callback);

                if ((event == Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE || event == Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP)
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

        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    void addAddendumEvidenceToExistingEvidences(String party) {
        List<IdValue<DocumentWithDescription>> additionalEvidence =
            Arrays.asList(
                new IdValue<>("1", additionalEvidence1),
                new IdValue<>("2", additionalEvidence2)
            );

        List<DocumentWithMetadata> additionalEvidenceWithMetadata =
            Arrays.asList(
                additionalEvidence1WithMetadata,
                additionalEvidence2WithMetadata
            );

        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(existingAddendumEvidenceDocuments));
        when(asylumCase.read(ADDENDUM_EVIDENCE)).thenReturn(Optional.of(additionalEvidence));

        when(documentReceiver.tryReceive(additionalEvidence1, DocumentTag.ADDENDUM_EVIDENCE, party))
            .thenReturn(Optional.of(additionalEvidence1WithMetadata));

        when(documentReceiver.tryReceive(additionalEvidence2, DocumentTag.ADDENDUM_EVIDENCE, party))
            .thenReturn(Optional.of(additionalEvidence2WithMetadata));

        when(documentsAppender.append(existingAddendumEvidenceDocuments, additionalEvidenceWithMetadata))
            .thenReturn(allAddendumEvidenceDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(ADDENDUM_EVIDENCE);

        verify(documentReceiver, times(1)).tryReceive(additionalEvidence1, DocumentTag.ADDENDUM_EVIDENCE, party);
        verify(documentReceiver, times(1)).tryReceive(additionalEvidence2, DocumentTag.ADDENDUM_EVIDENCE, party);

        verify(documentsAppender, times(1)).append(existingAddendumEvidenceDocuments, additionalEvidenceWithMetadata);

        verify(asylumCase, times(1)).write(ADDENDUM_EVIDENCE_DOCUMENTS, allAddendumEvidenceDocuments);
        verify(asylumCase, times(1)).clear(ADDENDUM_EVIDENCE);
    }

    void addAddendumEvidenceAsNewList(String party) {
        List<IdValue<DocumentWithDescription>> additionalEvidence = Arrays.asList(new IdValue<>("1", additionalEvidence1));
        List<DocumentWithMetadata> additionalEvidenceWithMetadata = Arrays.asList(additionalEvidence1WithMetadata);

        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDENDUM_EVIDENCE)).thenReturn(Optional.of(additionalEvidence));

        when(documentReceiver.tryReceive(additionalEvidence1, DocumentTag.ADDENDUM_EVIDENCE, party))
            .thenReturn(Optional.of(additionalEvidence1WithMetadata));

        when(documentsAppender.append(any(List.class), eq(additionalEvidenceWithMetadata)))
            .thenReturn(allAddendumEvidenceDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadAddendumEvidenceLegalRepHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(ADDENDUM_EVIDENCE);

        verify(documentReceiver, times(1)).tryReceive(additionalEvidence1, DocumentTag.ADDENDUM_EVIDENCE, party);

        verify(documentsAppender, times(1)).append(existingAdditionalEvidenceDocumentsCaptor.capture(), eq(additionalEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> actualExistingAdditionalDocuments =
            existingAdditionalEvidenceDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingAdditionalDocuments.size());

        verify(asylumCase, times(1)).write(ADDENDUM_EVIDENCE_DOCUMENTS, allAddendumEvidenceDocuments);
        verify(asylumCase, times(1)).clear(ADDENDUM_EVIDENCE);
    }
}
