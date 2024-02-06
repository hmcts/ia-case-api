package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICATION_APPELLANT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICATION_RESPONDENT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_LIST_VISIBLE;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ResidentJudgeFtpaDecisionHandler.DLRM_SETASIDE_FEATURE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.ResidentJudgeFtpaDecisionHandler.FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.FtpaDecisionCheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FtpaDisplayService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ResidentJudgeFtpaDecisionHandlerTest {

    @Mock
    List<IdValue<DocumentWithDescription>> maybeFtpaDecisionAndReasonsDocument;
    @Mock
    Document maybeFtpaApplicationDecisionAndReasonsDocument;
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
    private ResidentJudgeFtpaDecisionHandler residentJudgeFtpaDecisionHandler;
    private final LocalDate now = LocalDate.now();

    private final FtpaDecisionCheckValues ftpaCheckValues =
            new FtpaDecisionCheckValues(List.of("specialReasons"),
                    List.of("countryGuidance"),
                    List.of("specialDifficulty"));

    @BeforeEach
    public void setUp() {

        residentJudgeFtpaDecisionHandler = new ResidentJudgeFtpaDecisionHandler(
            dateProvider,
            documentReceiver,
            documentsAppender,
            ftpaDisplayService,
            featureToggler
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(now);
    }

    @ParameterizedTest
    @CsvSource({
        "RESIDENT_JUDGE_FTPA_DECISION",
        "DECIDE_FTPA_APPLICATION",
    })
    void should_append_all_ftpa_appellant_decision_documents(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));

        if (event.equals(Event.DECIDE_FTPA_APPLICATION)) {
            when(featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false)).thenReturn(true);
            when(asylumCase.read(FTPA_APPLICATION_APPELLANT_DOCUMENT, Document.class))
                .thenReturn(Optional.of(maybeFtpaApplicationDecisionAndReasonsDocument));
            when(documentReceiver.receive(maybeFtpaApplicationDecisionAndReasonsDocument,
                FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION,
                DocumentTag.FTPA_DECISION_AND_REASONS
            )).thenReturn(ftpaAppellantDecisionDocument);
        } else {
            when(asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT))
                .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
            when(documentReceiver.tryReceiveAll(maybeFtpaDecisionAndReasonsDocument,
                DocumentTag.FTPA_DECISION_AND_REASONS))
                .thenReturn(singletonList(ftpaAppellantDecisionDocument));
        }
        List<DocumentWithMetadata> ftpaAppellantDecisionAndReasonsDocument =
            Arrays.asList(
                ftpaAppellantDecisionDocument,
                ftpaAppellantDecisionNoticeDocument
            );
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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (event.equals(Event.DECIDE_FTPA_APPLICATION)) {
            verify(asylumCase, times(1)).read(FTPA_APPLICATION_APPELLANT_DOCUMENT, Document.class);
            verify(documentReceiver, times(1))
                .receive(maybeFtpaApplicationDecisionAndReasonsDocument,
                    FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION, DocumentTag.FTPA_DECISION_AND_REASONS);
        } else {
            verify(asylumCase, times(1)).read(FTPA_APPELLANT_DECISION_DOCUMENT);
            verify(documentReceiver, times(1))
                .tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS);
        }

        verify(asylumCase, times(1)).read(FTPA_APPELLANT_NOTICE_DOCUMENT);
        verify(asylumCase, times(1)).read(ALL_FTPA_APPELLANT_DECISION_DOCS);

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

    @ParameterizedTest
    @CsvSource({
        "RESIDENT_JUDGE_FTPA_DECISION",
        "DECIDE_FTPA_APPLICATION",
    })
    void should_append_all_ftpa_respondent_decision_documents(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        if (event.equals(Event.DECIDE_FTPA_APPLICATION)) {
            when(featureToggler.getValue(DLRM_SETASIDE_FEATURE_FLAG, false)).thenReturn(true);
            when(asylumCase.read(FTPA_APPLICATION_RESPONDENT_DOCUMENT, Document.class))
                .thenReturn(Optional.of(maybeFtpaApplicationDecisionAndReasonsDocument));
            when(documentReceiver.receive(maybeFtpaApplicationDecisionAndReasonsDocument,
                FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION,
                DocumentTag.FTPA_DECISION_AND_REASONS
            )).thenReturn(ftpaRespondentDecisionDocument);
        } else {
            when(asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT))
                .thenReturn(Optional.of(maybeFtpaDecisionAndReasonsDocument));
            when(documentReceiver.tryReceiveAll(maybeFtpaDecisionAndReasonsDocument,
                DocumentTag.FTPA_DECISION_AND_REASONS))
                .thenReturn(singletonList(ftpaRespondentDecisionDocument));
        }

        List<DocumentWithMetadata> ftpaRespondentDecisionAndReasonsDocument =
            Arrays.asList(
                ftpaRespondentDecisionDocument,
                ftpaRespondentDecisionNoticeDocument
            );

        when(asylumCase.read(FTPA_RESPONDENT_NOTICE_DOCUMENT)).thenReturn(Optional.of(maybeFtpaDecisionNoticeDocument));
        when(asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS))
            .thenReturn(Optional.of(existingFtpaDecisionAndReasonsDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("granted"));

        when(documentReceiver.tryReceiveAll(maybeFtpaDecisionNoticeDocument, DocumentTag.FTPA_DECISION_AND_REASONS))
            .thenReturn(singletonList(ftpaRespondentDecisionNoticeDocument));
        when(
            documentsAppender.append(existingFtpaDecisionAndReasonsDocuments, ftpaRespondentDecisionAndReasonsDocument))
            .thenReturn(allFtpaDecisionDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());


        if (event.equals(Event.DECIDE_FTPA_APPLICATION)) {
            verify(asylumCase, times(1)).read(FTPA_APPLICATION_RESPONDENT_DOCUMENT, Document.class);
            verify(documentReceiver, times(1))
                .receive(maybeFtpaApplicationDecisionAndReasonsDocument,
                    FTPA_DECISIONS_AND_REASONS_DOCUMENT_DESCRIPTION, DocumentTag.FTPA_DECISION_AND_REASONS);
        } else {
            verify(asylumCase, times(1)).read(FTPA_RESPONDENT_DECISION_DOCUMENT);
            verify(documentReceiver, times(1))
                .tryReceiveAll(maybeFtpaDecisionAndReasonsDocument, DocumentTag.FTPA_DECISION_AND_REASONS);
        }

        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_NOTICE_DOCUMENT);
        verify(asylumCase, times(1)).read(ALL_FTPA_RESPONDENT_DECISION_DOCS);

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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_RJ_NEW_DECISION_OF_APPEAL, "Allowed");
    }

    @Test
    void should_update_ftpa_application_with_decision_data() {
        when(featureToggler.getValue("dlrm-setaside-feature-flag", false)).thenReturn(true);
        List<IdValue<FtpaApplications>> ftpaApplications = Lists.newArrayList(new IdValue<>("1",
                FtpaApplications.builder()
                        .ftpaApplicant("respondent")
                        .build()));

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_APPLICATION_RESPONDENT_DOCUMENT, Document.class))
                .thenReturn(Optional.of(maybeFtpaApplicationDecisionAndReasonsDocument));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
                .thenReturn(Optional.of("granted"));
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(ftpaApplications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(ftpaDisplayService, times(1)).mapFtpaDecision(anyBoolean(), any(AsylumCase.class), anyString(), any(FtpaApplications.class));
        verify(asylumCase, times(1)).write(FTPA_LIST, ftpaApplications);
        //verify(asylumCase, times(1)).write(IS_FTPA_LIST_VISIBLE, YES);
    }

    @Test
    void should_throw_if_ftpa_applicant_type_missing_in_ftpa_list() {
        when(featureToggler.getValue("dlrm-setaside-feature-flag", false)).thenReturn(true);
        List<IdValue<FtpaApplications>> ftpaApplications = Lists.newArrayList(new IdValue<>("1",
                FtpaApplications.builder()
                        .ftpaApplicant("respondent")
                        .build()));

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPLICATION_APPELLANT_DOCUMENT, Document.class))
                .thenReturn(Optional.of(maybeFtpaApplicationDecisionAndReasonsDocument));
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
                .thenReturn(Optional.of("granted"));
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(ftpaApplications));

        assertThatThrownBy(
                () -> residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("appellant application is not present in FTPA list")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_ftpa_applicant_type_missing() {

        assertThatThrownBy(
            () -> residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("FtpaApplicantType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_ftpa_decision_outcome_type_missing() {

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        assertThatThrownBy(
            () -> residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("ftpaDecisionOutcomeType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = residentJudgeFtpaDecisionHandler.canHandle(callbackStage, callback);

                if ((event == Event.RESIDENT_JUDGE_FTPA_DECISION ||
                    event == Event.DECIDE_FTPA_APPLICATION)
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

        assertThatThrownBy(() -> residentJudgeFtpaDecisionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> residentJudgeFtpaDecisionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> residentJudgeFtpaDecisionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> residentJudgeFtpaDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
