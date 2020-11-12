package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.ADDITIONAL_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.NONE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
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

@RunWith(JUnitParamsRunner.class)
public class EditDocsAboutToSubmitHandlerTest {

    public static final String ID_VALUE = "0a165fa5-086b-49d6-8b7e-f00ed34d941a";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private EditDocsCaseNoteService editDocsCaseNoteService;
    @Mock
    private EditDocsService editDocService;
    @InjectMocks
    private EditDocsAboutToSubmitHandler editDocsAboutToSubmitHandler;

    @Test
    @Parameters({
        "EDIT_DOCUMENTS, ABOUT_TO_SUBMIT, true",
        "START_APPEAL, ABOUT_TO_SUBMIT, false",
        "EDIT_DOCUMENTS, ABOUT_TO_START, false"
    })
    public void canHandleHappyPathScenarios(Event event, PreSubmitCallbackStage callbackStage, boolean expectedResult) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = editDocsAboutToSubmitHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    @Parameters({
        "null, null, callbackStage must not be null",
        "ABOUT_TO_SUBMIT, null, callback must not be null"
    })
    public void canHandleCornerCaseScenarios(@Nullable PreSubmitCallbackStage callbackStage,
                                             @Nullable Callback<AsylumCase> callback,
                                             String expectedExceptionMessage) {
        try {
            editDocsAboutToSubmitHandler.canHandle(callbackStage, callback);
        } catch (Exception e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }

    @Test
    @Parameters(method = "generateNewFileAddedScenario, generateFileUpdatedScenario, generateDeletedFileScenario")
    public void handle(AsylumCase asylumCase,
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
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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
            assertTrue("file deleted as expected", true);
        } else {
            fail();
        }
        then(editDocsCaseNoteService).should(times(1))
            .writeAuditCaseNoteForGivenCaseId(anyLong(), any(AsylumCase.class), any());
        then(editDocService).should(times(1)).cleanUpOverviewTabDocs(any(), any());
    }

    private boolean isDeletedFileScenario(String expectedSuppliedBy) {
        return expectedSuppliedBy == null;
    }

    /**
     * This method simulates adding new documents.
     *
     * @return parameters for the handle method to recreate different file addition scenarios
     */
    private Object[] generateNewFileAddedScenario() {
        String expectedSuppliedBy = "some supplier";
        AsylumCase asylumCaseWithAdditionalEvidenceDoc = buildAsylumCaseGivenParams(expectedSuppliedBy,
            DocumentTag.NONE, ADDITIONAL_EVIDENCE_DOCUMENTS);

        AsylumCase asylumCaseWithTribunalDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            TRIBUNAL_DOCUMENTS);

