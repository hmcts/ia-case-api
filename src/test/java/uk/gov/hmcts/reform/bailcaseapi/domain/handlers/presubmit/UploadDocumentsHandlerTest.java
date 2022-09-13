package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_HOME_OFFICE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_USER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_DOCUMENTS_SUPPLIED_BY;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
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
public class UploadDocumentsHandlerTest {
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
    private DocumentWithDescription homeOfficeDocumentBeingUploaded1;
    @Mock
    private DocumentWithDescription homeOfficeDocumentBeingUploaded2;
    @Mock
    private DocumentWithMetadata homeOfficeDocumentBeingUploaded1WithMetadata;
    @Mock
    private DocumentWithMetadata homeOfficeDocumentBeingUploaded2WithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingHomeOfficeDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allHomeOfficeDocuments;
    @Mock
    private DocumentWithDescription applicantDocumentBeingUploaded1;
    @Mock
    private DocumentWithDescription applicantDocumentBeingUploaded2;
    @Mock
    private DocumentWithMetadata applicantDocumentBeingUploaded1WithMetadata;
    @Mock
    private DocumentWithMetadata applicantDocumentBeingUploaded2WithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingApplicantDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allApplicantDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingHomeOfficeDocumentsCaptor;

    private UploadDocumentsHandler uploadDocumentsDocumentHandler;

