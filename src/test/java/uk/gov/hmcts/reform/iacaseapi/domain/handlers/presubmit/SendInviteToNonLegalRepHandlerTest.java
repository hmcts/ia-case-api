package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_INVITE_NLR_TO_IDAM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_INVITE_TO_NON_LEGAL_REP;

import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SendInviteToNonLegalRepHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private IdamService idamService;
    @Mock
    private User user;

    private SendInviteToNonLegalRepHandler sendInviteToNonLegalRepHandler;

    @BeforeEach
    public void setUp() {
        sendInviteToNonLegalRepHandler = new SendInviteToNonLegalRepHandler(idamService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_do_nothing_if_no_nlr_email() {
        when(callback.getEvent()).thenReturn(SEND_INVITE_TO_NON_LEGAL_REP);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        sendInviteToNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, never()).write(eq(SHOULD_INVITE_NLR_TO_IDAM), any(YesOrNo.class));
    }

    @Test
    void should_do_nothing_if_user_exists_with_nlr_email() {
        when(callback.getEvent()).thenReturn(SEND_INVITE_TO_NON_LEGAL_REP);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(
            NonLegalRepDetails.builder().emailAddress("nlrEmail@test.com").build()));
        when(idamService.getUserFromEmailV1("nlrEmail@test.com")).thenReturn(user);
        sendInviteToNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, never()).write(eq(SHOULD_INVITE_NLR_TO_IDAM), any(YesOrNo.class));
    }

    @Test
    void should_write_shouldInviteNlrToIdam_yes_if_no_user_exists_with_nlr_email() {
        when(callback.getEvent()).thenReturn(SEND_INVITE_TO_NON_LEGAL_REP);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(
            NonLegalRepDetails.builder().emailAddress("nlrEmail@test.com").build()));
        when(idamService.getUserFromEmailV1("nlrEmail@test.com")).thenReturn(null);
        sendInviteToNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase).write(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.YES);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> sendInviteToNonLegalRepHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SEND_INVITE_TO_NON_LEGAL_REP", "SUBMIT_APPEAL"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(sendInviteToNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"SEND_INVITE_TO_NON_LEGAL_REP", "SUBMIT_APPEAL"})
    void it_cannot_handle_incorrect_callback_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(sendInviteToNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_SUBMIT"})
    void it_cannot_handle_incorrect_callback_stage(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(Event.SEND_INVITE_TO_NON_LEGAL_REP);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(sendInviteToNonLegalRepHandler.canHandle(callbackStage, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SEND_INVITE_TO_NON_LEGAL_REP", "SUBMIT_APPEAL"})
    void it_cannot_handle_for_non_aip(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(sendInviteToNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        NullPointerException nullCallbackStage = assertThrows(NullPointerException.class,
            () -> sendInviteToNonLegalRepHandler.canHandle(null, callback));
        NullPointerException nullCallback = assertThrows(NullPointerException.class,
            () -> sendInviteToNonLegalRepHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));

        assertEquals("callbackStage must not be null", nullCallbackStage.getMessage());
        assertEquals("callback must not be null", nullCallback.getMessage());
    }
}
