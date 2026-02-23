package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.LatestQuery;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RaiseQueryCallbackHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    private RaiseQueryCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RaiseQueryCallbackHandler(userDetails);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_handle_correct_event_and_stage() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_wrong_stage() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void should_not_handle_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_write_latest_query_id_when_queries_exist() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            List<IdValue<CaseQueriesCollection>> queries = asList(
                    new IdValue<>("1", mock(CaseQueriesCollection.class)),
                    new IdValue<>("3", mock(CaseQueriesCollection.class)),
                    new IdValue<>("2", mock(CaseQueriesCollection.class))
            );

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES))
                    .thenReturn(Optional.of(queries));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            ArgumentCaptor<LatestQuery> captor = ArgumentCaptor.forClass(LatestQuery.class);
            verify(asylumCase).write(eq(QM_LATEST_QUERY), captor.capture());

            LatestQuery written = captor.getValue();
            assertEquals("3", written.getQueryId());
            assertEquals(YesOrNo.NO, written.getIsHearingRelated());
        }
    }

    @Test
    void should_default_to_one_when_no_queries_exist() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES))
                    .thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            ArgumentCaptor<LatestQuery> captor = ArgumentCaptor.forClass(LatestQuery.class);
            verify(asylumCase).write(eq(QM_LATEST_QUERY), captor.capture());

            assertEquals("1", captor.getValue().getQueryId());
        }
    }

    @Test
    void should_do_nothing_when_target_collection_is_null() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(false);
            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney(asylumCase))
                    .thenReturn(false);
            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase(asylumCase))
                    .thenReturn(false);

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase, never()).write(any(), any());
        }
    }

    @Test
    void should_handle_single_query_correctly() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            List<IdValue<CaseQueriesCollection>> queries = List.of(
                    new IdValue<>("7", mock(CaseQueriesCollection.class))
            );

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES))
                    .thenReturn(Optional.of(queries));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            ArgumentCaptor<LatestQuery> captor = ArgumentCaptor.forClass(LatestQuery.class);
            verify(asylumCase).write(eq(QM_LATEST_QUERY), captor.capture());

            assertEquals("7", captor.getValue().getQueryId());
            assertEquals(YesOrNo.NO, captor.getValue().getIsHearingRelated());
        }
    }

    @Test
    void should_handle_non_numeric_ids_lexicographically() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            List<IdValue<CaseQueriesCollection>> queries = asList(
                    new IdValue<>("abc", mock(CaseQueriesCollection.class)),
                    new IdValue<>("xyz", mock(CaseQueriesCollection.class)),
                    new IdValue<>("def", mock(CaseQueriesCollection.class))
            );

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES))
                    .thenReturn(Optional.of(queries));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            ArgumentCaptor<LatestQuery> captor = ArgumentCaptor.forClass(LatestQuery.class);
            verify(asylumCase).write(eq(QM_LATEST_QUERY), captor.capture());

            assertEquals("xyz", captor.getValue().getQueryId());
        }
    }
}