package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.BAIL_SUMMARY;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.EditDocsCaseNoteService;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class EditBailDocsAboutToSubmitHandlerTest {

    public static final String ID_VALUE = "0a165fa5-086b-49d6-8b7e-f00ed34d941a";
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private EditDocsCaseNoteService editDocsCaseNoteService;
    @InjectMocks
    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs.EditBailDocsAboutToSubmitHandler editBailDocsAboutToSubmitHandler;

    @ParameterizedTest
    @CsvSource({
        "EDIT_BAIL_DOCUMENTS, ABOUT_TO_SUBMIT, true",
        "START_APPLICATION, ABOUT_TO_SUBMIT, false",
        "EDIT_BAIL_DOCUMENTS, ABOUT_TO_START, false"
    })
    void canHandleHappyPathScenarios(Event event, PreSubmitCallbackStage callbackStage, boolean expectedResult) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = editBailDocsAboutToSubmitHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource({
        ", , callbackStage must not be null",
        "ABOUT_TO_SUBMIT, , callback must not be null"
    })
    void canHandleCornerCaseScenarios(PreSubmitCallbackStage callbackStage,
                                      Callback<BailCase> callback,
                                      String expectedExceptionMessage) {
        try {
            editBailDocsAboutToSubmitHandler.canHandle(callbackStage, callback);
        } catch (Exception e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource({"generateNewFileAddedScenario", "generateFileUpdatedScenario", "generateDeletedFileScenario"})
    void handle(BailCase bailCase,
                BailCase bailCaseBefore,
                BailCaseFieldDefinition caseFieldDefinition,
                String expectedSuppliedBy,
                DocumentTag expectedDocumentTag) {
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(bailCase);

        if (!isDeletedFileScenario(expectedSuppliedBy)) { // no need to mock for this scenario
            given(callback.getCaseDetailsBefore()).willReturn(Optional.of(caseDetailsBefore));
            given(caseDetailsBefore.getCaseData()).willReturn(bailCaseBefore);
        }

        PreSubmitCallbackResponse<BailCase> currentResult = editBailDocsAboutToSubmitHandler
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
            assertTrue(true, "file deleted as expected");
        } else {
            Assertions.fail();
        }
        then(editDocsCaseNoteService).should(times(1))
            .writeAuditCaseNoteForGivenCaseId(anyLong(), any(BailCase.class), any());
    }

    private boolean isDeletedFileScenario(String expectedSuppliedBy) {
        return expectedSuppliedBy == null;
    }

    /**
     * This method simulates adding new documents.
     *
     * @return parameters for the handle method to recreate different file addition scenarios
     */
    private static Object[] generateNewFileAddedScenario() {
        String expectedSuppliedBy = "some supplier";

        BailCase bailCaseWithTribunalDoc = buildBailCaseGivenParams(
            expectedSuppliedBy,
            DocumentTag.NONE,
            BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA
        );

        BailCase bailCaseWithHomeOfficeDoc = buildBailCaseGivenParams(
            expectedSuppliedBy,
            DocumentTag.NONE,
            HOME_OFFICE_DOCUMENTS_WITH_METADATA
        );

        BailCase bailCaseWithApplicantDoc = buildBailCaseGivenParams(
            expectedSuppliedBy,
            DocumentTag.NONE,
            BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA
        );

        BailCase bailCaseBefore = new BailCase();

        return new Object[] {
            new Object[] {bailCaseWithTribunalDoc, bailCaseBefore,
                BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA, expectedSuppliedBy, DocumentTag.NONE},
            new Object[] {bailCaseWithHomeOfficeDoc, bailCaseBefore, HOME_OFFICE_DOCUMENTS_WITH_METADATA,
                expectedSuppliedBy, DocumentTag.NONE},
            new Object[] {bailCaseWithApplicantDoc, bailCaseBefore,
                BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA, expectedSuppliedBy, DocumentTag.NONE}
        };
    }

    private static BailCase buildBailCaseGivenParams(String expectedSuppliedBy, DocumentTag documentTag,
                                                         BailCaseFieldDefinition fieldDefinition) {
        BailCase bailCase = new BailCase();
        bailCase.write(fieldDefinition, buildIdValuesGivenParams(documentTag, expectedSuppliedBy));
        return bailCase;
    }

    /**
     * This method simulates deleting existing documents.
     *
     * @return parameters for the handle method to recreate different delete file scenarios
     */
    private static Object[] generateDeletedFileScenario() {
        BailCase bailCase = new BailCase();

        BailCase bailCaseBefore = buildBailCaseGivenParams("some suppliedBy",
                                                                 BAIL_SUMMARY,
                                                           HOME_OFFICE_DOCUMENTS_WITH_METADATA);

        return new Object[] {
            new Object[] {bailCase, bailCaseBefore, HOME_OFFICE_DOCUMENTS_WITH_METADATA, null, BAIL_SUMMARY}
        };
    }

    /**
     * This method simulates updating the suppliedBy field for existing documents.
     *
     * @return parameters for the handle method to recreate different file updated scenarios
     */
    private static Object[] generateFileUpdatedScenario() {
        String expectedSuppliedBy = "updated supplier";
        BailCase bailCaseWithBailSummaryDoc = buildBailCaseGivenParams(expectedSuppliedBy,
                                                                       BAIL_SUMMARY,
                                                                       HOME_OFFICE_DOCUMENTS_WITH_METADATA);
        String beforeSupplier = "before supplier";
        BailCase bailCaseWithBailSummaryDocBefore = buildBailCaseGivenParams(beforeSupplier,
                                                                             BAIL_SUMMARY,
                                                                             HOME_OFFICE_DOCUMENTS_WITH_METADATA);

        BailCase bailCaseWithTribunalDoc = buildBailCaseGivenParams(expectedSuppliedBy,
                                                                    DocumentTag.NONE,
                                                                    TRIBUNAL_DOCUMENTS_WITH_METADATA);
        BailCase bailCaseWithTribunalDocBefore =
            buildBailCaseGivenParams(beforeSupplier, DocumentTag.NONE, TRIBUNAL_DOCUMENTS_WITH_METADATA);

        return new Object[] {
            new Object[] {bailCaseWithBailSummaryDoc, bailCaseWithBailSummaryDocBefore,
                HOME_OFFICE_DOCUMENTS_WITH_METADATA, expectedSuppliedBy, BAIL_SUMMARY},
            new Object[] {bailCaseWithTribunalDoc, bailCaseWithTribunalDocBefore, TRIBUNAL_DOCUMENTS_WITH_METADATA,
                expectedSuppliedBy, DocumentTag.NONE}
        };
    }

    private static List<IdValue<DocumentWithMetadata>> buildIdValuesGivenParams(DocumentTag documentTag,
                                                                                String suppliedBy) {
        IdValue<DocumentWithMetadata> idValueWithNoTag = new IdValue<>(ID_VALUE, buildValueWithNoTag(documentTag,
                                                                                                     suppliedBy));
        return Collections.singletonList(idValueWithNoTag);
    }

    private static DocumentWithMetadata buildValueWithNoTag(DocumentTag documentTag, String suppliedBy) {
        Document doc = new Document("http://dm-store:4506/documents/80e2af54-7a93-498f-af55-fe190f3224d2",
                                    "http://dm-store:4506/documents/80e2af54-7a93-498f-af55-fe190f3224d2/binary",
                                    "Screenshot 2020-03-06 at 10.07.01.jpg",
                                    "hash");
        return new DocumentWithMetadata(doc, "some desc", "2020-01-01", documentTag, suppliedBy);
    }
}
