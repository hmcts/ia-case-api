package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.ADDITIONAL_EVIDENCE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(JUnitParamsRunner.class)
public class EditDocsAuditServiceTest {

    private IdValue<HasDocument> idValue1;
    private IdValue<HasDocument> idValue2;
    private List<IdValue<HasDocument>> idValuesAfter;
    private List<IdValue<HasDocument>> idValuesBefore;
    private IdValue<HasDocument> idValue1WithHearingRecDoc;
    private IdValue<HasDocument> idValueW2ithHearingRecDoc;
    private List<IdValue<HasDocument>> idValuesWithHearingAfter;
    private List<IdValue<HasDocument>> idValuesWithHearingBefore;

    @Test
    @Parameters(method = "generateNewFileAddedForNameScenarios, generateFileUpdatedForNameScenarios, generateDeleteFileForNameScenarios")
    public void getUpdatedAndDeletedDocNamesForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
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

    private Object[] generateNewFileAddedForNameScenarios() {
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

    private Object[] generateFileUpdatedForNameScenarios() {
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

    private Object[] generateDeleteFileForNameScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", false, "someDocNameDeleted").get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", false, "someDocName2").get(0);
        idValuesAfter = Collections.singletonList(idValue2);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        return new Object[] {
            new Object[] {idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("someDocNameDeleted")},
            new Object[] {idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS, Collections.singletonList("someDocNameDeleted")}
        };
    }



    @Test
    @Parameters(method = "generateNewFileAddedScenarios, generateFileUpdatedScenarios, generateDeleteFileScenarios")
    public void getUpdatedAndDeletedDocIdsForGivenField(List<IdValue<DocumentWithMetadata>> idValues,
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

    private Object[] generateNewFileAddedScenarios() {
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

    private Object[] generateFileUpdatedScenarios() {
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

    private Object[] generateDeleteFileScenarios() {
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

    private List<IdValue<HasDocument>> buildIdValue(String id, String docId, String description,
                                                    boolean hearingRecordingDocFlag, String filename) {
        Document doc = new Document(
            "http://dm-store:89/" + docId,
            "",
            filename
        );
        IdValue<HasDocument> idValue = new IdValue<>(id, buildValue(doc, description, hearingRecordingDocFlag));
        return Collections.singletonList(idValue);
    }

    private HasDocument buildValue(Document doc, String desc, boolean hearingRecordingDocFlag) {
        if (!hearingRecordingDocFlag) {
            return new DocumentWithMetadata(doc, desc, "1-1-2020", ADDITIONAL_EVIDENCE, null);
        }
        return new HearingRecordingDocument(doc, desc);
    }

}