package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class FtpaAppellantHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private DateProvider dateProvider;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private List<IdValue<DocumentWithDescription>> groundsOfApplicationDocuments;
    @Mock private List<IdValue<DocumentWithDescription>> evidenceDocuments;
    @Mock private List<IdValue<DocumentWithDescription>> outOfTimeDocuments;
    @Mock private DocumentWithMetadata groundsOfApplicationWithMetadata;
    @Mock private DocumentWithMetadata evidenceWithMetadata;
    @Mock private DocumentWithMetadata outOfTimeWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingAppellantDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allAppellantDocuments;

    FtpaAppellantHandler ftpaAppellantHandler;

    @BeforeEach
    void setUp() {

        ftpaAppellantHandler =
            new FtpaAppellantHandler(
                dateProvider,
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_append_all_documents_and_set_ll_flags() {

        List<DocumentWithMetadata> ftpaAppellantDocumentsWithMetadata =
            Arrays.asList(
                outOfTimeWithMetadata,
                groundsOfApplicationWithMetadata,
                evidenceWithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        when(asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(evidenceDocuments));
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(existingAppellantDocuments));
        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.of("Some out of time explanation"));

        when(documentReceiver.tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(groundsOfApplicationWithMetadata));
        when(documentReceiver.tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(evidenceWithMetadata));
        when(documentReceiver.tryReceiveAll(outOfTimeDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(outOfTimeWithMetadata));

        when(documentsAppender.append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata))
            .thenReturn(allAppellantDocuments);

        final LocalDate now = LocalDate.now();
        when(dateProvider.now()).thenReturn(now);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_DOCUMENTS);
        verify(asylumCase, times(1)).read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class);

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

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        when(asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(evidenceDocuments));
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(existingAppellantDocuments));

        when(documentReceiver.tryReceiveAll(groundsOfApplicationDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(groundsOfApplicationWithMetadata));
        when(documentReceiver.tryReceiveAll(evidenceDocuments, DocumentTag.FTPA_APPELLANT))
            .thenReturn(singletonList(evidenceWithMetadata));

        when(documentsAppender.append(existingAppellantDocuments, ftpaAppellantDocumentsWithMetadata))
            .thenReturn(allAppellantDocuments);

        final LocalDate now = LocalDate.now();
        when(dateProvider.now()).thenReturn(now);
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