        AsylumCase asylumCaseWithReheardHearingDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, DocumentTag.NONE,
            REHEARD_HEARING_DOCUMENTS);

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

        return new Object[]{
            new Object[]{asylumCaseWithAdditionalEvidenceDoc, asylumCaseBefore, ADDITIONAL_EVIDENCE_DOCUMENTS,
                expectedSuppliedBy, NONE},
            new Object[]{asylumCaseWithTribunalDoc, asylumCaseBefore, TRIBUNAL_DOCUMENTS, expectedSuppliedBy,
                DocumentTag.NONE},
            new Object[]{asylumCaseWithReheardHearingDoc, asylumCaseBefore, REHEARD_HEARING_DOCUMENTS, expectedSuppliedBy,
                DocumentTag.NONE},
            new Object[]{asylumCaseWithHearingDoc, asylumCaseBefore, HEARING_DOCUMENTS, expectedSuppliedBy,
                DocumentTag.NONE},
            new Object[]{asylumCaseWithLegalRepsDoc, asylumCaseBefore, LEGAL_REPRESENTATIVE_DOCUMENTS,
                expectedSuppliedBy, DocumentTag.NONE},
            new Object[]{asylumCaseWithAddEndUmDoc, asylumCaseBefore, ADDENDUM_EVIDENCE_DOCUMENTS,
                expectedSuppliedBy, DocumentTag.NONE},
            new Object[]{asylumCaseWithRespondentDoc, asylumCaseBefore, RESPONDENT_DOCUMENTS,
                expectedSuppliedBy, DocumentTag.NONE},
            new Object[]{asylumCaseWithDraftDecisionAndReasonsDoc, asylumCaseBefore,
                DRAFT_DECISION_AND_REASONS_DOCUMENTS, expectedSuppliedBy, DocumentTag.NONE},
            new Object[]{asylumCaseWithFinalDecisionAndReasonsDoc, asylumCaseBefore,
                FINAL_DECISION_AND_REASONS_DOCUMENTS, expectedSuppliedBy, DocumentTag.NONE}
        };
    }

    private AsylumCase buildAsylumCaseGivenParams(String expectedSuppliedBy, DocumentTag documentTag,
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
    private Object[] generateDeletedFileScenario() {
        AsylumCase asylumCase = new AsylumCase();

        AsylumCase asylumCaseBefore = buildAsylumCaseGivenParams("some suppliedBy",
            ADDITIONAL_EVIDENCE, ADDITIONAL_EVIDENCE_DOCUMENTS);

        return new Object[]{
            new Object[]{asylumCase, asylumCaseBefore, ADDITIONAL_EVIDENCE_DOCUMENTS, null, ADDITIONAL_EVIDENCE}
        };
    }

    /**
     * This method simulates updating the suppliedBy field for existing documents.
     *
     * @return parameters for the handle method to recreate different file updated scenarios
     */
    private Object[] generateFileUpdatedScenario() {
        String expectedSuppliedBy = "updated supplier";
        AsylumCase asylumCaseWithAdditionalEvidenceDoc = buildAsylumCaseGivenParams(expectedSuppliedBy,
            ADDITIONAL_EVIDENCE, ADDITIONAL_EVIDENCE_DOCUMENTS);
        String beforeSupplier = "before supplier";
        AsylumCase asylumCaseWithAdditionalEvidenceDocBefore = buildAsylumCaseGivenParams(beforeSupplier,
            ADDITIONAL_EVIDENCE, ADDITIONAL_EVIDENCE_DOCUMENTS);

        AsylumCase asylumCaseWithTribunalDoc = buildAsylumCaseGivenParams(expectedSuppliedBy, NONE, TRIBUNAL_DOCUMENTS);
        AsylumCase asylumCaseWithTribunalDocBefore = buildAsylumCaseGivenParams(beforeSupplier, NONE, TRIBUNAL_DOCUMENTS);

        return new Object[]{
            new Object[]{asylumCaseWithAdditionalEvidenceDoc, asylumCaseWithAdditionalEvidenceDocBefore,
                ADDITIONAL_EVIDENCE_DOCUMENTS, expectedSuppliedBy, ADDITIONAL_EVIDENCE},
            new Object[]{asylumCaseWithTribunalDoc, asylumCaseWithTribunalDocBefore, TRIBUNAL_DOCUMENTS,
                expectedSuppliedBy, NONE}
        };
    }

    private List<IdValue<DocumentWithMetadata>> buildIdValuesGivenParams(DocumentTag documentTag,
                                                                         String suppliedBy) {
        IdValue<DocumentWithMetadata> idValueWithNoTag = new IdValue<>(ID_VALUE, buildValueWithNoTag(documentTag,
            suppliedBy));
        return Collections.singletonList(idValueWithNoTag);
    }

    private DocumentWithMetadata buildValueWithNoTag(DocumentTag documentTag, String suppliedBy) {
        Document doc = new Document("http://dm-store:4506/documents/80e2af54-7a93-498f-af55-fe190f3224d2",
            "http://dm-store:4506/documents/80e2af54-7a93-498f-af55-fe190f3224d2/binary",
            "Screenshot 2020-03-06 at 10.07.01.jpg");
        return new DocumentWithMetadata(doc, "some desc", "2020-01-01", documentTag, suppliedBy);
    }
}
