package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP_JOINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SPONSOR_SAME_AS_NLR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOIN_APPEAL_PIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HasNonLegalRepHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private NonLegalRepDetails nlrDetails;

    private MockedStatic<HandlerUtils> handlerUtils;

    private HasNonLegalRepHandler hasNonLegalRepHandler;

    @BeforeEach
    public void setUp() {
        hasNonLegalRepHandler = new HasNonLegalRepHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_do_nothing_if_has_nlr_is_yes() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(HAS_NON_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nlrDetails));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hasNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).clear(NLR_DETAILS);
        verify(asylumCase, never()).clear(JOIN_APPEAL_PIN);
        verify(asylumCase, never()).clear(IS_SPONSOR_SAME_AS_NLR);
        verify(asylumCase, never()).clear(HAS_NON_LEGAL_REP_JOINED);
    }

    @Test
    void should_clear_nlr_details_if_no_has_nlr_set() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(HAS_NON_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nlrDetails));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hasNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).clear(NLR_DETAILS);
        verify(asylumCase, times(1)).clear(JOIN_APPEAL_PIN);
        verify(asylumCase, times(1)).clear(IS_SPONSOR_SAME_AS_NLR);
        verify(asylumCase, times(1)).clear(HAS_NON_LEGAL_REP_JOINED);
    }

    @Test
    void should_clear_nlr_details_if_has_nlr_is_no() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(HAS_NON_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nlrDetails));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hasNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).clear(NLR_DETAILS);
        verify(asylumCase, times(1)).clear(JOIN_APPEAL_PIN);
        verify(asylumCase, times(1)).clear(IS_SPONSOR_SAME_AS_NLR);
        verify(asylumCase, times(1)).clear(HAS_NON_LEGAL_REP_JOINED);
    }


    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_setSponsorDetailsFromNlrIfSame_for_any_has_nlr(YesOrNo hasNlr) {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(HAS_NON_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(hasNlr));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nlrDetails));
        handlerUtils = mockStatic(HandlerUtils.class);
        handlerUtils.when(() -> HandlerUtils.isAipJourney(asylumCase)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hasNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        handlerUtils.verify(
            () -> HandlerUtils.setSponsorDetailsFromNlrIfSame(asylumCase),
            times(1)
        );
        handlerUtils.close();
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hasNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "SEND_PIP_TO_NON_LEGAL_REP", "NLR_DETAILS_UPDATED"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(hasNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"SUBMIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "SEND_PIP_TO_NON_LEGAL_REP", "NLR_DETAILS_UPDATED"})
    void it_cannot_handle_incorrect_callback_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(hasNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_SUBMIT"})
    void it_cannot_handle_incorrect_callback_stage(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(hasNonLegalRepHandler.canHandle(callbackStage, callback));
    }

    @Test
    void it_cannot_handle_rep_journey() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        assertFalse(hasNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        NullPointerException nullCallbackStage = assertThrows(NullPointerException.class,
            () -> hasNonLegalRepHandler.canHandle(null, callback));
        NullPointerException nullCallback = assertThrows(NullPointerException.class,
            () -> hasNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));

        assertEquals("callbackStage must not be null", nullCallbackStage.getMessage());
        assertEquals("callback must not be null", nullCallback.getMessage());
    }
}
