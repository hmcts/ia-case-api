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

    @Captor
    private ArgumentCaptor<List<LatestQuery>> latestQueryCaptor;

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
        assertTrue(handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        ));
    }

    @Test
    void should_not_handle_wrong_stage() {
        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
        ));
    }

    @Test
    void should_not_handle_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        ));
    }

    @Test
    void should_default_to_one_when_no_queries_exist() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase).write(eq(QM_LATEST_QUERY), latestQueryCaptor.capture());

            List<LatestQuery> captured = latestQueryCaptor.getValue();
            assertEquals(1, captured.size());
            assertEquals("1", captured.get(0).getQueryId());
            assertEquals(YesOrNo.NO, captured.get(0).getIsHearingRelated());
        }
    }

    @Test
    void should_throw_exception_when_target_collection_is_null() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);

            Exception ex = assertThrows(
                    IllegalStateException.class,
                    () -> handler.handle(
                            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                            callback
                    )
            );

            assertEquals(
                    "Unable to determine query collection for this asylum case",
                    ex.getMessage()
            );
        }
    }

    @Test
    void should_set_is_hearing_related_to_no() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            CaseMessage message = new CaseMessage();
            message.setId("7");

            List<IdValue<CaseMessage>> caseMessages =
                    List.of(new IdValue<>("7", message));

            CaseQueriesCollection collection =
                    CaseQueriesCollection.builder()
                            .caseMessages(caseMessages)
                            .build();

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.of(collection));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase).write(eq(QM_LATEST_QUERY), latestQueryCaptor.capture());

            LatestQuery captured = latestQueryCaptor.getValue().get(0);

            assertEquals("7", captured.getQueryId());
            assertEquals(YesOrNo.NO, captured.getIsHearingRelated());
        }
    }

    @Test
    void should_select_lexicographically_largest_query_id() {
        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            CaseMessage msgAbc = new CaseMessage();
            msgAbc.setId("abc");

            CaseMessage msgXyz = new CaseMessage();
            msgXyz.setId("xyz");

            CaseMessage msgDef = new CaseMessage();
            msgDef.setId("def");

            List<IdValue<CaseMessage>> caseMessages = asList(
                    new IdValue<>("1", msgAbc),
                    new IdValue<>("2", msgXyz),
                    new IdValue<>("3", msgDef)
            );

            CaseQueriesCollection collection =
                    CaseQueriesCollection.builder()
                            .caseMessages(caseMessages)
                            .build();

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES, CaseQueriesCollection.class))
                    .thenReturn(Optional.of(collection));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase).write(eq(QM_LATEST_QUERY), latestQueryCaptor.capture());

            LatestQuery captured = latestQueryCaptor.getValue().get(0);

            assertEquals("xyz", captured.getQueryId());
        }
    }
}