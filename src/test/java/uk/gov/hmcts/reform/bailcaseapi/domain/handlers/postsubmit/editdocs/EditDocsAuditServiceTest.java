package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.EditDocsAuditService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EditDocsAuditServiceTest {

    private static IdValue<HasDocument> idValue1;
    private static IdValue<HasDocument> idValue2;
    private static List<IdValue<HasDocument>> idValuesAfter;
    private static List<IdValue<HasDocument>> idValuesBefore;

    @ParameterizedTest
    @MethodSource({"generateFileUpdatedForNameScenarios", "generateDeleteFileForNameScenarios"})
    void getUpdatedAndDeletedDocNamesForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
                                                   List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                   BailCaseFieldDefinition caseFieldDefinition,
                                                   List<String> expectedNames) {
        EditDocsAuditService service = new EditDocsAuditService();

        BailCase bailCase = new BailCase();
        bailCase.write(caseFieldDefinition, idValues);

        BailCase bailCaseBefore = new BailCase();
        bailCaseBefore.write(caseFieldDefinition, idValuesBefore);

        List<String> docNames = service.getUpdatedAndDeletedDocNamesForGivenField(
            bailCase, bailCaseBefore, caseFieldDefinition);

        assertEquals(docNames, expectedNames);
    }

    @ParameterizedTest
    @MethodSource({"generateNewFileAddedForNameScenarios"})
    void getAddedDocNamesForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
                                                   List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                   BailCaseFieldDefinition caseFieldDefinition,
                                                   List<String> expectedNames) {
        EditDocsAuditService service = new EditDocsAuditService();

        BailCase bailCase = new BailCase();
        bailCase.write(caseFieldDefinition, idValues);

        BailCase bailCaseBefore = new BailCase();
        bailCaseBefore.write(caseFieldDefinition, idValuesBefore);

        List<String> docNames = service.getAddedDocNamesForGivenField(
            bailCase, bailCaseBefore, caseFieldDefinition);

        assertEquals(docNames, expectedNames);
    }

    private static Object[] generateNewFileAddedForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", "someDocName1").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", "someDocName2").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2);
        idValuesBefore = Collections.singletonList(idValue1);

        return new Object[] {
            new Object[] {idValuesAfter, null, TRIBUNAL_DOCUMENTS_WITH_METADATA,
                List.of("someDocName1","someDocName2")},
            new Object[] {idValuesAfter, idValuesBefore, HOME_OFFICE_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("someDocName2")}
        };
    }

    private static Object[] generateFileUpdatedForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", "someDocName1").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", "someDocName2").get(0);
        IdValue<HasDocument> idValue2Updated = buildIdValue("2", "3333-4444", "desc3",
                                                            "someDocNameUpdated").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2Updated);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("someDocName2")},
            new Object[] {idValuesAfter, idValuesBefore, APPLICANT_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("someDocName2")}
        };
    }

    private static Object[] generateDeleteFileForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", "someDocNameDeleted").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", "someDocName2").get(0);
        idValuesAfter = Collections.singletonList(idValue2);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("someDocNameDeleted")},
            new Object[] {idValuesAfter, idValuesBefore, APPLICANT_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("someDocNameDeleted")}
        };
    }

    @ParameterizedTest
    @MethodSource({"generateFileUpdatedScenarios", "generateDeleteFileScenarios"})
    void getUpdatedAndDeletedDocIdsForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
                                                 List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                 BailCaseFieldDefinition caseFieldDefinition,
                                                 List<String> expectedIds) {
        EditDocsAuditService service = new EditDocsAuditService();

        BailCase bailCase = new BailCase();
        bailCase.write(caseFieldDefinition, idValues);

        BailCase bailCaseCaseBefore = new BailCase();
        bailCaseCaseBefore.write(caseFieldDefinition, idValuesBefore);

        List<String> ids = service.getUpdatedAndDeletedDocIdsForGivenField(
            bailCase, bailCaseCaseBefore, caseFieldDefinition);

        assertEquals(ids, expectedIds);
    }

    @ParameterizedTest
    @MethodSource({"generateNewFileAddedScenarios"})
    void getAddedDocIdsForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
                                                 List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                 BailCaseFieldDefinition caseFieldDefinition,
                                                 List<String> expectedIds) {
        EditDocsAuditService service = new EditDocsAuditService();

        BailCase bailCase = new BailCase();
        bailCase.write(caseFieldDefinition, idValues);

        BailCase bailCaseBefore = new BailCase();
        bailCaseBefore.write(caseFieldDefinition, idValuesBefore);

        List<String> ids = service.getAddedDocIdsForGivenField(
            bailCase, bailCaseBefore, caseFieldDefinition);

        assertEquals(ids, expectedIds);
    }

    private static Object[] generateNewFileAddedScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", "someDocName").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", "someDocName").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2);
        idValuesBefore = Collections.singletonList(idValue1);

        return new Object[] {
            new Object[] {idValuesAfter, null, TRIBUNAL_DOCUMENTS_WITH_METADATA, List.of("1111-2222","2222-3333")},
            new Object[] {idValuesAfter, idValuesBefore, APPLICANT_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("2222-3333")},
        };
    }

    private static Object[] generateFileUpdatedScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", "someDocName").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", "someDocName").get(0);
        IdValue<HasDocument> idValue2Updated = buildIdValue("2", "3333-4444", "desc3",
                                                            "someDocName").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2Updated);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("2222-3333")},
            new Object[] {idValuesAfter, idValuesBefore, APPLICANT_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("2222-3333")},
        };
    }

    private static Object[] generateDeleteFileScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", "someDocName").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", "someDocName").get(0);
        idValuesAfter = Collections.singletonList(idValue2);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("1111-2222")},
            new Object[] {idValuesAfter, idValuesBefore, HOME_OFFICE_DOCUMENTS_WITH_METADATA,
                Collections.singletonList("1111-2222")},
        };
    }

    private static List<IdValue<HasDocument>> buildIdValue(String id, String docId, String description,
                                                           String filename) {
        Document doc = new Document(
            "http://dm-store:89/" + docId,
            "",
            filename,
            null
        );
        IdValue<HasDocument> idValue = new IdValue<>(id, buildValue(doc, description));
        return Collections.singletonList(idValue);
    }

    private static HasDocument buildValue(Document doc, String desc) {
        return new DocumentWithMetadata(doc, desc, "1-1-2022", DocumentTag.BAIL_SUMMARY, null);
    }

}
