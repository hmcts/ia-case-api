package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESP_ADDITIONAL_EVIDENCE_DOCS;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UploadAdditionalEvidenceHomeOfficeHandlerTest {

    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentWithDescription additionalEvidenceDoc;
    @Mock
    private DocumentWithMetadata additionalEvidenceDocWithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> respondentDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allRespondentDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingAdditionalEvidenceDocumentsCaptor;

    private UploadAdditionalEvidenceHomeOfficeHandler uploadAdditionalEvidenceHomeOfficeHandler;

    private List<IdValue<DocumentWithDescription>> additionalEvidence;
    private List<DocumentWithMetadata> additionalEvidenceWithMetadata;

    @BeforeEach
    public void setUp() {

        additionalEvidence = Arrays.asList(new IdValue<>("1", additionalEvidenceDoc));
        additionalEvidenceWithMetadata = Arrays.asList(additionalEvidenceDocWithMetadata);

        uploadAdditionalEvidenceHomeOfficeHandler =
            new UploadAdditionalEvidenceHomeOfficeHandler(
                documentReceiver,
                documentsAppender,
                featureToggler
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_append_new_evidence_to_existing_additional_evidence_documents_for_a_reheard_case() {
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(respondentDocuments));
        when(asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS)).thenReturn(Optional.of(respondentDocuments));
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

        verify(documentsAppender, times(2)).append(respondentDocuments, additionalEvidenceWithMetadata);

        verify(asylumCase, times(1)).write(RESP_ADDITIONAL_EVIDENCE_DOCS, allRespondentDocuments);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).clear(ADDITIONAL_EVIDENCE_HOME_OFFICE);

    }

    @Test
    public void should_append_new_evidence_to_existing_additional_evidence_documents_for_the_case() {

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
    public void should_add_new_evidence_to_a_reheard_case_when_no_additional_evidence_documents_exist() {
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        when(asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS)).thenReturn(Optional.empty());
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

        verify(documentsAppender, times(2))
            .append(existingAdditionalEvidenceDocumentsCaptor.capture(), eq(additionalEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> actualExistingAdditionalDocuments =
            existingAdditionalEvidenceDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingAdditionalDocuments.size());

        verify(asylumCase, times(1)).write(RESP_ADDITIONAL_EVIDENCE_DOCS, allRespondentDocuments);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).clear(ADDITIONAL_EVIDENCE_HOME_OFFICE);

    }

    @Test
    public void should_add_new_evidence_to_the_case_when_no_additional_evidence_documents_exist() {

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

        verify(documentsAppender, times(1))
            .append(existingAdditionalEvidenceDocumentsCaptor.capture(), eq(additionalEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> actualExistingAdditionalDocuments =
            existingAdditionalEvidenceDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingAdditionalDocuments.size());

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).clear(ADDITIONAL_EVIDENCE_HOME_OFFICE);
    }

    @Test
    public void should_throw_when_new_evidence_is_not_present() {

        when(asylumCase.read(ADDITIONAL_EVIDENCE_HOME_OFFICE)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("additionalEvidenceHomeOffice is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        assertThatThrownBy(
            () -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadAdditionalEvidenceHomeOfficeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadAdditionalEvidenceHomeOfficeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadAdditionalEvidenceHomeOfficeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
