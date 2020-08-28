package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.ADDITIONAL_EVIDENCE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class EditDocsAuditServiceTest {

    static IdValue<HasDocument> idValue1;
    static IdValue<HasDocument> idValue2;
    static List<IdValue<HasDocument>> idValuesAfter;
    static List<IdValue<HasDocument>> idValuesBefore;
    static IdValue<HasDocument> idValue1WithHearingRecDoc;
    static IdValue<HasDocument> idValueW2ithHearingRecDoc;
    static List<IdValue<HasDocument>> idValuesWithHearingAfter;
    static List<IdValue<HasDocument>> idValuesWithHearingBefore;

    @ParameterizedTest
    @MethodSource({
        "generateNewFileAddedScenarios",
        "generateFileUpdatedScenarios",
        "generateDeleteFileScenarios"
    })
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

    private static Stream<Arguments> generateNewFileAddedScenarios() {

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

        List<Arguments> scenarios = new ArrayList<>();

        scenarios.add(Arguments.of(idValuesAfter, null, ADDITIONAL_EVIDENCE_DOCUMENTS, Collections.emptyList()));
        scenarios.add(Arguments.of(idValuesAfter, null, TRIBUNAL_DOCUMENTS, Collections.emptyList()));
        scenarios.add(Arguments.of(idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.emptyList()));
        scenarios.add(Arguments.of(idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.emptyList()));

        return scenarios.stream();
    }

    private static Stream<Arguments> generateFileUpdatedScenarios() {
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

        List<Arguments> scenarios = new ArrayList<>();

        scenarios.add(Arguments.of(idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("2222-3333")));
        scenarios.add(Arguments.of(idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("2222-3333")));
        scenarios.add(Arguments.of(idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.singletonList("2222-3333")));

        return scenarios.stream();
    }

    private static Stream<Arguments> generateDeleteFileScenarios() {
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

        List<Arguments> scenarios = new ArrayList<>();


        scenarios.add(Arguments.of(idValuesAfter, idValuesBefore, TRIBUNAL_DOCUMENTS, Collections.singletonList("1111-2222")));
        scenarios.add(Arguments.of(idValuesAfter, idValuesBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                Collections.singletonList("1111-2222")));
        scenarios.add(Arguments.of(idValuesWithHearingAfter, idValuesWithHearingBefore, HEARING_RECORDING_DOCUMENTS,
                Collections.singletonList("1111-2222")));

        return scenarios.stream();
    }

    private static List<IdValue<HasDocument>> buildIdValue(String id, String docId, String description,
                                                    AsylumCaseFieldDefinition fieldDefinition) {
        Document doc = new Document("http://dm-store:89/" + docId, "", "");
        IdValue<HasDocument> idValue = new IdValue<>(id, buildValue(doc, description, fieldDefinition));
        return Collections.singletonList(idValue);
    }

    private static HasDocument buildValue(Document doc, String desc, AsylumCaseFieldDefinition fieldDefinition) {
        if (!fieldDefinition.equals(HEARING_RECORDING_DOCUMENTS)) {
            return new DocumentWithMetadata(doc, desc, "1-1-2020", ADDITIONAL_EVIDENCE, null);
        }
        return new HearingRecordingDocument(doc, desc);
    }

}