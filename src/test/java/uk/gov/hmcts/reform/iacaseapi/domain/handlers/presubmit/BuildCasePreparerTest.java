package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BuildCasePreparerTest {

    private final String argumentEvidence01FileName = "Evidence01";
    private final String argumentEvidence02FileName = "Evidence02";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentWithMetadata caseArgument1WithMetadata;
    @Mock
    private DocumentWithMetadata caseArgument2WithMetadata;
    @Mock
    private Document document1;
    @Mock
    private Document document2;
    @Captor
    private ArgumentCaptor<String> fileNames;


    private BuildCasePreparer buildCasePreparer;

    @BeforeEach
    public void setUp() {

        buildCasePreparer =
            new BuildCasePreparer();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        List<IdValue<DocumentWithMetadata>> legalRepDocuments =

            Arrays.asList(
                new IdValue<>("1", caseArgument1WithMetadata),
                new IdValue<>("2", caseArgument2WithMetadata)
            );

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(legalRepDocuments));

        when(caseArgument1WithMetadata.getTag()).thenReturn(DocumentTag.CASE_ARGUMENT);
        when(caseArgument2WithMetadata.getTag()).thenReturn(DocumentTag.CASE_ARGUMENT);

        when(caseArgument1WithMetadata.getDocument()).thenReturn(document1);
        when(caseArgument2WithMetadata.getDocument()).thenReturn(document2);

        when(document1.getDocumentFilename()).thenReturn(argumentEvidence01FileName);
        when(document2.getDocumentFilename()).thenReturn(argumentEvidence02FileName);

    }

    @Test
    void should_set_uploaded_documents() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(caseArgument1WithMetadata, times(1)).getTag();
        verify(caseArgument2WithMetadata, times(1)).getTag();
        verify(document1, times(1)).getDocumentFilename();
        verify(document2, times(1)).getDocumentFilename();
        verify(asylumCase).write(eq(UPLOADED_LEGAL_REP_BUILD_CASE_DOCS), fileNames.capture());

        final String value = fileNames.getValue();
        assertTrue(value.contains(argumentEvidence01FileName));
        assertTrue(value.contains(argumentEvidence02FileName));

    }

    @Test
    void should_set_none_for_documents_not_loaded() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).read(LEGAL_REPRESENTATIVE_DOCUMENTS);

        verify(asylumCase).write(eq(UPLOADED_LEGAL_REP_BUILD_CASE_DOCS), fileNames.capture());

        final String value = fileNames.getValue();
        assertTrue(value.contains("- None"));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> buildCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> buildCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = buildCasePreparer.canHandle(callbackStage, callback);

                if (callback.getEvent() == Event.BUILD_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> buildCasePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> buildCasePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCasePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> buildCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
