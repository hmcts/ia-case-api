package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALL_FTPA_APPELLANT_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICATION_APPELLANT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_DECISION_AND_REASONS_DOC_UPLOAD;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_FIRST_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_SECOND_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPELLANT_FTPA_DECISION_VISIBLE_TO_ALL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECOND_FTPA_DECISION_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUMMARISE_FTPA_DECISION_AND_REASONS_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_FTPA_DECISION_OUTCOME_TYPE;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CurrentFtpaDecisionResolver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FtpaDisplayService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EditFtpaDecisionHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CurrentFtpaDecisionResolver resolver;
    @Mock
    private FtpaDisplayService ftpaDisplayService;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DocumentWithMetadata documentWithMetadata;

    private final Document document = new Document("url", "binaryUrl", "decision.pdf");

    private EditFtpaDecisionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EditFtpaDecisionHandler(resolver, ftpaDisplayService, documentReceiver, documentsAppender);

        when(callback.getEvent()).thenReturn(Event.UPDATE_FTPA_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(resolver.getApplicantType(asylumCase)).thenReturn(Optional.of("appellant"));
    }

    @Test
    void should_update_outcome_and_add_new_document() {
        FtpaApplications ftpaApplication = FtpaApplications.builder().ftpaApplicant("appellant").build();
        List<IdValue<FtpaApplications>> ftpaList = List.of(new IdValue<>("1", ftpaApplication));
        List<IdValue<DocumentWithMetadata>> appendedDocs = List.of(new IdValue<>("1", documentWithMetadata));

        when(asylumCase.read(FTPA_APPLICATION_APPELLANT_DOCUMENT, Document.class)).thenReturn(Optional.of(document));
        when(resolver.isDecisionChanged(asylumCase)).thenReturn(true);
        when(resolver.isDocumentUploaded(asylumCase)).thenReturn(true);
        when(resolver.getCurrentOutcomeField(asylumCase, "appellant"))
            .thenReturn(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE);
        when(asylumCase.read(UPDATE_FTPA_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("granted"));
        when(asylumCase.read(FTPA_DECISION_AND_REASONS_DOC_UPLOAD, Document.class)).thenReturn(Optional.of(document));
        when(asylumCase.read(SUMMARISE_FTPA_DECISION_AND_REASONS_DOCUMENT, String.class))
            .thenReturn(Optional.of("summary"));
        when(documentReceiver.receive(document, "summary", DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(documentWithMetadata);
        when(documentsAppender.append(any(), eq(List.of(documentWithMetadata)))).thenReturn(appendedDocs);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(ftpaList));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, "granted");
        verify(asylumCase).write(FTPA_FIRST_DECISION, "granted");
        verify(asylumCase).write(IS_APPELLANT_FTPA_DECISION_VISIBLE_TO_ALL, YesOrNo.YES);
        verify(asylumCase).write(ALL_FTPA_APPELLANT_DECISION_DOCS, appendedDocs);
        verify(asylumCase).write(FTPA_APPLICATION_APPELLANT_DOCUMENT, document);
        verify(ftpaDisplayService).mapFtpaDecision(false, asylumCase, "APPELLANT", ftpaApplication);
        verify(asylumCase).write(FTPA_LIST, ftpaList);
        verify(resolver).clearUpdateFtpaDecisionFields(asylumCase);
    }

    @Test
    void should_update_second_decision_for_legacy_case_without_document() {
        when(resolver.isDecisionChanged(asylumCase)).thenReturn(true);
        when(resolver.isDocumentUploaded(asylumCase)).thenReturn(false);
        when(resolver.getCurrentOutcomeField(asylumCase, "appellant"))
            .thenReturn(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE);
        when(asylumCase.read(UPDATE_FTPA_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("refused"));
        when(asylumCase.read(SECOND_FTPA_DECISION_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(FTPA_SECOND_DECISION, "refused");
        verify(asylumCase).write(IS_APPELLANT_FTPA_DECISION_VISIBLE_TO_ALL, YesOrNo.NO);
        verify(documentReceiver, never()).receive(any(), anyString(), any());
        verify(ftpaDisplayService, never()).mapFtpaDecision(anyBoolean(), any(), anyString(), any());
        verify(resolver).clearUpdateFtpaDecisionFields(asylumCase);
    }

    @Test
    void should_throw_when_no_applicant_type() {
        when(resolver.getApplicantType(asylumCase)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Can't edit FTPA on this case: No FTPA present");
    }

    @Test
    void should_only_handle_about_to_submit_update_ftpa_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                assertThat(handler.canHandle(stage, callback)).isEqualTo(
                    stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event == Event.UPDATE_FTPA_DECISION);
            }
        }
    }

    @Test
    void should_throw_if_cannot_handle() {
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
