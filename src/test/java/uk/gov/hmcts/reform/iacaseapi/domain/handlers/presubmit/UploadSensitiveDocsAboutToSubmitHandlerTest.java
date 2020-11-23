package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_SENSITIVE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_SENSITIVE_DOCS_FILE_UPLOADS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_SENSITIVE_DOCS_IS_APPELLANT_RESPONDENT;

import com.launchdarkly.shaded.org.joda.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class UploadSensitiveDocsAboutToSubmitHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithDescription>>> docWithDescListCaptor;

    private UploadSensitiveDocsAboutToSubmitHandler handler;
    private Document doc;
    private String someSensitiveDocDesc;
    private String suppliedBy;
    private DocumentWithDescription uploadedSensitiveDoc;
    private DocumentWithMetadata someDocWithMetadata;

    @BeforeEach
    public void setUp() {
        handler = new UploadSensitiveDocsAboutToSubmitHandler(documentReceiver, documentsAppender);
    }

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = handler.canHandle(scenario.callbackStage, callback);

        Assertions.assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private static List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Test
    public void handle() {
        mockCallback();
        mockAsylumCase();
        mockDocumentReceiver();
        mockDocumentAppender();

        PreSubmitCallbackResponse<AsylumCase> actualResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        Optional<List<IdValue<DocumentWithMetadata>>> actualUploadSensitiveDocsOptional =
            actualResponse.getData().read(UPLOAD_SENSITIVE_DOCS);

        if (actualUploadSensitiveDocsOptional.isPresent()) {
            assertEquals(someDocWithMetadata, actualUploadSensitiveDocsOptional.get().get(0).getValue());
        } else {
            fail("expected sensitive document not found");
        }

        verify(callback, times(1)).getCaseDetails();
        verify(caseDetails, times(1)).getCaseData();

        verify(documentReceiver, times(1))
            .tryReceiveAll(docWithDescListCaptor.capture(), eq(DocumentTag.SENSITIVE_DOCUMENT), eq(suppliedBy));
        assertEquals(uploadedSensitiveDoc, docWithDescListCaptor.getValue().get(0).getValue());

    }

    private void mockDocumentAppender() {
        when(documentsAppender.append(anyList(), anyList()))
            .thenReturn(Collections.singletonList(new IdValue<>("1", someDocWithMetadata)));
    }

    private void mockDocumentReceiver() {
        someDocWithMetadata = new DocumentWithMetadata(
            doc,
            someSensitiveDocDesc,
            LocalDate.now().toString(),
            DocumentTag.SENSITIVE_DOCUMENT,
            suppliedBy);
        when(documentReceiver.tryReceiveAll(anyList(), eq(DocumentTag.SENSITIVE_DOCUMENT), eq(suppliedBy)))
            .thenReturn(Collections.singletonList(someDocWithMetadata));
    }

    private void mockCallback() {
        when(callback.getEvent()).thenReturn(Event.UPLOAD_SENSITIVE_DOCUMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
    }

    private void mockAsylumCase() {
        AsylumCase asylum = new AsylumCase();
        asylum.write(UPLOAD_SENSITIVE_DOCS_FILE_UPLOADS, Collections.singletonList(buildSomeDocumentWithDescription()));
        suppliedBy = "The respondent";
        asylum.write(UPLOAD_SENSITIVE_DOCS_IS_APPELLANT_RESPONDENT, suppliedBy);
        when(caseDetails.getCaseData()).thenReturn(asylum);
    }

    @NotNull
    private IdValue<DocumentWithDescription> buildSomeDocumentWithDescription() {
        doc = new Document(
            "http://someUrl",
            "http://someUrl/bin",
            "some sensitive document uploaded");
        someSensitiveDocDesc = "some sensitive doc desc";
        uploadedSensitiveDoc = new DocumentWithDescription(doc, someSensitiveDocDesc);
        return new IdValue<>("1", uploadedSensitiveDoc);
    }

    @Test
    public void should_throw_exception() {

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

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Value
    private static class CanHandleScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean canHandleExpectedResult;

        public static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (PreSubmitCallbackStage cb : PreSubmitCallbackStage.values()) {
                for (Event e : Event.values()) {
                    if (Event.UPLOAD_SENSITIVE_DOCUMENTS.equals(e)
                        && PreSubmitCallbackStage.ABOUT_TO_SUBMIT.equals(cb)) {
                        scenarios.add(new CanHandleScenario(e, cb, true));
                    } else {
                        scenarios.add(new CanHandleScenario(e, cb, false));
                    }
                }
            }
            return scenarios;
        }
    }
}
