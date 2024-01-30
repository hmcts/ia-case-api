package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.lang.Nullable;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class EditDocsServiceTest {

    public static final String DOC_ID = "5c39421f-e518-49da-987b-c4c48dffab43";
    public static final String ANOTHER_DOC_ID = "ad22bb0f-5b5a-4a39-bd6d-b966e8602072";
    private static final String DOCUMENT_URL = "https://dm-store/5c39421f-e518-49da-987b-c4c48dffab43";
    private static final String ANOTHER_DOCUMENT_URL = "https://dm-store/ad22bb0f-5b5a-4a39-bd6d-b966e8602072";
    private static final String DOCUMENT_BINARY_URL = "some-document-binary-url";
    private static final String DOCUMENT_FILENAME = "some-document-filename";
    private static final String DOCUMENT_DESCRIPTION = "some-document-description";
    private static final List<String> DOC_ID_LIST = new ArrayList<>(Collections.singletonList(DOC_ID));
    private static final List<IdValue<DocumentWithDescription>> DOC_ID_VALUE_LIST = new ArrayList<>(Collections.singletonList(
            new IdValue<>(DOC_ID, new DocumentWithDescription(new Document(DOCUMENT_URL, DOCUMENT_BINARY_URL, DOCUMENT_FILENAME), DOCUMENT_DESCRIPTION))));

    private static final List<IdValue<DocumentWithDescription>> ANOTHER_DOC_ID_VALUE_LIST = new ArrayList<>(Collections.singletonList(
            new IdValue<>(ANOTHER_DOC_ID, new DocumentWithDescription(new Document(ANOTHER_DOCUMENT_URL, DOCUMENT_BINARY_URL, DOCUMENT_FILENAME), DOCUMENT_DESCRIPTION))));
    private static final List<IdValue<DocumentWithMetadata>> ANOTHER_DOC_ID_VALUE_LIST_2 = new ArrayList<>(Collections.singletonList(
            new IdValue<>(ANOTHER_DOC_ID, new DocumentWithMetadata(new Document(ANOTHER_DOCUMENT_URL, DOCUMENT_BINARY_URL, DOCUMENT_FILENAME), DOCUMENT_DESCRIPTION, "10-02-2023", DocumentTag.FTPA_APPELLANT))));
    private AsylumCase asylumCase;
    @Mock
    private EditDocsAuditService editDocsAuditService;
    @InjectMocks
    private EditDocsService editDocsService;

    @BeforeEach
    public void setUp() {
        asylumCase = new AsylumCase();
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), any()))
                .willReturn(new ArrayList<>());
    }

    @ParameterizedTest
    @MethodSource({
        "documentIsDeletedScenario",
        "anotherDocumentIsDeletedScenario",
        "theOverviewTabFieldIsNullScenario",
        "documentIsUpdatedScenario"
    })
    void cleanUpOverviewTabDocs(AsylumCase asylum,
                                       AsylumCase asylumBefore,
                                       List<String> updatedDeletedDocIdsList,
                                       @Nullable Document expectedDocument) {

        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(),
            eq(FINAL_DECISION_AND_REASONS_DOCUMENTS))).willReturn(updatedDeletedDocIdsList);

        editDocsService.cleanUpOverviewTabDocs(asylum, asylumBefore);

        Document actualFinalDecisionAndReasonPdf = asylum.read(FINAL_DECISION_AND_REASONS_PDF, Document.class)
            .orElse(null);
        assertThat(actualFinalDecisionAndReasonPdf).isEqualTo(expectedDocument);
    }

    private static Object[] documentIsDeletedScenario() {
        AsylumCase asylumCase = buildCaseWithPopulatedFieldForGivenDoc(buildDocumentGivenDocId());

        return new Object[] {
            new Object[] {
                asylumCase,
                new AsylumCase(),
                new ArrayList<>(Collections.singletonList(DOC_ID)),
                null}
        };
    }

    private static Object[] anotherDocumentIsDeletedScenario() {
        AsylumCase asylumCase = buildCaseWithPopulatedFieldForGivenDoc(buildDocumentGivenDocId());
        asylumCase.write(FINAL_DECISION_AND_REASONS_DOCUMENTS, Collections.singletonList(buildIdValue()));
        Document expectedFinalDecisionAndReasonPdf = buildDocumentGivenDocId();

        return new Object[] {
            new Object[] {
                asylumCase,
                new AsylumCase(),
                new ArrayList<>(Collections.singletonList(ANOTHER_DOC_ID)),
                expectedFinalDecisionAndReasonPdf}
        };
    }

    private static Object[] theOverviewTabFieldIsNullScenario() {
        AsylumCase asylumCase = buildCaseWithPopulatedFieldForGivenDoc(null);

        return new Object[] {
            new Object[] {
                asylumCase,
                new AsylumCase(),
                new ArrayList<>(Collections.singletonList(DOC_ID)),
                null}
        };
    }

    private static Object[] documentIsUpdatedScenario() {
        AsylumCase asylumCase = buildCaseWithPopulatedFieldForGivenDoc(buildDocumentGivenDocId());
        asylumCase.write(FINAL_DECISION_AND_REASONS_DOCUMENTS, Collections.singletonList(buildIdValue()));
        Document expectedFinalDecisionAndReasonPdf = buildDocumentGivenDocId();

        return new Object[] {
            new Object[] {
                asylumCase,
                new AsylumCase(),
                new ArrayList<>(Collections.singletonList(DOC_ID)),
                expectedFinalDecisionAndReasonPdf}
        };
    }

    private static AsylumCase buildCaseWithPopulatedFieldForGivenDoc(Document document) {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_PDF, document);
        return asylumCase;
    }

    private static Document buildDocumentGivenDocId() {
        return new Document("http://dm-store/" + DOC_ID, "", "");
    }

    private static DocumentWithMetadata buildDocWithMeta() {
        return new DocumentWithMetadata(buildDocumentGivenDocId(), "", "", DocumentTag.NONE);
    }

    private static IdValue<DocumentWithMetadata> buildIdValue() {
        return new IdValue<>("1", buildDocWithMeta());
    }


    @Test
    void cleanUpOverviewTabDocsFtpaDecisionAppellantDoesNotNeedCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(ALL_FTPA_APPELLANT_DECISION_DOCS)))
            .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_APPELLANT_DECISION_DOCUMENT, Optional.of(ANOTHER_DOC_ID_VALUE_LIST));
        asylumCase.write(ALL_FTPA_APPELLANT_DECISION_DOCS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST_2));

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaDecisionAppellantNeedsCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(ALL_FTPA_APPELLANT_DECISION_DOCS)))
            .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_APPELLANT_DECISION_DOCUMENT, Optional.of(DOC_ID_VALUE_LIST));

        assertDocumentListEquality(DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(new ArrayList<>(), asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaDecisionRespondentDoesNotNeedCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(ALL_FTPA_RESPONDENT_DECISION_DOCS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_RESPONDENT_DECISION_DOCUMENT, Optional.of(ANOTHER_DOC_ID_VALUE_LIST));
        asylumCase.write(ALL_FTPA_RESPONDENT_DECISION_DOCS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST_2));

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaDecisionRespondentNeedsCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(ALL_FTPA_RESPONDENT_DECISION_DOCS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_RESPONDENT_DECISION_DOCUMENT, Optional.of(DOC_ID_VALUE_LIST));

        assertDocumentListEquality(DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(new ArrayList<>(), asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaGroundsAppellantDoesNotNeedCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_APPELLANT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_APPELLANT_GROUNDS_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST));
        asylumCase.write(FTPA_APPELLANT_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST_2));

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaGroundsAppellantNeedsCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_APPELLANT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_APPELLANT_GROUNDS_DOCUMENTS, Optional.of(DOC_ID_VALUE_LIST));

        assertDocumentListEquality(DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(new ArrayList<>(), asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaGroundsRespondentDoesNotNeedCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_RESPONDENT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_RESPONDENT_GROUNDS_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST));
        asylumCase.write(FTPA_RESPONDENT_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST_2));

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaGroundsRespondentNeedsCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_RESPONDENT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_RESPONDENT_GROUNDS_DOCUMENTS, Optional.of(DOC_ID_VALUE_LIST));

        assertDocumentListEquality(DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(new ArrayList<>(), asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaSupportingEvidenceAppellantDoesNotNeedCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_APPELLANT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_APPELLANT_EVIDENCE_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST));
        asylumCase.write(FTPA_APPELLANT_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST_2));

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaSupportingEvidenceAppellantNeedsCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_APPELLANT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_APPELLANT_EVIDENCE_DOCUMENTS, Optional.of(DOC_ID_VALUE_LIST));

        assertDocumentListEquality(DOC_ID_VALUE_LIST, asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(new ArrayList<>(), asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaSupportingEvidenceRespondentDoesNotNeedCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_RESPONDENT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST));
        asylumCase.write(FTPA_RESPONDENT_DOCUMENTS, Optional.of(ANOTHER_DOC_ID_VALUE_LIST_2));

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(ANOTHER_DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS));
    }

    @Test
    void cleanUpOverviewTabDocsFtpaSupportingEvidenceRespondentNeedsCleaning() {
        given(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(), any(), eq(FTPA_RESPONDENT_DOCUMENTS)))
                .willReturn(DOC_ID_LIST);

        asylumCase.write(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS, Optional.of(DOC_ID_VALUE_LIST));

        assertDocumentListEquality(DOC_ID_VALUE_LIST, asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS));

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCase);

        assertDocumentListEquality(new ArrayList<>(), asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS));
    }

    private void assertDocumentListEquality(List<IdValue<DocumentWithDescription>> expected, Optional<List<IdValue<DocumentWithDescription>>> actual) {
        assertThat(actual).isEqualTo(Optional.of(expected));
    }
}
