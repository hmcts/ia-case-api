package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOADED_HOME_OFFICE_BUNDLE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE;

import java.util.Arrays;
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

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class UploadHomeOfficeBundlePreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DocumentWithMetadata respondentEvidence1WithMetadata;
    @Mock private DocumentWithMetadata respondentEvidence2WithMetadata;
    @Mock private Document document1;
    @Mock private Document document2;
    private final String evidence01FileName = "Evidence01";
    private final String evidence02FileName = "Evidence02";
    @Captor private ArgumentCaptor<String> fileNames;


    private UploadHomeOfficeBundlePreparer uploadHomeOfficeBundlePreparer;

    @Before
    public void setUp() {

        uploadHomeOfficeBundlePreparer =
            new UploadHomeOfficeBundlePreparer();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HOME_OFFICE_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        List<IdValue<DocumentWithMetadata>> respondentDocuments =

            Arrays.asList(
                new IdValue<>("1", respondentEvidence1WithMetadata),
                new IdValue<>("2", respondentEvidence2WithMetadata)
            );
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(respondentDocuments));

        when(respondentEvidence1WithMetadata.getTag()).thenReturn(DocumentTag.RESPONDENT_EVIDENCE);
        when(respondentEvidence2WithMetadata.getTag()).thenReturn(DocumentTag.RESPONDENT_EVIDENCE);

        when(respondentEvidence1WithMetadata.getDocument()).thenReturn(document1);
        when(respondentEvidence2WithMetadata.getDocument()).thenReturn(document2);

        when(document1.getDocumentFilename()).thenReturn(evidence01FileName);
        when(document2.getDocumentFilename()).thenReturn(evidence02FileName);

    }


    @Test
    public void should_set_errors_if_upload_action_is_not_available_for_home_office_event() {

        when(asylumCase.read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertTrue(callbackResponse.getErrors().contains("You cannot upload more documents until the evidence bundle has been reviewed"));
        verify(asylumCase).read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE);
    }

    @Test
    public void should_not_set_errors_if_upload_action_is_not_available_for_case_officer_event() {

        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(asylumCase.read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse.getErrors()).hasSize(0);
        verify(asylumCase).read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE);
        verify(asylumCase, times(1)).read(RESPONDENT_DOCUMENTS);
        verify(respondentEvidence1WithMetadata, times(1)).getTag();
        verify(respondentEvidence2WithMetadata, times(1)).getTag();
        verify(document1, times(1)).getDocumentFilename();
        verify(document2, times(1)).getDocumentFilename();
        verify(asylumCase).write(eq(UPLOADED_HOME_OFFICE_BUNDLE_DOCS), fileNames.capture());

        final String value = fileNames.getValue();
        assertTrue(value.contains(evidence01FileName));
        assertTrue(value.contains(evidence02FileName));

    }

    @Test
    public void should_not_set_errors_if_upload_action_is_available_for_case_officer_event() {

        when(callback.getEvent()).thenReturn(Event.UPLOAD_RESPONDENT_EVIDENCE);
        when(asylumCase.read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse.getErrors()).hasSize(0);
        verify(asylumCase).read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE);
        verify(asylumCase, times(1)).read(RESPONDENT_DOCUMENTS);
        verifyNoInteractions(respondentEvidence1WithMetadata);
        verifyNoInteractions(respondentEvidence2WithMetadata);
        verifyNoInteractions(document1);
        verifyNoInteractions(document2);
        verify(asylumCase).write(eq(UPLOADED_HOME_OFFICE_BUNDLE_DOCS), fileNames.capture());

        final String value = fileNames.getValue();
        assertTrue(value.contains("- None"));

    }

    @Test
    public void should_set_uploaded_documents() {

        when(asylumCase.read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse.getErrors()).hasSize(0);

        verify(asylumCase).read(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE);
        verify(asylumCase, times(1)).read(RESPONDENT_DOCUMENTS);
        verify(respondentEvidence1WithMetadata, times(1)).getTag();
        verify(respondentEvidence2WithMetadata, times(1)).getTag();
        verify(document1, times(1)).getDocumentFilename();
        verify(document2, times(1)).getDocumentFilename();
        verify(asylumCase).write(eq(UPLOADED_HOME_OFFICE_BUNDLE_DOCS), fileNames.capture());

        final String value = fileNames.getValue();
        assertTrue(value.contains(evidence01FileName));
        assertTrue(value.contains(evidence02FileName));

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = uploadHomeOfficeBundlePreparer.canHandle(callbackStage, callback);

                if ((callback.getEvent() == Event.UPLOAD_HOME_OFFICE_BUNDLE || callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

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

        assertThatThrownBy(() -> uploadHomeOfficeBundlePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeBundlePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeBundlePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
