package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.ADDITIONAL_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.NONE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import junitparams.converters.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class EditDocsAboutToSubmitHandlerTest {

    static final String ID_VALUE = "0a165fa5-086b-49d6-8b7e-f00ed34d941a";
    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    EditDocsCaseNoteService editDocsCaseNoteService;
    @Mock private
    EditDocsService editDocService;
    @InjectMocks
    EditDocsAboutToSubmitHandler editDocsAboutToSubmitHandler;

    @ParameterizedTest
    @MethodSource("canHandleHappyPathTestData")
    void canHandleHappyPathScenarios(Event event, PreSubmitCallbackStage callbackStage, boolean expectedResult) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = editDocsAboutToSubmitHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }
    
    private static Stream<Arguments> canHandleHappyPathTestData() {
        
        return Stream.of(
            Arguments.of(EDIT_DOCUMENTS, ABOUT_TO_SUBMIT, true),
            Arguments.of(START_APPEAL, ABOUT_TO_SUBMIT, false),
            Arguments.of(EDIT_DOCUMENTS, ABOUT_TO_START, false)
        );
    }

    @ParameterizedTest
    @MethodSource("canHandleTestData")
    void canHandleCornerCaseScenarios(@Nullable PreSubmitCallbackStage callbackStage,
                                             @Nullable Callback<AsylumCase> callback,
                                             String expectedExceptionMessage) {
        try {
            editDocsAboutToSubmitHandler.canHandle(callbackStage, callback);
        } catch (Exception e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }

    static Stream<Arguments> canHandleTestData() {

        return Stream.of(
            Arguments.of(null, null, "callbackStage must not be null"),
            Arguments.of(ABOUT_TO_SUBMIT, null, "callback must not be null")
        );
    }

    @ParameterizedTest
    @MethodSource({
        "generateNewFileAddedScenario",
        "generateFileUpdatedScenario",
        "generateDeletedFileScenario" })
    void handle(AsylumCase asylumCase,
                       AsylumCase asylumCaseBefore,
                       AsylumCaseFieldDefinition caseFieldDefinition,
                       String expectedSuppliedBy,
                       DocumentTag expectedDocumentTag) {
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);

        if (!isDeletedFileScenario(expectedSuppliedBy)) { // no need to mock for this scenario
            given(callback.getCaseDetailsBefore()).willReturn(Optional.of(caseDetailsBefore));
            given(caseDetailsBefore.getCaseData()).willReturn(asylumCaseBefore);
        }

        PreSubmitCallbackResponse<AsylumCase> currentResult = editDocsAboutToSubmitHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        Optional<List<IdValue<DocumentWithMetadata>>> currentIdValueOptional = currentResult.getData()
            .read(caseFieldDefinition);
        if (currentIdValueOptional.isPresent()) {
            List<IdValue<DocumentWithMetadata>> currentIdValue = currentIdValueOptional.get();
            DocumentWithMetadata currentValue = currentIdValue.stream()
                .filter(idValue -> idValue.getId().equals(ID_VALUE))
                .findFirst()
                .orElseThrow(RuntimeException::new).getValue();
            assertEquals(expectedDocumentTag, currentValue.getTag());
            assertEquals(expectedSuppliedBy, currentValue.getSuppliedBy());
        } else if (isDeletedFileScenario(expectedSuppliedBy)) {
            assertTrue(true, "file deleted as expected");
        } else {
            fail();
        }
        then(editDocsCaseNoteService).should(times(1))
            .writeAuditCaseNoteForGivenCaseId(anyLong(), any(AsylumCase.class), any());
        then(editDocService).should(times(1)).cleanUpOverviewTabDocs(any(), any());
    }

    boolean isDeletedFileScenario(String expectedSuppliedBy) {
        return expectedSuppliedBy == null;
    }

    /**
     * This method simulates adding new documents.
     *
     * @return parameters for the handle method to recreate different file addition scenarios
     */
    static Stream<Arguments> generateNewFileAddedScenario() {
        String expectedSuppliedBy = "some supplier";
        AsylumCase asylumCaseWithAdditionalEvidenceDoc = buildAsylumCaseGivenParams(expectedSuppliedBy,
            DocumentTag.NONE, ADDITIONAL_EVIDENCE_DOCUMENTS);

        AsylumCase asylumCaseWithTribunalDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            TRIBUNAL_DOCUMENTS);

        AsylumCase asylumCaseWithHearingDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            HEARING_DOCUMENTS);

        AsylumCase asylumCaseWithLegalRepsDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            LEGAL_REPRESENTATIVE_DOCUMENTS);

        AsylumCase asylumCaseWithAddEndUmDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            ADDENDUM_EVIDENCE_DOCUMENTS);

        AsylumCase asylumCaseWithRespondentDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            RESPONDENT_DOCUMENTS);

        AsylumCase asylumCaseWithDraftDecisionAndReasonsDoc = buildAsylumCaseGivenParams(expectedSuppliedBy,
            DocumentTag.NONE, DRAFT_DECISION_AND_REASONS_DOCUMENTS);

        AsylumCase asylumCaseWithFinalDecisionAndReasonsDoc = buildAsylumCaseGivenParams(expectedSuppliedBy,
            DocumentTag.NONE, FINAL_DECISION_AND_REASONS_DOCUMENTS);

        AsylumCase asylumCaseBefore = new AsylumCase();

        List<Arguments> scenarios = new ArrayList<>();


        scenarios.add(Arguments.of(asylumCaseWithAdditionalEvidenceDoc, asylumCaseBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                expectedSuppliedBy, NONE));
        scenarios.add(Arguments.of(asylumCaseWithTribunalDoc, asylumCaseBefore, TRIBUNAL_DOCUMENTS, expectedSuppliedBy,
                DocumentTag.NONE));
        scenarios.add(Arguments.of(asylumCaseWithHearingDoc, asylumCaseBefore, HEARING_DOCUMENTS, expectedSuppliedBy,
                DocumentTag.NONE));
        scenarios.add(Arguments.of(asylumCaseWithLegalRepsDoc, asylumCaseBefore, LEGAL_REPRESENTATIVE_DOCUMENTS,
                expectedSuppliedBy, DocumentTag.NONE));
        scenarios.add(Arguments.of(asylumCaseWithAddEndUmDoc, asylumCaseBefore, ADDENDUM_EVIDENCE_DOCUMENTS,
                expectedSuppliedBy, DocumentTag.NONE));
        scenarios.add(Arguments.of(asylumCaseWithRespondentDoc, asylumCaseBefore, RESPONDENT_DOCUMENTS,
                expectedSuppliedBy, DocumentTag.NONE));
        scenarios.add(Arguments.of(asylumCaseWithDraftDecisionAndReasonsDoc, asylumCaseBefore,
                DRAFT_DECISION_AND_REASONS_DOCUMENTS, expectedSuppliedBy, DocumentTag.NONE));
        scenarios.add(Arguments.of(asylumCaseWithFinalDecisionAndReasonsDoc, asylumCaseBefore,
                FINAL_DECISION_AND_REASONS_DOCUMENTS, expectedSuppliedBy, DocumentTag.NONE));

        return scenarios.stream();
    }

    static AsylumCase buildAsylumCaseGivenParams(String expectedSuppliedBy, DocumentTag documentTag,
                                                  AsylumCaseFieldDefinition fieldDefinition) {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(fieldDefinition, buildIdValuesGivenParams(documentTag, expectedSuppliedBy));
        return asylumCase;
    }

    /**
     * This method simulates deleting existing documents.
     *
     * @return parameters for the handle method to recreate different delete file scenarios
     */
    static Stream<Arguments> generateDeletedFileScenario() {
        AsylumCase asylumCase = new AsylumCase();

        AsylumCase asylumCaseBefore = buildAsylumCaseGivenParams("some suppliedBy",
            ADDITIONAL_EVIDENCE, ADDITIONAL_EVIDENCE_DOCUMENTS);

        return Stream.of(
            Arguments.of(asylumCase, asylumCaseBefore, ADDITIONAL_EVIDENCE_DOCUMENTS, null, ADDITIONAL_EVIDENCE)
        );
    }

    /**
     * This method simulates updating the suppliedBy field for existing documents.
     *
     * @return parameters for the handle method to recreate different file updated scenarios
     */
    static Stream<Arguments> generateFileUpdatedScenario() {
        String expectedSuppliedBy = "updated supplier";
        AsylumCase asylumCaseWithAdditionalEvidenceDoc = buildAsylumCaseGivenParams(expectedSuppliedBy,
            ADDITIONAL_EVIDENCE, ADDITIONAL_EVIDENCE_DOCUMENTS);
        String beforeSupplier = "before supplier";
        AsylumCase asylumCaseWithAdditionalEvidenceDocBefore = buildAsylumCaseGivenParams(beforeSupplier,
            ADDITIONAL_EVIDENCE, ADDITIONAL_EVIDENCE_DOCUMENTS);

        AsylumCase asylumCaseWithTribunalDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, NONE, TRIBUNAL_DOCUMENTS);
        AsylumCase asylumCaseWithTribunalDocBefore = buildAsylumCaseGivenParams(beforeSupplier, NONE, TRIBUNAL_DOCUMENTS);

        List<Arguments> scenarios = new ArrayList<>();

        scenarios.add(Arguments.of(asylumCaseWithAdditionalEvidenceDoc, asylumCaseWithAdditionalEvidenceDocBefore,
                ADDITIONAL_EVIDENCE_DOCUMENTS, expectedSuppliedBy, ADDITIONAL_EVIDENCE));
        scenarios.add(Arguments.of(asylumCaseWithTribunalDoc, asylumCaseWithTribunalDocBefore, TRIBUNAL_DOCUMENTS,
                expectedSuppliedBy, NONE));

        return scenarios.stream();
    }

    static List<IdValue<DocumentWithMetadata>> buildIdValuesGivenParams(DocumentTag documentTag,
                                                                         String suppliedBy) {
        IdValue<DocumentWithMetadata> idValueWithNoTag = new IdValue<>(ID_VALUE, buildValueWithNoTag(documentTag,
            suppliedBy));
        return Collections.singletonList(idValueWithNoTag);
    }

    static DocumentWithMetadata buildValueWithNoTag(DocumentTag documentTag, String suppliedBy) {
        Document doc = new Document("http://dm-store:4506/documents/80e2af54-7a93-498f-af55-fe190f3224d2",
            "http://dm-store:4506/documents/80e2af54-7a93-498f-af55-fe190f3224d2/binary",
            "Screenshot 2020-03-06 at 10.07.01.jpg");
        return new DocumentWithMetadata(doc, "some desc", "2020-01-01", documentTag, suppliedBy);
    }
}