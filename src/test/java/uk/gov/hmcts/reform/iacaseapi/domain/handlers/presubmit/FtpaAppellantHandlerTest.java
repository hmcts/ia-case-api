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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_DECISION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_APPLICATION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_GROUNDS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_GROUNDS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
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
class FtpaAppellantHandlerTest {

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

    private FtpaAppellantHandler ftpaAppellantHandler;
    private final LocalDate now = LocalDate.now();

    @BeforeEach
    public void setUp() {
        ftpaAppellantHandler =
            new FtpaAppellantHandler(
                dateProvider,
                documentReceiver,
                documentsAppender,
                ftpaAppender,
                featureToggler
            );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        when(asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(evidenceDocuments));
    }

    @Test
    void should_append_all_documents_and_set_ll_flags() {

        List<DocumentWithMetadata> ftpaAppellantDocumentsWithMetadata =
            Arrays.asList(
                outOfTimeWithMetadata,
                groundsOfApplicationWithMetadata,
                evidenceWithMetadata
            );

        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(existingAppellantDocuments));
        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class))
            .thenReturn(Optional.of("Some out of time explanation"));
        when(asylumCase.read(FTPA_APPELLANT_GROUNDS, String.class))
            .thenReturn(Optional.of("Some explanation for FTPA Appellant grounds"));

        when(documentReceiver.tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(groundsOfApplicationWithMetadata));
        when(documentReceiver.tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(evidenceWithMetadata));
        when(documentReceiver.tryReceiveAll(outOfTimeDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(outOfTimeWithMetadata));

        when(documentsAppender.append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata))
            .thenReturn(allAppellantDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_GROUNDS, String.class);

        verify(documentReceiver, times(1)).tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_APPELLANT);
        verify(documentReceiver, times(1)).tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_APPELLANT);
        verify(documentReceiver, times(1)).tryReceiveAll(outOfTimeDocuments, DocumentTag.FTPA_APPELLANT);

        verify(documentsAppender, times(1)).append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata);

        verify(asylumCase, times(1)).write(FTPA_APPELLANT_DOCUMENTS, allAppellantDocuments);
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_APPLICATION_DATE, now.toString());
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_SUBMITTED, YES);

        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_DECIDED, NO);
        verify(asylumCase, times(1)).write(APPEAL_DECISION_AVAILABLE, YES);
    }

    @Test
    void should_not_set_out_of_time_flag() {

        List<DocumentWithMetadata> ftpaAppellantDocumentsWithMetadata =
            Arrays.asList(
                groundsOfApplicationWithMetadata,
                evidenceWithMetadata
            );

        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(existingAppellantDocuments));

        when(documentReceiver.tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(groundsOfApplicationWithMetadata));
        when(documentReceiver.tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(evidenceWithMetadata));

        when(documentsAppender.append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata))
            .thenReturn(allAppellantDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_DOCUMENTS);

        verify(documentReceiver, times(1)).tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_APPELLANT);
        verify(documentReceiver, times(1)).tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_APPELLANT);
        verify(documentReceiver, times(1)).tryReceiveAll(emptyList(), DocumentTag.FTPA_APPELLANT);

        verify(documentsAppender, times(1)).append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata);

        verify(asylumCase, times(1)).write(FTPA_APPELLANT_DOCUMENTS, allAppellantDocuments);
        verify(asylumCase, never()).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YES);
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_APPLICATION_DATE, now.toString());
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_SUBMITTED, YES);
        verify(asylumCase, times(1)).write(APPEAL_DECISION_AVAILABLE, YES);
    }

    @Test
    void should_append_new_ftpa_to_existing_ftpa_list() {

        when(featureToggler.getValue("dlrm-setaside-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(existingFtpas));
        when(ftpaAppender.append(any(FtpaApplications.class), anyList())).thenReturn(allAppendedFtpas);
        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class))
                .thenReturn(Optional.of("Some out of time explanation"));
        when(asylumCase.read(FTPA_APPELLANT_GROUNDS, String.class))
                .thenReturn(Optional.of("Some explanation for FTPA Appellant grounds"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(ftpaAppender, times(1)).append(
                newFtpaCaptor.capture(),
                existingFtpasCaptor.capture());

        FtpaApplications capturedFtpa = newFtpaCaptor.getValue();

        assertThat(capturedFtpa.getFtpaApplicant()).isEqualTo(APPELLANT.toString());
        assertThat(capturedFtpa.getFtpaApplicationDate()).isEqualTo(now.toString());
        assertThat(capturedFtpa.getFtpaGroundsDocuments()).isEqualTo(groundsOfApplicationDocuments);
        assertThat(capturedFtpa.getFtpaEvidenceDocuments()).isEqualTo(evidenceDocuments);
        assertThat(capturedFtpa.getFtpaOutOfTimeExplanation()).isEqualTo("Some out of time explanation");
        assertThat(capturedFtpa.getFtpaAppellantGroundsText())
                .isEqualTo("Some explanation for FTPA Appellant grounds");
        assertThat(capturedFtpa.getFtpaOutOfTimeDocuments()).isEqualTo(outOfTimeDocuments);

        assertThat(existingFtpasCaptor.getValue()).isEqualTo(existingFtpas);

        verify(asylumCase, times(1)).write(FTPA_LIST, allAppendedFtpas);
        verify(asylumCase, times(1)).write(IS_FTPA_LIST_VISIBLE, YES);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, NO);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaAppellantHandler.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_FTPA_APPELLANT
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

        assertThatThrownBy(() -> ftpaAppellantHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
