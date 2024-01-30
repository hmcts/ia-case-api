package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALL_FTPA_APPELLANT_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALL_FTPA_RESPONDENT_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_NEW_DECISION_OF_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FtpaDisplayService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideFtpaApplicationDecisionHandlerTest {

    @Mock
    List<IdValue<DocumentWithDescription>> maybeFtpaDecisionAndReasonsDocument;
    @Mock
    List<IdValue<DocumentWithDescription>> maybeFtpaDecisionNoticeDocument;
    @Mock
    List<IdValue<DocumentWithMetadata>> existingFtpaDecisionAndReasonsDocuments;
    @Mock
    List<IdValue<DocumentWithMetadata>> allFtpaDecisionDocuments;
    @Mock
    DocumentWithMetadata ftpaAppellantDecisionDocument;
    @Mock
    DocumentWithMetadata ftpaAppellantDecisionNoticeDocument;
    @Mock
    DocumentWithMetadata ftpaRespondentDecisionDocument;
    @Mock
    DocumentWithMetadata ftpaRespondentDecisionNoticeDocument;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private FtpaDisplayService ftpaDisplayService;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private DecideFtpaApplicationDecisionHandler decideFtpaApplicationDecisionHandler;

    @BeforeEach
    public void setUp() {

        decideFtpaApplicationDecisionHandler = new DecideFtpaApplicationDecisionHandler(
            dateProvider,
            documentReceiver,
            documentsAppender,
            ftpaDisplayService,
            featureToggler
        );
    }

    @Test
    void should_append_all_ftpa_appellant_decision_documents() {

        List<DocumentWithMetadata> ftpaAppellantDecisionAndReasonsDocument =
            Arrays.asList(
                ftpaAppellantDecisionDocument,
                ftpaAppellantDecisionNoticeDocument
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT))
            .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
        when(asylumCase.read(FTPA_APPELLANT_NOTICE_DOCUMENT)).thenReturn(Optional.of(maybeFtpaDecisionNoticeDocument));
        when(asylumCase.read(ALL_FTPA_APPELLANT_DECISION_DOCS))
            .thenReturn(Optional.of(existingFtpaDecisionAndReasonsDocuments));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of("granted"));

        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaAppellantDecisionDocument));
        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionNoticeDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaAppellantDecisionNoticeDocument));
        when(documentsAppender.append(existingFtpaDecisionAndReasonsDocuments, ftpaAppellantDecisionAndReasonsDocument))
            .thenReturn(allFtpaDecisionDocuments);

        final LocalDate now = LocalDate.now();
        when(dateProvider.now()).thenReturn(now);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_APPELLANT_DECISION_DOCUMENT);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_NOTICE_DOCUMENT);
        verify(asylumCase, times(1)).read(ALL_FTPA_APPELLANT_DECISION_DOCS);

        verify(documentReceiver, times(1))
            .tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS);
        verify(documentReceiver, times(1))
            .tryReceiveAll(maybeFtpaDecisionNoticeDocument, DocumentTag.FTPA_DECISION_AND_REASONS);

        verify(documentsAppender, times(1))
            .append(existingFtpaDecisionAndReasonsDocuments, ftpaAppellantDecisionAndReasonsDocument);

        verify(asylumCase, times(1)).write(ALL_FTPA_APPELLANT_DECISION_DOCS, allFtpaDecisionDocuments);
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_DECISION_DATE, now.toString());
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DECIDED, YES);

        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_DECIDED, YES);
    }

    @Test
    void should_append_all_ftpa_respondent_decision_documents() {

        List<DocumentWithMetadata> ftpaRespondentDecisionAndReasonsDocument =
            Arrays.asList(
                ftpaRespondentDecisionDocument,
                ftpaRespondentDecisionNoticeDocument
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT))
            .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
        when(asylumCase.read(FTPA_RESPONDENT_NOTICE_DOCUMENT)).thenReturn(Optional.of(maybeFtpaDecisionNoticeDocument));
        when(asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS))
            .thenReturn(Optional.of(existingFtpaDecisionAndReasonsDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("granted"));

        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaRespondentDecisionDocument));
        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionNoticeDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaRespondentDecisionNoticeDocument));
        when(
            documentsAppender.append(existingFtpaDecisionAndReasonsDocuments, ftpaRespondentDecisionAndReasonsDocument))
            .thenReturn(allFtpaDecisionDocuments);

        final LocalDate now = LocalDate.now();
        when(dateProvider.now()).thenReturn(now);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_DECISION_DOCUMENT);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_NOTICE_DOCUMENT);
        verify(asylumCase, times(1)).read(ALL_FTPA_RESPONDENT_DECISION_DOCS);

        verify(documentReceiver, times(1))
            .tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS);
        verify(documentReceiver, times(1))
            .tryReceiveAll(maybeFtpaDecisionNoticeDocument, DocumentTag.FTPA_DECISION_AND_REASONS);

        verify(documentsAppender, times(1))
            .append(existingFtpaDecisionAndReasonsDocuments, ftpaRespondentDecisionAndReasonsDocument);

        verify(asylumCase, times(1)).write(ALL_FTPA_RESPONDENT_DECISION_DOCS, allFtpaDecisionDocuments);
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_DECISION_DATE, now.toString());
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DECIDED, YES);

        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_DECIDED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_SUBMITTED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_DECIDED, YES);
    }

    @Test
    void should_setup_appeal_field_on_remedy_rule_32() {

        List<DocumentWithMetadata> ftpaRespondentDecisionAndReasonsDocument =
            Arrays.asList(
                ftpaRespondentDecisionDocument,
                ftpaRespondentDecisionNoticeDocument
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT))
            .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
        when(asylumCase.read(FTPA_RESPONDENT_NOTICE_DOCUMENT)).thenReturn(Optional.of(maybeFtpaDecisionNoticeDocument));
        when(asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS))
            .thenReturn(Optional.of(existingFtpaDecisionAndReasonsDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("remadeRule32"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, String.class)).thenReturn(Optional.of("Allowed"));

        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaRespondentDecisionDocument));
        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionNoticeDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaRespondentDecisionNoticeDocument));
        when(
            documentsAppender.append(existingFtpaDecisionAndReasonsDocuments, ftpaRespondentDecisionAndReasonsDocument))
            .thenReturn(allFtpaDecisionDocuments);

        final LocalDate now = LocalDate.now();
        when(dateProvider.now()).thenReturn(now);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_RJ_NEW_DECISION_OF_APPEAL, "Allowed");
    }

    @Test
    void should_throw_if_ftpa_applicant_type_missing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertThatThrownBy(
            () -> decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("FtpaApplicantType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_ftpa_decision_outcome_type_missing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        assertThatThrownBy(
            () -> decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("ftpaDecisionOutcomeType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decideFtpaApplicationDecisionHandler.canHandle(callbackStage, callback);

                if (event == Event.DECIDE_FTPA_APPLICATION
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

        assertThatThrownBy(() -> decideFtpaApplicationDecisionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> decideFtpaApplicationDecisionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideFtpaApplicationDecisionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideFtpaApplicationDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
