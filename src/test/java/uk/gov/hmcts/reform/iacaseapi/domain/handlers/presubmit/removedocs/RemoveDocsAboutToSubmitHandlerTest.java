package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.removedocs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.ADDITIONAL_EVIDENCE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RemoveDocsAboutToSubmitHandlerTest {

    public static final String ID_VALUE = "0a165fa5-086b-49d6-8b7e-f00ed34d941a";

    private final Document newUtTransferOrderDocumentOne = new Document(
            "someurl_ut_transfer_order_one",
            "someurl_ut_transfer_order_binaryurl_one",
            "someurl_ut_transfer_order_filename_one.pdf");

    private final Document newEjpAppealFormDocumentOne = new Document(
            "someurl_ejp_appeal_form_one",
            "someurl_ejp_appeal_form_binaryurl_one",
            "someurl_ejp_appeal_form_filename_one.pdf");

    private final DocumentWithMetadata someTribunalMetaOne = new DocumentWithMetadata(
            newUtTransferOrderDocumentOne,
            "some description",
            "21/07/2021",
            DocumentTag.UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT,
            "some supplier"
    );

    private final DocumentWithMetadata someTribunalMetaTwo = new DocumentWithMetadata(
            newEjpAppealFormDocumentOne,
            "some description",
            "21/07/2021",
            DocumentTag.IAUT_2_FORM,
            "some supplier"
    );

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCaseEjp;
    @Mock
    private RemoveDocsService removeDocService;
    @InjectMocks
    private RemoveDocsAboutToSubmitHandler removeDocsAboutToSubmitHandler;

    @ParameterizedTest
    @CsvSource({
            "REMOVE_DOCS, ABOUT_TO_SUBMIT, true",
            "START_APPEAL, ABOUT_TO_SUBMIT, false",
            "REMOVE_DOCS, ABOUT_TO_START, false"
    })
    void canHandleHappyPathScenarios(Event event, PreSubmitCallbackStage callbackStage, boolean expectedResult) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = removeDocsAboutToSubmitHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource({
            ", , callbackStage must not be null",
            "ABOUT_TO_SUBMIT, , callback must not be null"
    })
    void canHandleCornerCaseScenarios(PreSubmitCallbackStage callbackStage,
                                      Callback<AsylumCase> callback,
                                      String expectedExceptionMessage) {
        try {
            removeDocsAboutToSubmitHandler.canHandle(callbackStage, callback);
        } catch (Exception e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }


}
