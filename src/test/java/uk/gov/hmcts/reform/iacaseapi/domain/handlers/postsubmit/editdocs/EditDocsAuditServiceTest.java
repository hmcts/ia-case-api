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
        idValue1 = buildIdValue("1", "1111-2222", "desc1", ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2);
        idValuesBefore = Collections.singletonList(idValue1);

        idValue1WithHearingRecDoc = buildIdValue(
            "1", "1111-2222", "desc1", HEARING_RECORDING_DOCUMENTS).get(0);
        idValueW2ithHearingRecDoc = buildIdValue(
            "2", "2222-3333", "desc2", HEARING_RECORDING_DOCUMENTS).get(0);
        idValuesWithHearingAfter = Arrays.asList(
            idValue1WithHearingRecDoc, idValueW2ithHearingRecDoc);
        idValuesWithHearingBefore = Collections.singletonList(idValue1WithHearingRecDoc);

        return new Object[]{
            new Object[]{idValuesAfter, null, ADDITIONAL_EVIDENCE_DOCUMENTS, Collections.emptyList()},
            new Object[]{idValuesAfter, null, TRIBUNAL_DOCUMENTS, Collections.emptyList()},
            new Object[]{idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.emptyList()},
            new Object[]{idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.emptyList()}
        };
    }

    private Object[] generateFileUpdatedScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        IdValue<HasDocument> idValue2Updated = buildIdValue("2", "3333-4444", "desc3",
            ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        idValuesAfter = Arrays.asList(idValue1, idValue2Updated);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        idValue1WithHearingRecDoc = buildIdValue(
            "1", "1111-2222", "desc1", HEARING_RECORDING_DOCUMENTS).get(0);
        idValueW2ithHearingRecDoc = buildIdValue(
            "2", "2222-3333", "desc2", HEARING_RECORDING_DOCUMENTS).get(0);
        IdValue<HasDocument> idValue2WithHearingRecDocUpdated = buildIdValue(
            "2", "3333-4444", "desc3", HEARING_RECORDING_DOCUMENTS).get(0);
        idValuesWithHearingAfter = Arrays.asList(idValue1WithHearingRecDoc, idValue2WithHearingRecDocUpdated);
        idValuesWithHearingBefore = Arrays.asList(idValue1WithHearingRecDoc, idValueW2ithHearingRecDoc);

        return new Object[]{
            new Object[]{idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("2222-3333")},
            new Object[]{idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("2222-3333")},
            new Object[]{idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.singletonList("2222-3333")}
        };
    }

    private Object[] generateDeleteFileScenarios() {
        idValue1 = buildIdValue("1", "1111-2222", "desc1", ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        idValue2 = buildIdValue("2", "2222-3333", "desc2", ADDITIONAL_EVIDENCE_DOCUMENTS).get(0);
        idValuesAfter = Collections.singletonList(idValue2);
        idValuesBefore = Arrays.asList(idValue1, idValue2);

        idValue1WithHearingRecDoc = buildIdValue(
            "1", "1111-2222", "desc1", HEARING_RECORDING_DOCUMENTS).get(0);
        idValueW2ithHearingRecDoc = buildIdValue(
            "2", "2222-3333", "desc2", HEARING_RECORDING_DOCUMENTS).get(0);
        idValuesWithHearingAfter = Collections.singletonList(idValueW2ithHearingRecDoc);
        idValuesWithHearingBefore = Arrays.asList(idValue1WithHearingRecDoc, idValueW2ithHearingRecDoc);

        return new Object[]{
            new Object[]{idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("1111-2222")},
            new Object[]{idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("1111-2222")},
            new Object[]{idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.singletonList("1111-2222")}
        };
    }

    private List<IdValue<HasDocument>> buildIdValue(String id, String docId, String description,
                                                    AsylumCaseFieldDefinition fieldDefinition) {
        Document doc = new Document("http://dm-store:89/" + docId, "", "");
        IdValue<HasDocument> idValue = new IdValue<>(id, buildValue(doc, description, fieldDefinition));
        return Collections.singletonList(idValue);
    }

    private HasDocument buildValue(Document doc, String desc, AsylumCaseFieldDefinition fieldDefinition) {
        if (!fieldDefinition.equals(HEARING_RECORDING_DOCUMENTS)) {
            return new DocumentWithMetadata(doc, desc, "1-1-2020", ADDITIONAL_EVIDENCE, null);
        }
        return new HearingRecordingDocument(doc, desc);
    }

}