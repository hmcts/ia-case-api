package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP_JOINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SPONSOR_SAME_AS_NLR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOIN_APPEAL_PIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RemoveNonLegalRepHandlerTest {

    @Mock
    private CcdDataService ccdDataService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private RemoveNonLegalRepHandler handler;

    private final long caseId = 12345L;

    @BeforeEach
    void setUp() {
        handler = new RemoveNonLegalRepHandler(ccdDataService);
        when(callback.getEvent()).thenReturn(Event.REMOVE_NON_LEGAL_REP);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
    }

    @Test
    void should_revoke_access_and_clear_details_if_nlr() {
        String nlrIdamId = "nlr-user-123";
        NonLegalRepDetails nlrDetails = NonLegalRepDetails.builder()
            .idamId(nlrIdamId)
            .build();
        when(asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class))
            .thenReturn(Optional.of(nlrDetails));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(ccdDataService).revokeUserAccessToCase(caseId, nlrIdamId);
        verify(asylumCase).write(HAS_NON_LEGAL_REP, NO);
        verify(asylumCase).clear(NLR_DETAILS);
        verify(asylumCase).clear(JOIN_APPEAL_PIN);
        verify(asylumCase).clear(IS_SPONSOR_SAME_AS_NLR);
        verify(asylumCase).clear(HAS_NON_LEGAL_REP_JOINED);
    }

    @Test
    void should_do_nothing_if_no_nlr() {
        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verifyNoInteractions(ccdDataService);
        verify(asylumCase, never()).write(any(), any());
        verify(asylumCase, never()).clear();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REMOVE_NON_LEGAL_REP"}, mode = EnumSource.Mode.EXCLUDE)
    void cannot_handle_for_wrong_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void cannot_handle_for_wrong_stage(PreSubmitCallbackStage stage) {
        assertFalse(handler.canHandle(stage, callback));
    }

    @Test
    void can_handle_only_for_correct_event_and_stage() {
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_if_cannot_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.REMOVE_NON_LEGAL_REP);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }
}