package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ChangeTribunalCentreHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private ChangeTribunalCentreHandler changeTribunalCentreHandler;

    @BeforeEach
    public void setUp() {
        changeTribunalCentreHandler = new ChangeTribunalCentreHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_TRIBUNAL_CENTRE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @ParameterizedTest
    @EnumSource(value = HearingCentre.class)
    void should_write_hearing_centre_if_valid_designated_tribunal_centre(HearingCentre hearingCentre) {
        when(bailCase.read(DESIGNATED_TRIBUNAL_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));
        PreSubmitCallbackResponse<BailCase> response = changeTribunalCentreHandler.handle(ABOUT_TO_SUBMIT, callback);
        verify(bailCase).write(HEARING_CENTRE, Optional.of(hearingCentre));
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_return_response_error_if_no_designated_tribunal_centre() {
        PreSubmitCallbackResponse<BailCase> response = changeTribunalCentreHandler.handle(ABOUT_TO_SUBMIT, callback);
        verify(bailCase, never()).write(eq(HEARING_CENTRE), any(HearingCentre.class));
        Set<String> errors = Set.of("designatedTribunalCentre cannot be empty");
        assertEquals(response.getErrors(), errors);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void handler_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            boolean canHandle = changeTribunalCentreHandler.canHandle(callbackStage, callback);
            if (callbackStage == ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.CHANGE_TRIBUNAL_CENTRE)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        Assertions.assertThatThrownBy(() -> changeTribunalCentreHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        Assertions.assertThatThrownBy(() -> changeTribunalCentreHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> changeTribunalCentreHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeTribunalCentreHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeTribunalCentreHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeTribunalCentreHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
