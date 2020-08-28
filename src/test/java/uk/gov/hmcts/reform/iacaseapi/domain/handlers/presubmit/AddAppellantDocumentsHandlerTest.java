package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AddAppellantDocumentsHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;
    @Mock private
    DocumentsAppender documentsAppender;

    AddAppellantDocumentsHandler addAppellantDocumentsHandler;

    @BeforeEach
    void setupHandler() {
        addAppellantDocumentsHandler = new AddAppellantDocumentsHandler(documentsAppender);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> addAppellantDocumentsHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> addAppellantDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = addAppellantDocumentsHandler.canHandle(callbackStage, callback);

                if ((event == Event.SUBMIT_REASONS_FOR_APPEAL || event == Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS)
                        && callbackStage == ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> addAppellantDocumentsHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addAppellantDocumentsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addAppellantDocumentsHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addAppellantDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void addsReasonsForAppealEvidenceToAppellantEvidence() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        DocumentWithMetadata reasonsForAppealEvidence = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFielname"),
                "description",
                "dateUploaded",
                DocumentTag.ADDITIONAL_EVIDENCE
        );
        List<IdValue<DocumentWithMetadata>> reasonsForAppealEvidenceList = asList(
                new IdValue<DocumentWithMetadata>(
                        "1",
                        reasonsForAppealEvidence
                )
        );

        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS))
                .thenReturn(Optional.of(reasonsForAppealEvidenceList));
        when(asylumCase.read(AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS))
                .thenReturn(Optional.empty());
        when(documentsAppender.append(Collections.emptyList(), asList(reasonsForAppealEvidence)))
                .thenReturn(reasonsForAppealEvidenceList);

        addAppellantDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(AsylumCaseFieldDefinition.APPELLANT_DOCUMENTS, reasonsForAppealEvidenceList);
    }

    @Test
    void addsClarifyQuestionsEvidenceToAppellantEvidence() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS))
                .thenReturn(Optional.empty());
        Document clarifyingQuestionEvidence = new Document("documentUrl", "binaryUrl", "documentFielname");
        when(asylumCase.read(AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS))
                .thenReturn(Optional.of(asList(
                        new IdValue<>(
                                "1",
                                new ClarifyingQuestionAnswer(
                                        "dateSent",
                                        "dateDue",
                                        "dateResponded",
                                        "question",
                                        "answer",
                                        asList(
                                                new IdValue<Document>(
                                                        "1",
                                                        clarifyingQuestionEvidence
                                                )
                                        )
                                )
                        ),
                        new IdValue<>(
                                "2",
                                new ClarifyingQuestionAnswer(
                                        "dateSent2",
                                        "dateDue2",
                                        "dateResponded2",
                                        "question2",
                                        "answer2",
                                        null
                                )
                        )

                )));
        List appellantEvidence = asList(new DocumentWithMetadata(
                clarifyingQuestionEvidence,
                "Clarifying question evidence",
                "dateResponded",
                DocumentTag.ADDITIONAL_EVIDENCE
        ));
        when(documentsAppender.append(Collections.emptyList(), appellantEvidence))
                .thenReturn(appellantEvidence);

        addAppellantDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(AsylumCaseFieldDefinition.APPELLANT_DOCUMENTS, appellantEvidence);
    }

    @Test
    void donotAddAppellantDocumentsIfNoEvidenceHasBeenUploaded() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS))
                .thenReturn(Optional.of(asList(
                        new IdValue<>(
                                "1",
                                new ClarifyingQuestionAnswer(
                                        "dateSent",
                                        "dateDue",
                                        "dateResponded",
                                        "question",
                                        "answer",
                                        null
                                )
                        ),
                        new IdValue<>(
                                "2",
                                new ClarifyingQuestionAnswer(
                                        "dateSent2",
                                        "dateDue2",
                                        "dateResponded2",
                                        "question2",
                                        "answer2",
                                        null
                                )
                        )

                )));

        addAppellantDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(any(), any());
    }
}
