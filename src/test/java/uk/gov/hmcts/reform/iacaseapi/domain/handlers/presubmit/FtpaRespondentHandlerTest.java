package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaRespondentHandlerTest {

    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DateProvider dateProvider;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<DocumentWithDescription>> groundsOfApplicationDocuments;
    @Mock
    private List<IdValue<DocumentWithDescription>> evidenceDocuments;
    @Mock
    private List<IdValue<DocumentWithDescription>> outOfTimeDocuments;
    @Mock
    private DocumentWithMetadata groundsOfApplicationWithMetadata;
    @Mock
    private DocumentWithMetadata evidenceWithMetadata;
    @Mock
    private DocumentWithMetadata outOfTimeWithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingAppellantDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allAppellantDocuments;
    @Mock
    private Appender<FtpaApplications> ftpaAppender;
    @Mock private FtpaApplications existingFtpa;
    @Mock private List allAppendedFtpas;
    @Mock
    private FeatureToggler featureToggler;
    private final List<FtpaApplications> existingFtpas = singletonList(existingFtpa);
    @Captor
    private ArgumentCaptor<List<IdValue<FtpaApplications>>> existingFtpasCaptor;
    @Captor private ArgumentCaptor<FtpaApplications> newFtpaCaptor;

    private FtpaRespondentHandler ftpaRespondentHandler;
    private final LocalDate now = LocalDate.now();

    @BeforeEach
    public void setUp() {
        ftpaRespondentHandler =
            new FtpaRespondentHandler(
                dateProvider,
                documentReceiver,
                documentsAppender,
                ftpaAppender,
                featureToggler
            );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(evidenceDocuments));
    }

    @Test
    void should_append_all_documents_and_set_ll_flags() {

        List<DocumentWithMetadata> ftpaAppellantDocumentsWithMetadata =
            Arrays.asList(
                outOfTimeWithMetadata,
                groundsOfApplicationWithMetadata,
                evidenceWithMetadata
            );

        when(asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(existingAppellantDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION, String.class))
            .thenReturn(Optional.of("Some out of time explanation"));

        when(documentReceiver.tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_RESPONDENT))
            .thenReturn(singletonList(groundsOfApplicationWithMetadata));
        when(documentReceiver.tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_RESPONDENT))
            .thenReturn(singletonList(evidenceWithMetadata));
        when(documentReceiver.tryReceiveAll(outOfTimeDocuments, DocumentTag.FTPA_RESPONDENT))
            .thenReturn(singletonList(outOfTimeWithMetadata));

        when(documentsAppender.append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata))
            .thenReturn(allAppellantDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaRespondentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION, String.class);

        verify(documentReceiver, times(1)).tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_RESPONDENT);
        verify(documentReceiver, times(1)).tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_RESPONDENT);
        verify(documentReceiver, times(1)).tryReceiveAll(outOfTimeDocuments, DocumentTag.FTPA_RESPONDENT);

        verify(documentsAppender, times(1)).append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata);

        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_DOCUMENTS, allAppellantDocuments);
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_APPLICATION_DATE, now.toString());
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_SUBMITTED, YES);

        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(APPEAL_DECISION_AVAILABLE, YES);
    }

    @Test
    void should_not_set_out_of_time_flag() {

        List<DocumentWithMetadata> ftpaAppellantDocumentsWithMetadata =
            Arrays.asList(
                groundsOfApplicationWithMetadata,
                evidenceWithMetadata
            );

        when(asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(existingAppellantDocuments));

        when(documentReceiver.tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_RESPONDENT))
            .thenReturn(singletonList(groundsOfApplicationWithMetadata));
        when(documentReceiver.tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_RESPONDENT))
            .thenReturn(singletonList(evidenceWithMetadata));

        when(documentsAppender.append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata))
            .thenReturn(allAppellantDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaRespondentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_RESPONDENT_DOCUMENTS);

        verify(documentReceiver, times(1)).tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_RESPONDENT);
        verify(documentReceiver, times(1)).tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_RESPONDENT);
        verify(documentReceiver, times(1)).tryReceiveAll(emptyList(), DocumentTag.FTPA_RESPONDENT);

        verify(documentsAppender, times(1)).append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata);

        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_DOCUMENTS, allAppellantDocuments);
        verify(asylumCase, never()).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YES);
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_APPLICATION_DATE, now.toString());
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(APPEAL_DECISION_AVAILABLE, YES);
    }

    @Test
    void should_append_new_ftpa_to_existing_ftpa_list() {

        when(featureToggler.getValue("dlrm-setaside-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(existingFtpas));
        when(ftpaAppender.append(any(FtpaApplications.class), anyList())).thenReturn(allAppendedFtpas);
        when(asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION, String.class))
                .thenReturn(Optional.of("Some out of time explanation"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                ftpaRespondentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(ftpaAppender, times(1)).append(
                newFtpaCaptor.capture(),
                existingFtpasCaptor.capture());

        FtpaApplications capturedFtpa = newFtpaCaptor.getValue();

        assertThat(capturedFtpa.getFtpaApplicant()).isEqualTo(RESPONDENT.toString());
        assertThat(capturedFtpa.getFtpaApplicationDate()).isEqualTo(now.toString());
        assertThat(capturedFtpa.getFtpaGroundsDocuments()).isEqualTo(groundsOfApplicationDocuments);
        assertThat(capturedFtpa.getFtpaEvidenceDocuments()).isEqualTo(evidenceDocuments);
        assertThat(capturedFtpa.getFtpaOutOfTimeExplanation()).isEqualTo("Some out of time explanation");
        assertThat(capturedFtpa.getFtpaOutOfTimeDocuments()).isEqualTo(outOfTimeDocuments);

        assertThat(existingFtpasCaptor.getValue()).isEqualTo(existingFtpas);

        verify(asylumCase, times(1)).write(FTPA_LIST, allAppendedFtpas);
        verify(asylumCase, times(1)).write(IS_FTPA_LIST_VISIBLE, YES);
        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaRespondentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> ftpaRespondentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaRespondentHandler.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_FTPA_RESPONDENT
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

        assertThatThrownBy(() -> ftpaRespondentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
