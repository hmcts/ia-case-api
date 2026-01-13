package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_32;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateTribunalDecisionHandlerTest {
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Appender<DecisionAndReasons> decisionAndReasonsAppender;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<DecisionAndReasons>> allAppendedDecisionAndReasosn;
    @Mock
    private DecisionAndReasons existingDecision;
    @Mock
    List<IdValue<DocumentWithMetadata>> existingFtpaSetAsideDocuments;
    private final List<DecisionAndReasons> existingDecisions = singletonList(existingDecision);
    @Mock
    Document correctedDecisionDocument;
    @Mock
    Document rule32Document;
    @Mock
    DocumentWithMetadata ftpaSetAsideR32Document;
    @Mock
    List<IdValue<DocumentWithMetadata>> allFtpaSetAsideDocuments;
    @Mock
    private FeatureToggler featureToggler;
    @Captor
    private ArgumentCaptor<List<IdValue<DecisionAndReasons>>> existingDecisionsCaptor;
    @Captor private ArgumentCaptor<DecisionAndReasons> newDecisionCaptor;
    private UpdateTribunalDecisionHandler updateTribunalDecisionHandler;
    private final LocalDate now = LocalDate.now();
    private final String summarisedChanges = "Summarise document example";
    @Mock
    private DocumentWithMetadata decisionsAndReasonsDocumentWithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> newUpdateTribunalDecisionDocs;


    @BeforeEach
    public void setUp() {
        updateTribunalDecisionHandler = new UpdateTribunalDecisionHandler(dateProvider,
                decisionAndReasonsAppender,documentReceiver,documentsAppender, featureToggler);

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
            .thenReturn(Optional.of(UNDER_RULE_31));
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(existingDecisions));
        when(decisionAndReasonsAppender.append(any(DecisionAndReasons.class), anyList())).thenReturn(allAppendedDecisionAndReasosn);
    }

    @Test
    void should_write_IS_DECISION_RULE_31_NO() {
        final DynamicList dynamicList = new DynamicList(
                new Value("dismissed", "No"),
                newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(UPDATED_APPEAL_DECISION, "Dismissed");
        verify(asylumCase, times(1)).write(IS_DECISION_RULE31_CHANGED, YES);
    }

    @Test
    void should_write_updated_appeal_decision_dismissed() {
        final DynamicList dynamicList = new DynamicList(
            new Value("dismissed", "Yes, change decision to Dismissed"),
            newArrayList()
        );

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(decisionAndReasonsAppender, times(1))
            .append(newDecisionCaptor.capture(), existingDecisionsCaptor.capture());

        final DecisionAndReasons capturedDecision = newDecisionCaptor.getValue();
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(UPDATED_APPEAL_DECISION, "Dismissed");
        verify(asylumCase, times(1)).write(IS_DECISION_RULE31_CHANGED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CORRECTED_DECISION_AND_REASONS, allAppendedDecisionAndReasosn);
        verify(asylumCase).clear(FTPA_APPELLANT_SUBMITTED);
        verify(asylumCase).clear(FTPA_RESPONDENT_SUBMITTED);
        assertThat(capturedDecision.getUpdatedDecisionDate()).isEqualTo(now.toString());
    }

    @Test
    void should_write_updated_appeal_decision_allowed() {
        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(decisionAndReasonsAppender, times(1))
            .append(newDecisionCaptor.capture(), existingDecisionsCaptor.capture());

        final DecisionAndReasons capturedDecision = newDecisionCaptor.getValue();
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(UPDATED_APPEAL_DECISION, "Allowed");
        verify(asylumCase, times(1)).write(IS_DECISION_RULE31_CHANGED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CORRECTED_DECISION_AND_REASONS, allAppendedDecisionAndReasosn);
        verify(asylumCase, times(1)).write(UPDATE_TRIBUNAL_DECISION_DATE, now.toString());
        assertThat(capturedDecision.getUpdatedDecisionDate()).isEqualTo(now.toString());
        verify(asylumCase).clear(FTPA_APPELLANT_SUBMITTED);
        verify(asylumCase).clear(FTPA_RESPONDENT_SUBMITTED);
    }

    @Test
    void should_write_updated_decision_document_if_has_corrected_document() {
        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class))
            .thenReturn(Optional.of(correctedDecisionDocument));
        when(asylumCase.read(SUMMARISE_TRIBUNAL_DECISION_AND_REASONS_DOCUMENT, String.class))
            .thenReturn(Optional.of(summarisedChanges));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(decisionAndReasonsAppender, times(1))
            .append(newDecisionCaptor.capture(), existingDecisionsCaptor.capture());

        final DecisionAndReasons capturedDecision = newDecisionCaptor.getValue();
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(UPDATED_APPEAL_DECISION, "Allowed");
        verify(asylumCase, times(1)).write(CORRECTED_DECISION_AND_REASONS, allAppendedDecisionAndReasosn);
        assertThat(capturedDecision.getUpdatedDecisionDate()).isEqualTo(now.toString());
        assertThat(capturedDecision.getDateDocumentAndReasonsDocumentUploaded()).isEqualTo(now.toString());
        assertThat(capturedDecision.getDocumentAndReasonsDocument()).isEqualTo(correctedDecisionDocument);
        assertThat(capturedDecision.getSummariseChanges()).isEqualTo(summarisedChanges);
    }

    @Test
    void should_write_set_aside_documents_if_is_r32() {

        LocalDate currentDate = LocalDate.now();
        when(dateProvider.now()).thenReturn(currentDate);

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_32));

        when(asylumCase.read(RULE_32_NOTICE_DOCUMENT, Document.class))
                .thenReturn(Optional.of(rule32Document));
        when(asylumCase.read(ALL_SET_ASIDE_DOCS))
                .thenReturn(Optional.of(existingFtpaSetAsideDocuments));

        when(documentReceiver.receive(rule32Document, "",DocumentTag.FTPA_SET_ASIDE)).thenReturn(ftpaSetAsideR32Document);

        List<DocumentWithMetadata> ftpaSetAsideNewDocuments =
                Arrays.asList(
                        documentReceiver.receive(rule32Document,"",DocumentTag.FTPA_SET_ASIDE));

        when(documentsAppender.append(existingFtpaSetAsideDocuments, ftpaSetAsideNewDocuments)).thenReturn(allFtpaSetAsideDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(ALL_SET_ASIDE_DOCS, allFtpaSetAsideDocuments);
        verify(asylumCase, times(1)).write(UPDATE_TRIBUNAL_DECISION_DATE_RULE_32, currentDate.toString());
        verify(asylumCase, times(1)).write(REASON_REHEARING_RULE_32, "Set aside and to be reheard under rule 32");

    }

    @Test
    void should_write_set_reheard_case_flag_if_is_r32() {

        LocalDate currentDate = LocalDate.now();
        when(dateProvider.now()).thenReturn(currentDate);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_32));
        when(asylumCase.read(RULE_32_NOTICE_DOCUMENT, Document.class))
                .thenReturn(Optional.of(rule32Document));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "");
    }

    @Test
    void should_not_write_set_reheard_case_flag_if_is_r32_and_feature_flag_is_false() {

        LocalDate currentDate = LocalDate.now();
        when(dateProvider.now()).thenReturn(currentDate);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_32));
        when(asylumCase.read(RULE_32_NOTICE_DOCUMENT, Document.class))
                .thenReturn(Optional.of(rule32Document));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, NO);
        verify(asylumCase, times(0)).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(STITCHING_STATUS, "");
    }

    @Test
    void should_throw_on_missing_decision_and_reason_doc() {
        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("decisionAndReasonDocsUpload is not present");
    }

    @Test
    void should_throw_on_missing_rule_32_notice_document() {

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_32));

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Rule 32 notice document is not present");
    }

    @Test
    void should_throw_on_missing_summarise_decision_and_reason_doc() {
        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class))
            .thenReturn(Optional.of(correctedDecisionDocument));

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("summariseTribunalDecisionAndReasonsDocument is not present");
    }

    @Test
    void should_throw_on_missing_types_of_update_tribunal_decision() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("typesOfUpdateTribunalDecision is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateTribunalDecisionHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_TRIBUNAL_DECISION
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
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

        assertThatThrownBy(() -> updateTribunalDecisionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_append_update_tribunal_decision_document_if_yes() {
        List<DocumentWithMetadata> decisionsAndReasonsDocumentsWithMetadata =
            Arrays.asList(decisionsAndReasonsDocumentWithMetadata);

        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );

        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments = new ArrayList<>();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SUMMARISE_TRIBUNAL_DECISION_AND_REASONS_DOCUMENT, String.class))
            .thenReturn(Optional.of(summarisedChanges));
        when(asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class))
            .thenReturn(Optional.of(correctedDecisionDocument));
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(finalDecisionAndReasonsDocuments));

        when(documentReceiver.receive(correctedDecisionDocument, "", DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF))
            .thenReturn(decisionsAndReasonsDocumentWithMetadata);

        when(documentsAppender
            .append(finalDecisionAndReasonsDocuments, decisionsAndReasonsDocumentsWithMetadata))
            .thenReturn(newUpdateTribunalDecisionDocs);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(documentReceiver).receive(correctedDecisionDocument, "", DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF);
        verify(documentsAppender).append(finalDecisionAndReasonsDocuments, Arrays.asList(decisionsAndReasonsDocumentWithMetadata));

        verify(asylumCase).write(FINAL_DECISION_AND_REASONS_DOCUMENTS, newUpdateTribunalDecisionDocs);
    }

    @Test
    void should_not_append_update_tribunal_decision_document_if_no() {

        final DynamicList dynamicList = new DynamicList(
                new Value("allowed", "Yes, change decision to Allowed"),
                newArrayList()
        );

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(NO));
        when(asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class))
                .thenReturn(Optional.of(correctedDecisionDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase).clear(DECISION_AND_REASON_DOCS_UPLOAD);
        verify(asylumCase).clear(SUMMARISE_TRIBUNAL_DECISION_AND_REASONS_DOCUMENT);
    }
}
