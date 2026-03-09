package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_ADMIN_QUERIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_LEGAL_REPRESENTATIVE_QUERIES;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
class RespondQueryCallbackHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;
    @Mock private UserDetailsHelper userDetailsHelper;

    private RespondQueryCallbackHandler handler;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        handler = new RespondQueryCallbackHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_handle_callback() {

        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RESPOND_QUERY);
        when(asylumCase.read(QM_ADMIN_QUERIES)).thenReturn(Optional.empty());
        when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
    }

    @Test
    void should_clear_old_queries_if_present() {

        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RESPOND_QUERY);
        when(asylumCase.read(QM_ADMIN_QUERIES)).thenReturn(Optional.of(new Object()));
        when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES)).thenReturn(Optional.empty());

        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(QM_LEGAL_REPRESENTATIVE_QUERIES, null);
        verify(asylumCase).write(QM_ADMIN_QUERIES, null);
    }

    @Test
    void hasOldQueries_should_return_true_if_admin_queries_exist() {

        when(asylumCase.read(QM_ADMIN_QUERIES)).thenReturn(Optional.of(new Object()));
        when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES)).thenReturn(Optional.empty());

        boolean result = RespondQueryCallbackHandler.hasOldQueries(callback);

        assertTrue(result);
    }

    @Test
    void hasOldQueries_should_return_false_if_no_queries() {

        when(asylumCase.read(QM_ADMIN_QUERIES)).thenReturn(Optional.empty());
        when(asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES)).thenReturn(Optional.empty());

        boolean result = RespondQueryCallbackHandler.hasOldQueries(callback);

        assertFalse(result);
    }
}