    @BeforeEach
    public void setUp() {
        uploadDocumentsDocumentHandler =
            new UploadDocumentsHandler(
                documentReceiver,
                documentsAppender
            );
        when(callback.getEvent()).thenReturn(Event.UPLOAD_DOCUMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void should_allow_home_office_user_to_append_home_office_docs_to_existing_ones() {

        List<IdValue<DocumentWithDescription>> homeOfficeDocumentsWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", homeOfficeDocumentBeingUploaded1),
                new IdValue<>("2", homeOfficeDocumentBeingUploaded2)
            );

        List<DocumentWithMetadata> homeOfficeDocumentsWithMetadataList =
            Arrays.asList(
                homeOfficeDocumentBeingUploaded1WithMetadata,
                homeOfficeDocumentBeingUploaded2WithMetadata
            );

        when(bailCase.read(CURRENT_USER, String.class))
            .thenReturn(Optional.of(UserRoleLabel.HOME_OFFICE_BAIL.toString()));

        when(bailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(existingHomeOfficeDocuments));
        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.of(homeOfficeDocumentsWithDescriptionList));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded1, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded1WithMetadata));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded2, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded2WithMetadata));

        when(documentsAppender.append(existingHomeOfficeDocuments, homeOfficeDocumentsWithMetadataList))
            .thenReturn(allHomeOfficeDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_DOCUMENTS);

        verify(documentReceiver, times(1)).tryReceive(homeOfficeDocumentBeingUploaded1,
                                                      DocumentTag.UPLOAD_DOCUMENT);
        verify(documentReceiver, times(1)).tryReceive(homeOfficeDocumentBeingUploaded2,
                                                      DocumentTag.UPLOAD_DOCUMENT);

        verify(documentsAppender, times(1)).append(existingHomeOfficeDocuments,
                                                   homeOfficeDocumentsWithMetadataList);

        verify(bailCase, times(1)).write(HOME_OFFICE_DOCUMENTS_WITH_METADATA,
                                         allHomeOfficeDocuments);

        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS);
        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS_SUPPLIED_BY);
    }

    @Test
    void should_allow_legal_rep_user_to_append_applicant_docs_to_existing_ones() {

        List<IdValue<DocumentWithDescription>> applicantDocumentsWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", applicantDocumentBeingUploaded1),
                new IdValue<>("2", applicantDocumentBeingUploaded2)
            );

        List<DocumentWithMetadata> applicantDocumentsWithMetadataList =
            Arrays.asList(
                applicantDocumentBeingUploaded1WithMetadata,
                applicantDocumentBeingUploaded2WithMetadata
            );

        when(bailCase.read(CURRENT_USER, String.class))
            .thenReturn(Optional.of(UserRoleLabel.LEGAL_REPRESENTATIVE.toString()));

        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(existingApplicantDocuments));
        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.of(applicantDocumentsWithDescriptionList));

        when(documentReceiver.tryReceive(applicantDocumentBeingUploaded1, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(applicantDocumentBeingUploaded1WithMetadata));

        when(documentReceiver.tryReceive(applicantDocumentBeingUploaded2, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(applicantDocumentBeingUploaded2WithMetadata));

        when(documentsAppender.append(existingApplicantDocuments, applicantDocumentsWithMetadataList))
            .thenReturn(allApplicantDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_DOCUMENTS);

        verify(documentReceiver, times(1)).tryReceive(applicantDocumentBeingUploaded1,
                                                      DocumentTag.UPLOAD_DOCUMENT);
        verify(documentReceiver, times(1)).tryReceive(applicantDocumentBeingUploaded2,
                                                      DocumentTag.UPLOAD_DOCUMENT);

        verify(documentsAppender, times(1)).append(existingApplicantDocuments,
                                                   applicantDocumentsWithMetadataList);

        verify(bailCase, times(1)).write(APPLICANT_DOCUMENTS_WITH_METADATA, allApplicantDocuments);

        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS);
        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS_SUPPLIED_BY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"legalRepresentative", "applicant"})
    void should_allow_admin_or_judge_to_append_applicant_docs_to_existing_ones(String suppliedBy) {

        when(bailCase.read(CURRENT_USER, String.class))
            .thenReturn(Optional.of(UserRoleLabel.ADMIN_OFFICER.toString()));
        when(bailCase.read(UPLOAD_DOCUMENTS_SUPPLIED_BY)).thenReturn(Optional.of(suppliedBy));

        List<IdValue<DocumentWithDescription>> applicantDocumentsWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", applicantDocumentBeingUploaded1),
                new IdValue<>("2", applicantDocumentBeingUploaded2)
            );

        List<DocumentWithMetadata> applicantDocumentsWithMetadataList =
            Arrays.asList(
                applicantDocumentBeingUploaded1WithMetadata,
                applicantDocumentBeingUploaded2WithMetadata
            );

        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(existingApplicantDocuments));
        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.of(applicantDocumentsWithDescriptionList));

        when(documentReceiver.tryReceive(applicantDocumentBeingUploaded1, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(applicantDocumentBeingUploaded1WithMetadata));

        when(documentReceiver.tryReceive(applicantDocumentBeingUploaded2, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(applicantDocumentBeingUploaded2WithMetadata));

        when(documentsAppender.append(existingApplicantDocuments, applicantDocumentsWithMetadataList))
            .thenReturn(allApplicantDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_DOCUMENTS);

        verify(documentReceiver, times(1)).tryReceive(applicantDocumentBeingUploaded1,
                                                      DocumentTag.UPLOAD_DOCUMENT);
        verify(documentReceiver, times(1)).tryReceive(applicantDocumentBeingUploaded2,
                                                      DocumentTag.UPLOAD_DOCUMENT);

        verify(documentsAppender, times(1)).append(existingApplicantDocuments,
                                                   applicantDocumentsWithMetadataList);

        verify(bailCase, times(1)).write(APPLICANT_DOCUMENTS_WITH_METADATA, allApplicantDocuments);

        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS);
        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS_SUPPLIED_BY);
    }

    @Test
    void should_allow_admin_or_judge_to_append_home_office_docs_to_existing_ones() {

        when(bailCase.read(CURRENT_USER, String.class))
            .thenReturn(Optional.of(UserRoleLabel.ADMIN_OFFICER.toString()));
        String suppliedByHomeOffice = "homeOffice";
        when(bailCase.read(UPLOAD_DOCUMENTS_SUPPLIED_BY)).thenReturn(Optional.of(suppliedByHomeOffice));

        List<IdValue<DocumentWithDescription>> homeOfficeDocumentsWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", homeOfficeDocumentBeingUploaded1),
                new IdValue<>("2", homeOfficeDocumentBeingUploaded2)
            );

        List<DocumentWithMetadata> homeOfficeDocumentsWithMetadataList =
            Arrays.asList(
                homeOfficeDocumentBeingUploaded1WithMetadata,
                homeOfficeDocumentBeingUploaded2WithMetadata
            );

        when(bailCase.read(IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(existingHomeOfficeDocuments));
        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.of(homeOfficeDocumentsWithDescriptionList));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded1, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded1WithMetadata));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded2, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded2WithMetadata));

        when(documentsAppender.append(existingHomeOfficeDocuments, homeOfficeDocumentsWithMetadataList))
            .thenReturn(allHomeOfficeDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_DOCUMENTS);

        verify(documentReceiver, times(1)).tryReceive(homeOfficeDocumentBeingUploaded1,
                                                      DocumentTag.UPLOAD_DOCUMENT);
        verify(documentReceiver, times(1)).tryReceive(homeOfficeDocumentBeingUploaded2,
                                                      DocumentTag.UPLOAD_DOCUMENT);

        verify(documentsAppender, times(1)).append(existingHomeOfficeDocuments,
                                                   homeOfficeDocumentsWithMetadataList);

        verify(bailCase, times(1)).write(HOME_OFFICE_DOCUMENTS_WITH_METADATA,
                                         allHomeOfficeDocuments);

        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS);
        verify(bailCase, times(1)).clear(UPLOAD_DOCUMENTS_SUPPLIED_BY);
    }

    @Test
    void should_not_change_uploaded_documents_if_not_present() {

        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.empty());
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);

        assertDoesNotThrow(() -> uploadDocumentsDocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        PreSubmitCallbackResponse<BailCase> response = uploadDocumentsDocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(bailCase, response.getData());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadDocumentsDocumentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadDocumentsDocumentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadDocumentsDocumentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_exception_if_supplier_is_invalid_value() {

        String wrongSupplier = "wrong value"; //valid values are: legalRepresentative, homeOffice, Applicant
        when(bailCase.read(UPLOAD_DOCUMENTS_SUPPLIED_BY)).thenReturn(Optional.of(wrongSupplier));
        when(bailCase.read(CURRENT_USER, String.class))
            .thenReturn(Optional.of(UserRoleLabel.ADMIN_OFFICER.toString()));

        List<IdValue<DocumentWithDescription>> homeOfficeDocumentsWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", homeOfficeDocumentBeingUploaded1),
                new IdValue<>("2", homeOfficeDocumentBeingUploaded2)
            );

        List<DocumentWithMetadata> homeOfficeDocumentsWithMetadataList =
            Arrays.asList(
                homeOfficeDocumentBeingUploaded1WithMetadata,
                homeOfficeDocumentBeingUploaded2WithMetadata
            );

        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.of(homeOfficeDocumentsWithDescriptionList));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded1, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded1WithMetadata));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded2, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded2WithMetadata));

        assertThatThrownBy(() -> uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                                                                       callback))
            .hasMessage("Unable to determine the supplier of the document")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(documentsAppender, never()).append(existingHomeOfficeDocuments,
                                                   homeOfficeDocumentsWithMetadataList);

        verify(bailCase, never()).write(HOME_OFFICE_DOCUMENTS_WITH_METADATA, allHomeOfficeDocuments);
    }

    @Test
    void should_throw_exception_if_current_user_cannot_be_determined() {

        String suppliedBy = "applicant"; //valid value
        when(bailCase.read(UPLOAD_DOCUMENTS_SUPPLIED_BY)).thenReturn(Optional.of(suppliedBy));
        when(bailCase.read(CURRENT_USER, String.class))
            .thenReturn(Optional.empty()); //should always be populated

        List<IdValue<DocumentWithDescription>> homeOfficeDocumentsWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", homeOfficeDocumentBeingUploaded1),
                new IdValue<>("2", homeOfficeDocumentBeingUploaded2)
            );

        List<DocumentWithMetadata> homeOfficeDocumentsWithMetadataList =
            Arrays.asList(
                homeOfficeDocumentBeingUploaded1WithMetadata,
                homeOfficeDocumentBeingUploaded2WithMetadata
            );

        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(UPLOAD_DOCUMENTS)).thenReturn(Optional.of(homeOfficeDocumentsWithDescriptionList));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded1, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded1WithMetadata));

        when(documentReceiver.tryReceive(homeOfficeDocumentBeingUploaded2, DocumentTag.UPLOAD_DOCUMENT))
            .thenReturn(Optional.of(homeOfficeDocumentBeingUploaded2WithMetadata));

        assertThatThrownBy(() -> uploadDocumentsDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                                                                       callback))
            .hasMessage("Unable to determine the supplier of the document")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(documentsAppender, never()).append(existingHomeOfficeDocuments,
                                                  homeOfficeDocumentsWithMetadataList);

        verify(bailCase, never()).write(HOME_OFFICE_DOCUMENTS_WITH_METADATA, allHomeOfficeDocuments);
    }
}
