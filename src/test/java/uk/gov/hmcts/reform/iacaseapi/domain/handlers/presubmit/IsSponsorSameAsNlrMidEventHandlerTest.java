package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SPONSOR_SAME_AS_NLR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;

@ExtendWith(MockitoExtension.class)
class IsSponsorSameAsNlrMidEventHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private NonLegalRepDetails nonLegalRepDetails;

    private IsSponsorSameAsNlrMidEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new IsSponsorSameAsNlrMidEventHandler();
    }

    @Test
    void canHandle_throws_if_null() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            handler.canHandle(null, callback);
        });
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception = assertThrows(NullPointerException.class, () -> {
            handler.canHandle(PreSubmitCallbackStage.MID_EVENT, null);
        });
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void canHandle_returns_true_for_mid_event_edit_appeal_after_submit() {
        when(callback.getPageId()).thenReturn("sponsor");
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void canHandle_returns_false_for_invalid_page_id() {
        when(callback.getPageId()).thenReturn("someInvalidPageId");

        assertFalse(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"MID_EVENT"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_returns_false_for_non_mid_event_stages(PreSubmitCallbackStage callbackStage) {
        assertFalse(handler.canHandle(callbackStage, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPEAL_AFTER_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_returns_false_for_non_edit_appeal_after_submit_events(Event event) {
        when(callback.getPageId()).thenReturn("sponsor");
        when(callback.getEvent()).thenReturn(event);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void handle_throws_if_cannot_handle() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        });
        assertEquals("Cannot handle callback", exception.getMessage());
    }

    @Test
    void does_nothing_if_nlr_details_not_present() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("sponsor");
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase, never()).read(IS_SPONSOR_SAME_AS_NLR, YesOrNo.class);
        verify(asylumCase).read(NLR_DETAILS, NonLegalRepDetails.class);
    }

    @Test
    void nlr_details_setSameAsSponsor_as_null_if_not_same() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("sponsor");
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nonLegalRepDetails));
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).read(IS_SPONSOR_SAME_AS_NLR, YesOrNo.class);
        verify(asylumCase).read(NLR_DETAILS, NonLegalRepDetails.class);
        verify(nonLegalRepDetails).setSameAsSponsor(null);
    }

    @Test
    void nlr_details_setSameAsSponsor_as_yes_if_same() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("sponsor");
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nonLegalRepDetails));
        when(asylumCase.read(IS_SPONSOR_SAME_AS_NLR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).read(IS_SPONSOR_SAME_AS_NLR, YesOrNo.class);
        verify(asylumCase).read(NLR_DETAILS, NonLegalRepDetails.class);
        verify(nonLegalRepDetails).setSameAsSponsor(YesOrNo.YES);
    }
}