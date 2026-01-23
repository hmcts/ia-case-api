package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealWasNotSubmittedSupportingDocumentsHandlerTest {
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
    private DocumentWithDescription appealWasNotSubmitted;
    @Mock
    private DocumentWithMetadata appealWasNotSubmittedWithMetadata;
    @Mock
    private DocumentWithDescription updatedAppealWasNotSubmitted;
    @Mock
    private DocumentWithMetadata updatedAppealWasNotSubmittedWithMetadata;
    @Mock
    FeatureToggler featureToggler;

    private List<IdValue<DocumentWithMetadata>> allLegalRepDocuments;
    private AppealWasNotSubmittedSupportingDocumentsHandler appealWasNotSubmittedSupportingDocumentsHandler;

    private final Document someDoc = new Document(
        "some url",
        "some binary url",
        "some filename");

    private final Document appealNotSubmittedDoc = new Document(
            "appeal not submitted url",
            "appeal not submitted binary url",
            "appeal not submitted filename");

    private final DocumentWithMetadata someLegalRepDocument = new DocumentWithMetadata(
        someDoc,
        "some description",
        "21/07/2021",
        DocumentTag.APPEAL_SUBMISSION,
        "some supplier"
    );

    @BeforeEach
    public void setUp() {
        appealWasNotSubmittedSupportingDocumentsHandler =
            new AppealWasNotSubmittedSupportingDocumentsHandler(documentReceiver, documentsAppender, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("dlrm-internal-feature-flag", false)).thenReturn(true);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealWasNotSubmittedSupportingDocumentsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealWasNotSubmittedSupportingDocumentsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealWasNotSubmittedSupportingDocumentsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealWasNotSubmittedSupportingDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> appealWasNotSubmittedSupportingDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_STARTED);
        assertThatThrownBy(() -> appealWasNotSubmittedSupportingDocumentsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                for (State state : State.values()) {

                    when(callback.getCaseDetails().getState()).thenReturn(state);

                    boolean canHandle = appealWasNotSubmittedSupportingDocumentsHandler.canHandle(callbackStage, callback);

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
    void should_append_appeal_was_not_submitted_doc_to_legal_rep_documents_if_not_present() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        allLegalRepDocuments = List.of(
            new IdValue<>("1", someLegalRepDocument)
        );

        List<IdValue<DocumentWithDescription>> notSubmittedDocument =
            List.of(
                new IdValue<>("1", appealWasNotSubmitted)
            );

        List<DocumentWithMetadata> notSubmittedWithMetadata =
            List.of(
                appealWasNotSubmittedWithMetadata
            );

        when(asylumCase.read(APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS)).thenReturn(Optional.of(notSubmittedDocument));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(allLegalRepDocuments));

        when(documentReceiver.tryReceive(appealWasNotSubmitted, APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT))
            .thenReturn(Optional.of(appealWasNotSubmittedWithMetadata));

        when(documentsAppender.prepend(allLegalRepDocuments, notSubmittedWithMetadata))
            .thenReturn(allLegalRepDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealWasNotSubmittedSupportingDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).append(
            allLegalRepDocuments,
            notSubmittedWithMetadata,
            APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT
        );
    }

    @Test
    void should_update_appeal_was_not_submitted_doc_when_legal_rep_documents_is_present_and_updated() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        final DocumentWithMetadata legalRepAppealNotSubmittedDocument = new DocumentWithMetadata(
                appealNotSubmittedDoc,
                "appeal not submitted description",
                "21/07/2021",
                APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT,
                "some supplier"
        );

        allLegalRepDocuments = List.of(
                new IdValue<>("1", someLegalRepDocument),
                new IdValue<>("2", legalRepAppealNotSubmittedDocument)
        );

        final List<IdValue<DocumentWithDescription>> notSubmittedDocuments =
                List.of(new IdValue<>("1", updatedAppealWasNotSubmitted));

        when(updatedAppealWasNotSubmitted.getDescription()).thenReturn(Optional.of("Updated document description"));
        when(asylumCase.read(APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS)).thenReturn(Optional.of(notSubmittedDocuments));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(allLegalRepDocuments));

        when(documentReceiver.tryReceive(updatedAppealWasNotSubmitted, APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT))
            .thenReturn(Optional.of(updatedAppealWasNotSubmittedWithMetadata));

        final List<IdValue<DocumentWithMetadata>> allLegalRepDocumentsUpdated = List.of(
                new IdValue<>("1", someLegalRepDocument),
                new IdValue<>("2", updatedAppealWasNotSubmittedWithMetadata)
        );

        when(documentsAppender.append(allLegalRepDocuments,
                List.of(updatedAppealWasNotSubmittedWithMetadata),
                APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT)).thenReturn(allLegalRepDocumentsUpdated);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse
                = appealWasNotSubmittedSupportingDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(1)).append(
                allLegalRepDocuments,
                List.of(updatedAppealWasNotSubmittedWithMetadata),
                APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT);

    }

    @Test
    void should_remove_appeal_was_not_submitted_doc_to_legal_rep_documents_for_appellant_manual_appeals() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(appealWasNotSubmittedWithMetadata.getTag()).thenReturn(APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT);
        allLegalRepDocuments = List.of(
            new IdValue<>("1", someLegalRepDocument),
            new IdValue<>("2", appealWasNotSubmittedWithMetadata)
        );

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(allLegalRepDocuments));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealWasNotSubmittedSupportingDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();
        verify(asylumCase, times(1)).write(eq(LEGAL_REPRESENTATIVE_DOCUMENTS), anyList());
        verify(asylumCase, never()).read(APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS);
        verifyNoInteractions(documentReceiver);
        verifyNoInteractions(documentsAppender);
    }

    @Test
    void should_not_add_new_document_if_feature_toggler_switched_off() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(featureToggler.getValue("dlrm-internal-feature-flag", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealWasNotSubmittedSupportingDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(0)).prepend(
            any(),
            any()
        );
    }

    @Test
    void should_not_add_new_document_if_non_admin_submitting_the_appeal() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealWasNotSubmittedSupportingDocumentsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse).isNotNull();

        verify(documentsAppender, times(0)).prepend(
            any(),
            any()
        );
    }

}