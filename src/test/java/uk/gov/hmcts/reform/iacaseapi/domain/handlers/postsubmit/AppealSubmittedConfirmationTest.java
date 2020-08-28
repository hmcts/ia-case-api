package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmittedConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private AppealSubmittedConfirmation appealSubmittedConfirmation =
        new AppealSubmittedConfirmation();

    @Before
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_return_standard_confirmation_when_not_out_of_time() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("submitted")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("What happens next")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You will receive an email confirming that this appeal has been submitted successfully.")
        );
    }

    @Test
    public void should_return_out_of_time_confirmation_for_nonpayment_appeal() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get(),
                containsString("")
        );

        assertThat(
                callbackResponse.getConfirmationBody().get(),
                containsString("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You have submitted this appeal beyond the deadline. The Tribunal Case Officer will decide if it can proceed. You'll get an email telling you whether your appeal can go ahead.")
        );
    }

    @Test
    public void should_return_out_of_time_confirmation_for_pay_offline_by_card_ea() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. ")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal.")
        );
    }

    @Test
    public void should_return_out_of_time_confirmation_for_pay_offline_hu() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. ")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal.")
        );
    }

    @Test
    public void should_return_out_of_time_confirmation_for_pay_offline_pa() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online.")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("Once you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.")
        );
    }

    @Test
    public void should_return_out_of_time_confirmation_for_pay_later_pa() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You can do this by selecting Make a payment from the dropdown on the")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("Once you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.")
        );
    }

    @Test
    public void should_return_confirmation_for_pay_offline_by_card_ea() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# Your appeal has been submitted")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal."
            )
        );
    }

    @Test
    public void should_return_confirmation_for_pay_offline_by_card_hu() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# Your appeal has been submitted")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal."
            )
        );
    }

    @Test
    public void should_return_confirmation_for_pay_offline_by_card_pa() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# Your appeal has been submitted")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online."
            )
        );
    }

    @Test
    public void should_return_confirmation_for_pay_later_by_card_pa() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# Your appeal has been submitted")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You still have to pay for this appeal. You can do this by selecting Make a payment from the dropdown on the [overview tab]"
            )
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("and following the instructions."
            )
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> appealSubmittedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = appealSubmittedConfirmation.canHandle(callback);

            if (event == Event.SUBMIT_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmittedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmittedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
