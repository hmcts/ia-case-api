package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_LEGAL_REPRESENTATIVE_QUERIES;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;

class RaiseQueryCallbackPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    private RaiseQueryCallbackPreparer handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RaiseQueryCallbackPreparer(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_handle_about_to_start_and_query_event() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void should_not_handle_other_stage() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_initialize_empty_collection_if_none_exists() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES))
                    .thenReturn(Optional.empty());

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase).write(QM_LEGAL_REPRESENTATIVE_QUERIES, emptyList());
        }
    }

    @Test
    void should_not_overwrite_existing_collection() {
        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RAISE_QUERY);

        try (MockedStatic<uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils> utils =
                     mockStatic(uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.class)) {

            utils.when(() -> uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase))
                    .thenReturn(true);

            List<IdValue<CaseQueriesCollection>> existing = asList(
                    new IdValue<>("1", new CaseQueriesCollection())
            );

            when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES))
                    .thenReturn(Optional.of(existing));

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

            verify(asylumCase, never()).write(eq(QM_LEGAL_REPRESENTATIVE_QUERIES), any());
        }
    }
}
