package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.HO_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.junit.Before;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@RunWith(JUnitParamsRunner.class)
public class UploadDecisionLetterHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DocumentWithMetadata newLegalRepDoc;

    @InjectMocks
    private UploadDecisionLetterHandler handler;

    private final Document someDoc = new Document(
        "some url",
        "some binary url",
        "some filename");


    @Before
    public void setUp() {
        given(callback.getEvent()).willReturn(Event.SUBMIT_APPEAL);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(asylumCase.read(
            eq(AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT), eq(Document.class)))
            .willReturn(Optional.of(someDoc)
            );
    }

    @Test
    @Parameters(method = "generateTestScenarios")
    public void canHandle(TestScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.getEvent());
        given(asylumCase.read(
            eq(AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT), eq(Document.class)))
            .willReturn(Optional.ofNullable(scenario.document));

        boolean actualResult = handler.canHandle(scenario.callbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.canBeHandledExpected);
    }

    @SuppressWarnings("unused")
    private List<TestScenario> generateTestScenarios() {
        return TestScenario.testScenarioBuilder();
    }

    @Value
    private static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        Document document;
        boolean canBeHandledExpected;

        public static List<TestScenario> testScenarioBuilder() {
            List<TestScenario> testScenarioList = new ArrayList<>();
            for (Event e : Event.values()) {
                for (PreSubmitCallbackStage cb : PreSubmitCallbackStage.values()) {
                    Document someDoc = new Document(
                        "some url",
                        "some binary url",
                        "some filename");
                    if (e.equals(Event.SUBMIT_APPEAL) && cb.equals(ABOUT_TO_SUBMIT)) {
                        testScenarioList.add(new TestScenario(e, cb, someDoc, true));
                    } else {
                        testScenarioList.add(new TestScenario(e, cb, someDoc, false));
                    }
                    testScenarioList.add(new TestScenario(e, cb, null, false));
                }
            }
            return testScenarioList;
        }
    }


    @Test
    public void handle() {
        given(documentReceiver.receive(any(Document.class), anyString(), eq(HO_DECISION_LETTER)))
            .willReturn(newLegalRepDoc);

        given(documentsAppender.append(anyList(), anyList()))
            .willReturn(Collections.emptyList());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(documentReceiver, times(1)).receive(someDoc, "", HO_DECISION_LETTER);
        verify(documentsAppender, times(1)).append(
            Collections.emptyList(),
            Collections.singletonList(newLegalRepDoc)
        );
        verify(asylumCase).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT);
        verify(asylumCase).clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}