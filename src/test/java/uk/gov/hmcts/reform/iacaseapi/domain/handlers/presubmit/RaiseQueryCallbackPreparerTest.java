package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RaiseQueryCallbackPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;
    @Captor private ArgumentCaptor<CaseQueriesCollection> collectionCaptor;

    private RaiseQueryCallbackPreparer handler;

    @BeforeEach
    void setUp() {
        handler = new RaiseQueryCallbackPreparer(userDetails);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);
    }

    @Test
    void canHandle_should_return_true_for_correct_stage_and_event() {
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void should_initialize_legal_rep_queries_with_empty_collection_if_absent() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(eq(QM_LEGAL_REPRESENTATIVE_QUERIES), collectionCaptor.capture());
            assertEquals(emptyList(), collectionCaptor.getValue().getCaseMessages());
        }
    }

    @Test
    void should_initialize_aip_queries_with_empty_collection_if_absent() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(true);

            when(asylumCase.read(QM_AIP_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(eq(QM_AIP_QUERIES), collectionCaptor.capture());
            assertEquals(emptyList(), collectionCaptor.getValue().getCaseMessages());
        }
    }

    @Test
    void should_initialize_admin_queries_with_empty_collection_if_absent() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(true);

            when(asylumCase.read(QM_ADMIN_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(eq(QM_ADMIN_QUERIES), collectionCaptor.capture());
            assertEquals(emptyList(), collectionCaptor.getValue().getCaseMessages());
        }
    }

    @Test
    void should_preserve_existing_collection_when_already_present() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);

            CaseQueriesCollection existing = CaseQueriesCollection.builder()
                    .caseMessages(emptyList())
                    .build();

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.of(existing));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(eq(QM_LEGAL_REPRESENTATIVE_QUERIES), collectionCaptor.capture());
            assertSame(existing, collectionCaptor.getValue());
        }
    }

    @Test
    void should_throw_when_journey_type_cannot_be_determined() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);

            Exception ex = assertThrows(IllegalStateException.class, () ->
                    handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback)
            );
            assertEquals("Unable to determine query collection for this asylum case", ex.getMessage());
        }
    }
}