package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HO_HAS_IMA_STATUS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HO_SELECT_IMA_STATUS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_DOCS;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UploadBailSummaryDocumentHandlerTest {

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
    private DocumentWithDescription bailSummary1;
    @Mock
    private DocumentWithDescription bailSummary2;
    @Mock
    private DocumentWithMetadata bailSummary1WithMetadata;
    @Mock
    private DocumentWithMetadata bailSummary2WithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingBailSummaryDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allBailSummaryDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingBailSummaryDocumentsCaptor;

    private UploadBailSummaryDocumentHandler uploadBailSummaryDocumentHandler;

    @BeforeEach
    public void setUp() {
        uploadBailSummaryDocumentHandler =
            new UploadBailSummaryDocumentHandler(
                documentReceiver,
                documentsAppender
            );
        when(callback.getEvent()).thenReturn(Event.UPLOAD_BAIL_SUMMARY);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void should_append_new_summaries_to_existing_applicant_documents_for_the_case() {

        List<IdValue<DocumentWithDescription>> summaryWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", bailSummary1),
                new IdValue<>("2", bailSummary2)
            );

        List<DocumentWithMetadata> summaryList =
            Arrays.asList(
                bailSummary1WithMetadata,
                bailSummary2WithMetadata
            );

        when(bailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(existingBailSummaryDocuments));
        when(bailCase.read(UPLOAD_BAIL_SUMMARY_DOCS)).thenReturn(Optional.of(summaryWithDescriptionList));

        when(documentReceiver.tryReceive(bailSummary1, DocumentTag.BAIL_SUMMARY))
            .thenReturn(Optional.of(bailSummary1WithMetadata));

        when(documentReceiver.tryReceive(bailSummary2, DocumentTag.BAIL_SUMMARY))
            .thenReturn(Optional.of(bailSummary2WithMetadata));

        when(documentsAppender.append(existingBailSummaryDocuments, summaryList))
            .thenReturn(allBailSummaryDocuments);

        when(bailCase.read(HO_SELECT_IMA_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadBailSummaryDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_BAIL_SUMMARY_DOCS);

        verify(documentReceiver, times(1)).tryReceive(bailSummary1, DocumentTag.BAIL_SUMMARY);
        verify(documentReceiver, times(1)).tryReceive(bailSummary2, DocumentTag.BAIL_SUMMARY);

        verify(documentsAppender, times(1)).append(existingBailSummaryDocuments, summaryList);

        verify(bailCase, times(1)).write(HOME_OFFICE_DOCUMENTS_WITH_METADATA, allBailSummaryDocuments);
      
        verify(bailCase, times(1)).clear(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE);

        verify(bailCase, times(1)).write(HO_HAS_IMA_STATUS, YesOrNo.YES);
    }

    @Test
    void should_add_new_summary_to_the_case_when_no_applicant_documents_exist() {

        List<IdValue<DocumentWithDescription>> summaryWithDescriptionList =
            singletonList(new IdValue<>("1", bailSummary1));
        List<DocumentWithMetadata> summaryList = singletonList(bailSummary1WithMetadata);

        when(bailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.empty());
        when(bailCase.read(UPLOAD_BAIL_SUMMARY_DOCS)).thenReturn(Optional.of(summaryWithDescriptionList));

        when(documentReceiver.tryReceive(bailSummary1, DocumentTag.BAIL_SUMMARY))
            .thenReturn(Optional.of(bailSummary1WithMetadata));

        when(documentsAppender.append(any(List.class), eq(summaryList)))
            .thenReturn(allBailSummaryDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadBailSummaryDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_BAIL_SUMMARY_DOCS);

        verify(documentReceiver, times(1)).tryReceive(bailSummary1, DocumentTag.BAIL_SUMMARY);

        verify(documentsAppender, times(1))
            .append(existingBailSummaryDocumentsCaptor.capture(), eq(summaryList));

        List<IdValue<DocumentWithMetadata>> actualExistingSummaryDocuments =
            existingBailSummaryDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingSummaryDocuments.size());

        verify(bailCase, times(1)).write(HOME_OFFICE_DOCUMENTS_WITH_METADATA, allBailSummaryDocuments);
        verify(bailCase, times(1)).clear(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE);
    }

    @Test
    void should_not_change_applicantDocuments_new_summary_is_not_present() {

        when(bailCase.read(UPLOAD_BAIL_SUMMARY_DOCS)).thenReturn(Optional.empty());
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);

        assertDoesNotThrow(() -> uploadBailSummaryDocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        PreSubmitCallbackResponse<BailCase> response = uploadBailSummaryDocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(bailCase, response.getData());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> uploadBailSummaryDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadBailSummaryDocumentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadBailSummaryDocumentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadBailSummaryDocumentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadBailSummaryDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @CsvSource({"YES, YES", "NO, YES", "YES, NO", "NO, NO"})
    void set_ima_status(YesOrNo imaStatus, YesOrNo isImaEnabled) {
        when(bailCase.read(HO_SELECT_IMA_STATUS, YesOrNo.class)).thenReturn(Optional.of(imaStatus));
        when(bailCase.read(IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isImaEnabled));
        PreSubmitCallbackResponse<BailCase> response = uploadBailSummaryDocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        if (isImaEnabled.equals(YesOrNo.YES)) {
            verify(bailCase, times(1)).write(HO_HAS_IMA_STATUS, imaStatus);
        } else {
            verify(bailCase, times(1)).write(HO_HAS_IMA_STATUS, YesOrNo.NO);
        }
    }
}
