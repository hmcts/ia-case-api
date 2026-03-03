package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.LatestQuery;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RaiseQueryCallbackHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    @Captor private ArgumentCaptor<List<IdValue<LatestQuery>>> latestQueryCaptor;

    private RaiseQueryCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RaiseQueryCallbackHandler(userDetails);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);
    }

    @Test
    void should_handle_correct_event_and_stage() {
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_wrong_stage() {
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void should_not_handle_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_throw_exception_when_target_collection_is_null() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);

            Exception ex = assertThrows(IllegalStateException.class,
                    () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

            assertEquals("Unable to determine query collection for this asylum case", ex.getMessage());
        }
    }

    @Test
    void should_write_latest_query_with_correct_isHearingRelated() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);

            CaseMessage message = new CaseMessage();
            message.setId("msg1");
            message.setIsHearingRelated(YesOrNo.YES);
            message.setCreatedOn(LocalDateTime.now());

            List<IdValue<CaseMessage>> caseMessages = List.of(new IdValue<>("msg1", message));
            CaseQueriesCollection collection = CaseQueriesCollection.builder().caseMessages(caseMessages).build();

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.of(collection));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase).write(eq(QM_LATEST_QUERY), latestQueryCaptor.capture());

            List<IdValue<LatestQuery>> captured = latestQueryCaptor.getValue();
            assertEquals(1, captured.size());

            IdValue<LatestQuery> latest = captured.get(0);
            assertEquals("msg1", latest.getId());
            assertEquals("msg1", latest.getValue().getQueryId());
            assertEquals(YesOrNo.YES, latest.getValue().getIsHearingRelated());
        }
    }

    @Test
    void should_preserve_existing_latest_queries_and_append_new_one() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);

            CaseMessage newMessage = new CaseMessage();
            newMessage.setId("msg2");
            newMessage.setIsHearingRelated(YesOrNo.NO);
            newMessage.setCreatedOn(LocalDateTime.now());

            List<IdValue<CaseMessage>> caseMessages = List.of(new IdValue<>("msg2", newMessage));
            CaseQueriesCollection collection = CaseQueriesCollection.builder().caseMessages(caseMessages).build();

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.of(collection));

            LatestQuery existing = LatestQuery.builder()
                    .queryId("msg0")
                    .isHearingRelated(YesOrNo.YES)
                    .build();
            when(asylumCase.read(QM_LATEST_QUERY)).thenReturn(Optional.of(List.of(new IdValue<>("msg0", existing))));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase).write(eq(QM_LATEST_QUERY), latestQueryCaptor.capture());

            List<IdValue<LatestQuery>> captured = latestQueryCaptor.getValue();
            assertEquals(2, captured.size());
            assertTrue(captured.stream().anyMatch(q -> q.getId().equals("msg0")));
            assertTrue(captured.stream().anyMatch(q -> q.getId().equals("msg2")));
        }
    }

    @Test
    void should_not_write_latest_query_when_collection_is_empty() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);

            CaseQueriesCollection emptyCollection = CaseQueriesCollection.builder().caseMessages(List.of()).build();
            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.of(emptyCollection));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase, never()).write(eq(QM_LATEST_QUERY), any());
        }
    }
}