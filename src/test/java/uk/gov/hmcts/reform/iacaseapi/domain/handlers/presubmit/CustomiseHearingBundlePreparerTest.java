package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_LEGAL_REP_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.ArrayList;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CustomiseHearingBundlePreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Appender<DocumentWithDescription> appender;

    @Captor
    ArgumentCaptor<DocumentWithDescription> legalRepresentativeDocumentsCaptor;

    CustomiseHearingBundlePreparer customiseHearingBundlePreparer;

    @BeforeEach
    void setUp() {
        customiseHearingBundlePreparer =
                new CustomiseHearingBundlePreparer(appender);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_create_custom_collections() {
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);

        DocumentWithDescription customDocument = new DocumentWithDescription(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description"
        );
        List<IdValue<DocumentWithDescription>> customCollections = asList(
                new IdValue<DocumentWithDescription>(
                        "1",
                        customDocument
                )
        );



        DocumentWithMetadata hearingDocument = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.HEARING_NOTICE
        );
        List<IdValue<DocumentWithMetadata>> hearingDocumentList = asList(
                new IdValue<DocumentWithMetadata>(
                        "1",
                        hearingDocument
                )
        );
        DocumentWithMetadata legalDocument = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.CASE_ARGUMENT
        );
        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
                new IdValue<DocumentWithMetadata>(
                        "1",
                        legalDocument
                ),
                new IdValue<DocumentWithMetadata>(
                        "2",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.APPEAL_SUBMISSION
                        )
                ),
                new IdValue<DocumentWithMetadata>(
                        "3",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.CASE_SUMMARY
                        )
                )
        );

        DocumentWithMetadata additionalEvidenceDocument = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.ADDITIONAL_EVIDENCE
        );

        List<IdValue<DocumentWithMetadata>> additionalEvidenceList = asList(
                new IdValue<DocumentWithMetadata>(
                        "1",
                        additionalEvidenceDocument
                )
        );

        DocumentWithMetadata respondentDocument = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.RESPONDENT_EVIDENCE
        );

        List<IdValue<DocumentWithMetadata>> respondentList = asList(
                new IdValue<DocumentWithMetadata>(
                        "1",
                        respondentDocument
                )
        );

        when(appender.append(any(DocumentWithDescription.class), anyList()))
                .thenReturn(customCollections);

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_DOCUMENTS))
                .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS))
                .thenReturn(Optional.of(legalDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS))
                .thenReturn(Optional.of(additionalEvidenceList));

        when(asylumCase.read(AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS))
                .thenReturn(Optional.of(respondentList));

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(AsylumCaseFieldDefinition.CUSTOM_HEARING_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);
        verify(asylumCase).write(AsylumCaseFieldDefinition.CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, customCollections);
        verify(asylumCase).write(AsylumCaseFieldDefinition.CUSTOM_RESPONDENT_DOCUMENTS, customCollections);
    }

    @Test
    void should_filter_legal_rep_document_with_correct_tags() {

        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);

        List<IdValue<DocumentWithDescription>> customCollections = new ArrayList<>();

        when(appender.append(any(DocumentWithDescription.class), anyList()))
                .thenReturn(customCollections);



        DocumentWithMetadata legalDocument = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.CASE_ARGUMENT
        );
        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
                new IdValue<DocumentWithMetadata>(
                        "1",
                        legalDocument
                ),
                new IdValue<DocumentWithMetadata>(
                        "2",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.APPEAL_SUBMISSION
                        )
                ),
                new IdValue<DocumentWithMetadata>(
                        "3",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.CASE_SUMMARY
                        )
                ), new IdValue<DocumentWithMetadata>(
                        "4",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.APPEAL_RESPONSE
                        )
                )
        );

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
                .thenReturn(Optional.of(legalDocumentList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);

        verify(appender, times(2)).append(
                        legalRepresentativeDocumentsCaptor.capture(), eq(customCollections));

        List<DocumentWithDescription> legalRepresentativeDocuments =
                legalRepresentativeDocumentsCaptor
                        .getAllValues();
        assertEquals(2, legalRepresentativeDocuments.size());

    }

    @Test
    void should_not_create_custom_collections_if_source_collections_are_empty() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_DOCUMENTS))
                .thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS))
                .thenReturn(Optional.empty());

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(any(), any());
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = customiseHearingBundlePreparer.canHandle(callbackStage, callback);

                if (event == Event.CUSTOMISE_HEARING_BUNDLE
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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> customiseHearingBundlePreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundlePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundlePreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
