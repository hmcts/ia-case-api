package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APP_ADDITIONAL_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_FINAL_DECISION_AND_REASONS_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_FTPA_APPELLANT_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_FTPA_RESPONDENT_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_LEGAL_REP_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_REHEARD_HEARING_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESP_ADDITIONAL_EVIDENCE_DOCS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.SystemDateProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CustomiseHearingBundlePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Appender<DocumentWithDescription> appender;
    @Mock
    private FeatureToggler featureToggler;

    @Captor
    private ArgumentCaptor<DocumentWithDescription> legalRepresentativeDocumentsCaptor;

    private CustomiseHearingBundlePreparer customiseHearingBundlePreparer;

    @BeforeEach
    public void setUp() {
        customiseHearingBundlePreparer =
            new CustomiseHearingBundlePreparer(appender, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_create_custom_collections() {
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);

        List<IdValue<DocumentWithDescription>> customCollections =
            asList(new IdValue("1", createDocumentWithDescription()));
        List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test")));

        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
            new IdValue("1", createDocumentWithMetadata(DocumentTag.CASE_ARGUMENT, "test")),
            new IdValue("2", createDocumentWithMetadata(DocumentTag.APPEAL_SUBMISSION, "tes")),
            new IdValue("3", createDocumentWithMetadata(DocumentTag.CASE_SUMMARY, "test")));

        List<IdValue<DocumentWithMetadata>> additionalEvidenceList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test")));
        List<IdValue<DocumentWithMetadata>> respondentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.RESPONDENT_EVIDENCE, "test")));

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
    public void should_create_custom_collections_in_reheard_case() {
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(callback.getCaseDetails().getCaseData().read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(callback.getCaseDetails().getCaseData().read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class),
            Optional.of(YesOrNo.YES));

        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);

        final List<IdValue<DocumentWithDescription>> customDocumentList =
            asList(new IdValue("1", createDocumentWithDescription()));

        final List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantEvidenceDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentEvidenceDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test")));
        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.FINAL_DECISION_AND_REASONS_PDF, "test")));

        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = asList(
            new IdValue("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customDocumentList);

        when(asylumCase.read(APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(ftpaAppellantEvidenceDocumentList));

        when(asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(ftpaRespondentEvidenceDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_APPELLANT_DOCUMENTS))
            .thenReturn(Optional.of(ftpaAppellantDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(ftpaRespondentDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(finalDecisionAndReasonsDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(addendumEvidenceDocumentList));


        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(CUSTOM_REHEARD_HEARING_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_APPELLANT_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_RESPONDENT_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customDocumentList);
    }

    @Test
    public void should_filter_legal_rep_document_with_correct_tags() {

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
    public void should_not_create_custom_collections_if_source_collections_are_empty() {
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
    public void should_not_create_custom_collections_if_source_collections_are_empty_in_reheard_case() {
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(any(), any());
    }


    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

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

    private DocumentWithDescription createDocumentWithDescription() {
        return
            new DocumentWithDescription(new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20)), "test");
    }

    private Document createDocument() {
        return
            new Document("some-url",
                "some-binary-url",
                "some-filename");
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag, String suppliedBy) {

        return
            new DocumentWithMetadata(createDocument(),
                "some-description",
                new SystemDateProvider().now().toString(), documentTag, suppliedBy);

    }
}
