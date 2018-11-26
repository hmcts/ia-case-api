package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class UploadRespondentEvidenceHandlerTest {

    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingRespondentDocumentsCaptor;

    private UploadRespondentEvidenceHandler uploadRespondentEvidenceHandler;

    @Before
    public void setUp() {
        uploadRespondentEvidenceHandler =
            new UploadRespondentEvidenceHandler(
                documentsAppender
            );
    }

    @Test
    public void should_append_new_evidence_to_existing_respondent_documents_for_the_case() {

        final List<IdValue<DocumentWithMetadata>> existingRespondentDocuments = new ArrayList<>();
        final List<IdValue<DocumentWithDescription>> respondentEvidence = new ArrayList<>();
        final List<IdValue<DocumentWithMetadata>> allRespondentDocuments = new ArrayList<>();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getRespondentDocuments()).thenReturn(Optional.of(existingRespondentDocuments));
        when(asylumCase.getUploadRespondentEvidence()).thenReturn(Optional.of(respondentEvidence));
        when(documentsAppender.append(
            existingRespondentDocuments,
            respondentEvidence
        )).thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getUploadRespondentEvidence();

        verify(documentsAppender, times(1)).append(
            existingRespondentDocuments,
            respondentEvidence
        );

        verify(asylumCase, times(1)).setRespondentDocuments(allRespondentDocuments);

        verify(asylumCase, times(1)).clearUploadRespondentEvidence();
    }

    @Test
    public void should_add_new_evidence_to_the_case_when_no_respondent_documents_exist() {

        final List<IdValue<DocumentWithDescription>> respondentEvidence = new ArrayList<>();
        final List<IdValue<DocumentWithMetadata>> allRespondentDocuments = new ArrayList<>();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getRespondentDocuments()).thenReturn(Optional.empty());
        when(asylumCase.getUploadRespondentEvidence()).thenReturn(Optional.of(respondentEvidence));
        when(documentsAppender.append(
            any(List.class),
            eq(respondentEvidence)
        )).thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getUploadRespondentEvidence();

        verify(documentsAppender, times(1)).append(
            existingRespondentDocumentsCaptor.capture(),
            eq(respondentEvidence)
        );

        List<IdValue<DocumentWithMetadata>> actualExistingRespondentDocuments =
            existingRespondentDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingRespondentDocuments.size());

        verify(asylumCase, times(1)).setRespondentDocuments(allRespondentDocuments);

        verify(asylumCase, times(1)).clearUploadRespondentEvidence();
    }

    @Test
    public void should_throw_when_new_evidence_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getUploadRespondentEvidence()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("uploadRespondentEvidence is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadRespondentEvidenceHandler.canHandle(callbackStage, callback);

                if (event == Event.UPLOAD_RESPONDENT_EVIDENCE
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

        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
