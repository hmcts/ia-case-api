package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RaiseQueryCallbackPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    private RaiseQueryCallbackPreparer handler;

    @BeforeEach
    void setUp() {
        handler = new RaiseQueryCallbackPreparer(userDetails);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void canHandle_should_return_true_for_correct_stage_and_event() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE); // any other event
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void should_initialize_legal_rep_queries_if_empty() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES)).thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(QM_LEGAL_REPRESENTATIVE_QUERIES, emptyList());
        }
    }

    @Test
    void should_initialize_aip_queries_if_empty() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(true);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);

            when(asylumCase.read(QM_AIP_QUERIES)).thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(QM_AIP_QUERIES, emptyList());
        }
    }

    @Test
    void should_initialize_admin_queries_if_empty() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(true);

            when(asylumCase.read(QM_ADMIN_QUERIES)).thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(QM_ADMIN_QUERIES, emptyList());
        }
    }

    @Test
    void should_not_overwrite_existing_legal_rep_collection() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<HandlerUtils> utils = mockStatic(HandlerUtils.class)) {
            utils.when(() -> HandlerUtils.isLegalRepJourney(asylumCase)).thenReturn(true);

            List<IdValue<CaseQueriesCollection>> existing = asList(new IdValue<>("1", new CaseQueriesCollection()));
            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES)).thenReturn(Optional.of(existing));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase, never()).write(eq(QM_LEGAL_REPRESENTATIVE_QUERIES), any());
        }
    }

}
