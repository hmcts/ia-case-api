package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.HO_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadDecisionLetterHandlerTest {

    private final Document someDoc = new Document(
        "some url",
        "some binary url",
        "some filename");

    private final DocumentWithMetadata someLegalRepDocument = new DocumentWithMetadata(
        someDoc,
        "some description",
        "21/07/2021",
        DocumentTag.APPEAL_SUBMISSION,
        "some supplier"
    );

    private final DocumentWithMetadata homeOfficeDecisionLetter = new DocumentWithMetadata(
        someDoc,
        "the home office decision letter",
        "21/07/2021",
        DocumentTag.HO_DECISION_LETTER,
        "the home office"
    );

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

    private List<IdValue<DocumentWithMetadata>> allLegalRepDocuments;
    private DocumentWithDescription appealForm1;
    private DocumentWithMetadata appealForm1WithMetadata;
    private UploadDecisionLetterHandler uploadDecisionLetterHandler;

    @BeforeEach
    public void setUp() {

        uploadDecisionLetterHandler =
            new UploadDecisionLetterHandler(documentReceiver, documentsAppender);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "REQUEST_CASE_BUILDING"})
    void should_append_home_office_decision_letter_to_legal_rep_documents_if_not_present(Event event) {

        when(callback.getEvent()).thenReturn(event);

        allLegalRepDocuments = Arrays.asList(
            new IdValue<>("1", someLegalRepDocument)
        );

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

        when(documentsAppender.prepend(allLegalRepDocuments, noticeOfDecisionWithMetadata))
            .thenReturn(allLegalRepDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = uploadDecisionLetterHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).prepend(
            allLegalRepDocuments,
            noticeOfDecisionWithMetadata
        );
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "REQUEST_CASE_BUILDING"})
    void should_not_append_home_office_decision_letter_to_legal_rep_documents_if_already_present(Event event) {

        when(callback.getEvent()).thenReturn(event);

        allLegalRepDocuments = Arrays.asList(
            new IdValue<>("1", homeOfficeDecisionLetter)
        );

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

        when(documentsAppender.prepend(allLegalRepDocuments, noticeOfDecisionWithMetadata))
            .thenReturn(allLegalRepDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = uploadDecisionLetterHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(0)).prepend(
            allLegalRepDocuments,
            noticeOfDecisionWithMetadata
        );
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                for (State state : State.values()) {

                    when(callback.getCaseDetails().getState()).thenReturn(state);

                    boolean canHandle = uploadDecisionLetterHandler.canHandle(callbackStage, callback);

                    if (callbackStage == ABOUT_TO_SUBMIT
                        && state != State.APPEAL_STARTED
                    ) {
                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }
            }
        }

        reset(callback);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadDecisionLetterHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadDecisionLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadDecisionLetterHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadDecisionLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadDecisionLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_STARTED);
        assertThatThrownBy(() -> uploadDecisionLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
