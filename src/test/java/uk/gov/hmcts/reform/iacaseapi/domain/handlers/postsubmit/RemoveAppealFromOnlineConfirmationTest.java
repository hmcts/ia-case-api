package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice.TimeToLiveDataService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RemoveAppealFromOnlineConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private TTL ttl;
    @Mock
    private TimeToLiveDataService timeToLiveDataService;

    private RemoveAppealFromOnlineConfirmation removeAppealFromOnlineConfirmation;

    @BeforeEach
    void setup() {
        removeAppealFromOnlineConfirmation = new RemoveAppealFromOnlineConfirmation(timeToLiveDataService);
    }

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_APPEAL_FROM_ONLINE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.ENDED);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));
        when(ttl.getSuspended()).thenReturn(YesOrNo.YES);

        PostSubmitCallbackResponse callbackResponse =
            removeAppealFromOnlineConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You've removed this appeal from the online service");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "## Do this next\n"
                    + "You now need to:</br>"
                    +
                    "1.Contact the appellant and the respondent to inform them that the case will proceed offline.</br>"
                    + "2.Save all files associated with the appeal to the shared drive.</br>"
                    +
                    "3.Email a link to the saved files with the appeal reference number to: BAUArnhemHouse@justice.gov.uk"
            );

        verify(timeToLiveDataService, times(1)).updateTheClock(callback, false);
    }

    @Test
    void should_not_retrigger_manageCaseTtl_if_clock_already_active() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_APPEAL_FROM_ONLINE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.ENDED);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));
        when(ttl.getSuspended()).thenReturn(YesOrNo.NO);

        PostSubmitCallbackResponse callbackResponse =
            removeAppealFromOnlineConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(timeToLiveDataService, never()).updateTheClock(callback, false);
    }

    @Test
    void should_not_retrigger_manageCaseTtl_if_callback_was_unsuccessful() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_APPEAL_FROM_ONLINE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED); // unexpected state

        PostSubmitCallbackResponse callbackResponse =
            removeAppealFromOnlineConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(timeToLiveDataService, never()).updateTheClock(callback, false);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> removeAppealFromOnlineConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = removeAppealFromOnlineConfirmation.canHandle(callback);

            if (event == Event.REMOVE_APPEAL_FROM_ONLINE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeAppealFromOnlineConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeAppealFromOnlineConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
