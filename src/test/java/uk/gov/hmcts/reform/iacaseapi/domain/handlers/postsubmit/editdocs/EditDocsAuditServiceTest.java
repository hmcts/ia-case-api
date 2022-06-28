package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.ADDITIONAL_EVIDENCE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EditDocsAuditServiceTest {

    private static IdValue<HasDocument> idValue1;
    private static IdValue<HasDocument> idValue2;
    private static List<IdValue<HasDocument>> idValuesAfter;
    private static List<IdValue<HasDocument>> idValuesBefore;
    private static IdValue<HasDocument> idValue1WithHearingRecDoc;
    private static IdValue<HasDocument> idValueW2ithHearingRecDoc;
    private static List<IdValue<HasDocument>> idValuesWithHearingAfter;
    private static List<IdValue<HasDocument>> idValuesWithHearingBefore;


    @ParameterizedTest
    @MethodSource({"generateNewFileAddedForNameScenarios", "generateFileUpdatedForNameScenarios", "generateDeleteFileForNameScenarios"})
    void getUpdatedAndDeletedDocNamesForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
                                                          List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                          AsylumCaseFieldDefinition caseFieldDefinition,
                                                          List<String> expectedNames) {
        EditDocsAuditService service = new EditDocsAuditService();

        AsylumCase asylum = new AsylumCase();
        asylum.write(caseFieldDefinition, idValues);

        AsylumCase asylumCaseBefore = new AsylumCase();
        asylumCaseBefore.write(caseFieldDefinition, idValuesBefore);

        List<String> docNames = service.getUpdatedAndDeletedDocNamesForGivenField(
            asylum, asylumCaseBefore, caseFieldDefinition);

        assertEquals(docNames, expectedNames);
    }

    private static Object[] generateNewFileAddedForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocName1").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName2").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2);
        idValuesBefore = Collections.singletonList(idValue1);

        return new Object[] {
            new Object[] {idValuesAfter, null, ADDITIONAL_EVIDENCE_DOCUMENTS, Collections.emptyList()},
            new Object[] {idValuesAfter, null, TRIBUNAL_DOCUMENTS, Collections.emptyList()},
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.emptyList()}
        };
    }

    private static Object[] generateFileUpdatedForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocName1").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName2").get(0);
        IdValue<HasDocument> idValue2Updated = buildIdValue("2", "3333-4444", "desc3",
            false, "someDocNameUpdated").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2Updated);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("someDocName2")},
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("someDocName2")}
        };
    }

    private static Object[] generateDeleteFileForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocNameDeleted").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName2").get(0);
        idValuesAfter = Collections.singletonList(idValue2);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS,
                Collections.singletonList("someDocNameDeleted")},
            new Object[] {idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("someDocNameDeleted")}
        };
    }


    @ParameterizedTest
    @MethodSource({"generateNewFileAddedScenarios", "generateFileUpdatedScenarios", "generateDeleteFileScenarios"})
    void getUpdatedAndDeletedDocIdsForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
                                                        List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                        AsylumCaseFieldDefinition caseFieldDefinition,
                                                        List<String> expectedIds) {
        EditDocsAuditService service = new EditDocsAuditService();

        AsylumCase asylum = new AsylumCase();
        asylum.write(caseFieldDefinition, idValues);

        AsylumCase asylumCaseBefore = new AsylumCase();
        asylumCaseBefore.write(caseFieldDefinition, idValuesBefore);

        List<String> ids = service.getUpdatedAndDeletedDocIdsForGivenField(
            asylum, asylumCaseBefore, caseFieldDefinition);

        assertEquals(ids, expectedIds);
    }

    private static Object[] generateNewFileAddedScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocName").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2);
        idValuesBefore = Collections.singletonList(idValue1);

        idValue1WithHearingRecDoc = buildIdValue(
            "1", "1111-2222", "desc1", true, "someDocName").get(0);
        idValueW2ithHearingRecDoc = buildIdValue(
            "2", "2222-3333", "desc2", true, "someDocName").get(0);
        idValuesWithHearingAfter = Arrays.asList(
            idValue1WithHearingRecDoc, idValueW2ithHearingRecDoc);
        idValuesWithHearingBefore = Collections.singletonList(idValue1WithHearingRecDoc);

        return new Object[] {
            new Object[] {idValuesAfter, null, ADDITIONAL_EVIDENCE_DOCUMENTS, Collections.emptyList()},
            new Object[] {idValuesAfter, null, TRIBUNAL_DOCUMENTS, Collections.emptyList()},
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.emptyList()},
            new Object[] {idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.emptyList()}
        };
    }

    private static Object[] generateFileUpdatedScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocName").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName").get(0);
        IdValue<HasDocument> idValue2Updated = buildIdValue("2", "3333-4444", "desc3",
            false, "someDocName").get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2Updated);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        idValue1WithHearingRecDoc = buildIdValue(
            "1", "1111-2222", "desc1", true, "someDocName").get(0);
        idValueW2ithHearingRecDoc = buildIdValue(
            "2", "2222-3333", "desc2", true, "someDocName").get(0);
        IdValue<HasDocument> idValue2WithHearingRecDocUpdated = buildIdValue(
            "2", "3333-4444", "desc3", true, "someDocName").get(0);
        idValuesWithHearingAfter = Arrays.asList(idValue1WithHearingRecDoc, idValue2WithHearingRecDocUpdated);
        idValuesWithHearingBefore = Arrays.asList(idValue1WithHearingRecDoc, idValueW2ithHearingRecDoc);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("2222-3333")},
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("2222-3333")},
            new Object[] {idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.singletonList("2222-3333")}
        };
    }

    private static Object[] generateDeleteFileScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocName").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName").get(0);
        idValuesAfter = Collections.singletonList(idValue2);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        idValue1WithHearingRecDoc = buildIdValue(
            "1", "1111-2222", "desc1", true, "someDocName").get(0);
        idValueW2ithHearingRecDoc = buildIdValue(
            "2", "2222-3333", "desc2", true, "someDocName").get(0);
        idValuesWithHearingAfter = Collections.singletonList(idValueW2ithHearingRecDoc);
        idValuesWithHearingBefore = Arrays.asList(idValue1WithHearingRecDoc, idValueW2ithHearingRecDoc);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("1111-2222")},
            new Object[] {idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("1111-2222")},
            new Object[] {idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.singletonList("1111-2222")}
        };
    }

    private static List<IdValue<HasDocument>> buildIdValue(String id, String docId, String description,
                                                    boolean hearingRecordingDocFlag, String filename) {
        Document doc = new Document(
            "http://dm-store:89/" + docId,
            "",
            filename,
            "1234567890"
        );
        IdValue<HasDocument> idValue = new IdValue<>(id, buildValue(doc, description, hearingRecordingDocFlag));
        return Collections.singletonList(idValue);
    }

    private static HasDocument buildValue(Document doc, String desc, boolean hearingRecordingDocFlag) {
        if (!hearingRecordingDocFlag) {
            return new DocumentWithMetadata(doc, desc, "1-1-2020", ADDITIONAL_EVIDENCE, null);
        }
        return new HearingRecordingDocument(doc, desc);
    }

}
