package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.removedocs;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RemoveDocsAboutToSubmitHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @InjectMocks
    private RemoveDocsAboutToSubmitHandler removeDocsAboutToSubmitHandler;

    @ParameterizedTest
    @CsvSource({
            "REMOVE_DOCUMENTS, ABOUT_TO_SUBMIT, true",
            "START_APPEAL, ABOUT_TO_SUBMIT, false",
            "REMOVE_DOCUMENTS, ABOUT_TO_START, false"
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
