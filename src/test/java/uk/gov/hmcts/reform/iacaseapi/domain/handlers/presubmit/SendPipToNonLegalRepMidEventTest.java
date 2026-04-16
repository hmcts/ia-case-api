package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_PIP_TO_NON_LEGAL_REP;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SendPipToNonLegalRepMidEventTest {

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

    private SendPipToNonLegalRepMidEvent sendPipToNonLegalRepMidEvent;

    @BeforeEach
    public void setUp() {
        sendPipToNonLegalRepMidEvent = new SendPipToNonLegalRepMidEvent(idamService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_throw_if_no_nlr_email() {
        when(callback.getEvent()).thenReturn(SEND_PIP_TO_NON_LEGAL_REP);
        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> sendPipToNonLegalRepMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback));

        assertEquals("NLR email is not present", exception.getMessage());
    }

    @Test
    void should_return_error_if_user_not_found() {
        when(callback.getEvent()).thenReturn(SEND_PIP_TO_NON_LEGAL_REP);
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(
            NonLegalRepDetails.builder().emailAddress("nlrEmail@test.com").build()));
        when(idamService.getUserFromEmailV1(anyString())).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> response =
            sendPipToNonLegalRepMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals("User with email nlrEmail@test.com has not signed up to HMCTS services. " +
                "Please invite them to sign up via the \"Send invite to non legal rep\" event before sending the PIP.",
            response.getErrors().iterator().next());
    }

    @Test
    void should_return_if_user_exists() {
        when(callback.getEvent()).thenReturn(SEND_PIP_TO_NON_LEGAL_REP);
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(
            NonLegalRepDetails.builder().emailAddress("nlrEmail@test.com").build()));
        when(idamService.getUserFromEmailV1(anyString())).thenReturn(user);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            sendPipToNonLegalRepMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().isEmpty());
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendPipToNonLegalRepMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.SEND_PIP_TO_NON_LEGAL_REP);
        assertTrue(sendPipToNonLegalRepMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"SEND_PIP_TO_NON_LEGAL_REP"})
    void it_cannot_handle_incorrect_callback_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(sendPipToNonLegalRepMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"MID_EVENT"})
    void it_cannot_handle_incorrect_callback_stage(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(Event.SEND_INVITE_TO_NON_LEGAL_REP);
        assertFalse(sendPipToNonLegalRepMidEvent.canHandle(callbackStage, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        NullPointerException nullCallbackStage = assertThrows(NullPointerException.class,
            () -> sendPipToNonLegalRepMidEvent.canHandle(null, callback));
        NullPointerException nullCallback = assertThrows(NullPointerException.class,
            () -> sendPipToNonLegalRepMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, null));

        assertEquals("callbackStage must not be null", nullCallbackStage.getMessage());
        assertEquals("callback must not be null", nullCallback.getMessage());
    }
}
