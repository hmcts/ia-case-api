package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.HO_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UploadDecisionLetterHandlerTest {

    private final Document someDoc = new Document(
        "some url",
        "some binary url",
        "some filename");
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
    private DocumentWithDescription noticeOfDecision1;

    @Mock
    private DocumentWithMetadata noticeOfDecision1WithMetadata;

    @Mock
    private List<IdValue<DocumentWithMetadata>> allLegalRepDocuments;

    @InjectMocks
    private UploadDecisionLetterHandler handler;

    @BeforeEach
    public void setUp() {
        given(callback.getEvent()).willReturn(SUBMIT_APPEAL);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
    }

    @Test
    void handle_decision_document_for_legal_rep_journey() {

        List<IdValue<DocumentWithDescription>> noticeOfDecisionDocument =
            Arrays.asList(
                new IdValue<>("1", noticeOfDecision1)
            );

        List<DocumentWithMetadata> noticeOfDecisionWithMetadata =
            Arrays.asList(
                noticeOfDecision1WithMetadata
            );

        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.of(noticeOfDecisionDocument));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(allLegalRepDocuments));

        when(documentReceiver.tryReceive(noticeOfDecision1, HO_DECISION_LETTER))
            .thenReturn(Optional.of(noticeOfDecision1WithMetadata));

        when(documentsAppender.append(allLegalRepDocuments, noticeOfDecisionWithMetadata))
            .thenReturn(allLegalRepDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).append(
            allLegalRepDocuments,
            noticeOfDecisionWithMetadata
        );
        verify(asylumCase).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        verify(asylumCase).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(asylumCase, times(0)).read(APPELLANT_DOCUMENTS);

    }

    @Test
    void handle_decision_document_for_appellant_journey() {

        List<IdValue<DocumentWithDescription>> noticeOfDecisionDocument =
            Arrays.asList(
                new IdValue<>("1", noticeOfDecision1)
            );

        List<DocumentWithMetadata> noticeOfDecisionWithMetadata =
            Arrays.asList(
                noticeOfDecision1WithMetadata
            );

        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.of(noticeOfDecisionDocument));
        when(asylumCase.read(APPELLANT_DOCUMENTS)).thenReturn(Optional.of(allLegalRepDocuments));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        when(documentReceiver.tryReceive(noticeOfDecision1, HO_DECISION_LETTER))
            .thenReturn(Optional.of(noticeOfDecision1WithMetadata));

        when(documentsAppender.append(allLegalRepDocuments, noticeOfDecisionWithMetadata))
            .thenReturn(allLegalRepDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).append(
            allLegalRepDocuments,
            noticeOfDecisionWithMetadata
        );
        verify(asylumCase).read(APPELLANT_DOCUMENTS);
        verify(asylumCase, times(0)).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(asylumCase).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
    }

    @Test
    void handle_should_error_on_notice_of_decision_document_is_not_present() {

        List<DocumentWithMetadata> noticeOfDecisionWithMetadata =
            Arrays.asList(
                noticeOfDecision1WithMetadata
            );

        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.empty());

        when(documentsAppender.append(allLegalRepDocuments, noticeOfDecisionWithMetadata))
            .thenReturn(allLegalRepDocuments);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("upload notice decision is not present");
    }

    @Test
    void should_not_allow_null_arguments() {

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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(ADD_CASE_NOTE);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
