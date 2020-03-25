package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOW_PAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_CASE_BUILDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(JUnitParamsRunner.class)
public class RequestCaseBuildingMidHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private RequestCaseBuildingMidHandler requestCaseBuildingMidHandler = new RequestCaseBuildingMidHandler();

    @Test
    @Parameters({
        "MID_EVENT, REQUEST_CASE_BUILDING, true",
        "ABOUT_TO_SUBMIT, REQUEST_CASE_BUILDING, false",
        "MID_EVENT, SUBMIT_CLARIFYING_ANSWERS, false"
    })
    public void canHandle(PreSubmitCallbackStage callbackStage, Event event, boolean expectedResult) {
        given(callback.getEvent()).willReturn(event);

        boolean actualResult = requestCaseBuildingMidHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    @Parameters(method = "generateDifferentScenarios")
    public void handle(@Nullable List<IdValue<DocumentWithMetadata>> respondentDocs, YesOrNo expected) {
        given(callback.getEvent()).willReturn(REQUEST_CASE_BUILDING);
        given(callback.getCaseDetails()).willReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(RESPONDENT_DOCUMENTS, respondentDocs);

        given(caseDetails.getCaseData()).willReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> actualResult = requestCaseBuildingMidHandler.handle(MID_EVENT, callback);

        Optional<YesOrNo> actualShowPageFlag = actualResult.getData().read(SHOW_PAGE_FLAG, YesOrNo.class);
        assertEquals(expected, actualShowPageFlag.orElse(null));
    }

    private Object[] generateDifferentScenarios() {
        Document doc = new Document("", "", "");
        DocumentWithMetadata value =
            new DocumentWithMetadata(doc, "desc", "", DocumentTag.NONE);
        IdValue<DocumentWithMetadata> idValueDoc = new IdValue<>("1", value);
        return new Object[]{
            new Object[]{Collections.emptyList(), YesOrNo.YES},
            new Object[]{null, YesOrNo.YES},
            new Object[]{Collections.singletonList(idValueDoc), YesOrNo.NO}
        };
    }

    @Test(expected = NullPointerException.class)
    @Parameters({"null,null", "ABOUT_TO_START, null"})
    public void given_null_callback_should_throw_exception(@Nullable PreSubmitCallbackStage callbackStage,
                                                           @Nullable Callback<AsylumCase> callback) {
        requestCaseBuildingMidHandler.canHandle(callbackStage, callback);
    }

}