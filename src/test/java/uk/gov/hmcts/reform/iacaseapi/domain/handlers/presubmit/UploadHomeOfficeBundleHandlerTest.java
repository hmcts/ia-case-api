package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadHomeOfficeBundleHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DocumentWithDescription respondentEvidence1;
    @Mock private DocumentWithDescription respondentEvidence2;
    @Mock private DocumentWithMetadata respondentEvidence1WithMetadata;
    @Mock private DocumentWithMetadata respondentEvidence2WithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingRespondentDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allRespondentDocuments;

    @Captor ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingRespondentDocumentsCaptor;

    UploadHomeOfficeBundleHandler uploadHomeOfficeBundleHandler;

    @BeforeEach
    void setUp() {

        uploadHomeOfficeBundleHandler =
            new UploadHomeOfficeBundleHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_append_new_evidence_to_existing_respondent_documents_for_the_case() {

        List<IdValue<DocumentWithDescription>> respondentEvidence =
            Arrays.asList(
                new IdValue<>("1", respondentEvidence1),
                new IdValue<>("2", respondentEvidence2)
            );

        List<DocumentWithMetadata> respondentEvidenceWithMetadata =
            Arrays.asList(
                respondentEvidence1WithMetadata,
                respondentEvidence2WithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HOME_OFFICE_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(existingRespondentDocuments));
        when(asylumCase.read(HOME_OFFICE_BUNDLE)).thenReturn(Optional.of(respondentEvidence));

        when(documentReceiver.tryReceive(respondentEvidence1, DocumentTag.RESPONDENT_EVIDENCE))
            .thenReturn(Optional.of(respondentEvidence1WithMetadata));

        when(documentReceiver.tryReceive(respondentEvidence2, DocumentTag.RESPONDENT_EVIDENCE))
            .thenReturn(Optional.of(respondentEvidence2WithMetadata));

        when(documentsAppender.append(existingRespondentDocuments, respondentEvidenceWithMetadata))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadHomeOfficeBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(HOME_OFFICE_BUNDLE);

        verify(documentReceiver, times(1)).tryReceive(respondentEvidence1, DocumentTag.RESPONDENT_EVIDENCE);
        verify(documentReceiver, times(1)).tryReceive(respondentEvidence2, DocumentTag.RESPONDENT_EVIDENCE);

        verify(documentsAppender, times(1)).append(existingRespondentDocuments, respondentEvidenceWithMetadata);

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void should_add_new_evidence_to_the_case_when_no_respondent_documents_exist() {

        List<IdValue<DocumentWithDescription>> respondentEvidence = singletonList(new IdValue<>("1", respondentEvidence1));
        List<DocumentWithMetadata> respondentEvidenceWithMetadata = singletonList(respondentEvidence1WithMetadata);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HOME_OFFICE_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_BUNDLE)).thenReturn(Optional.of(respondentEvidence));

        when(documentReceiver.tryReceive(respondentEvidence1, DocumentTag.RESPONDENT_EVIDENCE))
            .thenReturn(Optional.of(respondentEvidence1WithMetadata));

        when(documentsAppender.append(any(List.class), eq(respondentEvidenceWithMetadata)))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadHomeOfficeBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(HOME_OFFICE_BUNDLE);

        verify(documentReceiver, times(1)).tryReceive(respondentEvidence1, DocumentTag.RESPONDENT_EVIDENCE);

        verify(documentsAppender, times(1)).append(existingRespondentDocumentsCaptor.capture(), eq(respondentEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> actualExistingRespondentDocuments =
            existingRespondentDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingRespondentDocuments.size());

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void should_throw_when_new_evidence_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HOME_OFFICE_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(HOME_OFFICE_BUNDLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("respondentEvidence is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadHomeOfficeBundleHandler.canHandle(callbackStage, callback);

                if (event == Event.UPLOAD_HOME_OFFICE_BUNDLE
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

        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
