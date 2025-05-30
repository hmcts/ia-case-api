package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealSubmittedConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;

    private AppealSubmittedConfirmation appealSubmittedConfirmation;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        appealSubmittedConfirmation = new AppealSubmittedConfirmation(ccdSupplementaryUpdater);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
    }

    @Test
    void should_invoke_supplementary_updater() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        appealSubmittedConfirmation.handle(callback);

        verify(ccdSupplementaryUpdater).setHmctsServiceIdSupplementary(callback);

    }

    @Test
    void should_return_standard_confirmation_when_not_out_of_time() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get()).contains("submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("What happens next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("You will receive an email confirming that this appeal has been submitted successfully.");
    }

    @Test
    void should_return_standard_confirmation_when_not_out_of_time_for_internal_cases() {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get()).contains("submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("What happens next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("A Legal Officer will check the appeal is valid and all parties will be notified of next steps.");
    }

    @Test
    void should_return_out_of_time_confirmation_for_nonpayment_appeal() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn((Optional.of(NO)));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You have submitted this appeal beyond the deadline. The Tribunal Case Officer will decide if it can proceed. You'll get an email telling you whether your appeal can go ahead.");
    }

    @Test
    void should_return_out_of_time_confirmation_for_nonpayment_appeal_for_internal_cases() {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "A Legal Officer will decide if the appeal can proceed.");
    }

    @Test
    void should_return_ejp_confirmation_for_all_appeal_for_ejp_cases() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "A Legal Officer will progress the case to the correct state and upload the relevant documents at each point.");
    }

    @Test
    void aip_should_return_out_of_time_confirmation_for_pay_offline_by_card_ea() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. ");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal.");
    }

    @Test
    void aip_should_return_out_of_time_confirmation_for_pay_offline_hu() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. ");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal.");
    }

    @Test
    void aip_should_return_out_of_time_confirmation_for_pay_offline_pa() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online.");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "Once you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.");
    }

    @Test
    void aip_should_return_out_of_time_confirmation_for_pay_later_pa() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You can do this by selecting Make a payment from the dropdown on the");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "Once you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"EA", "HU", "EU"})
    void lr_should_return_out_of_time_confirmation_for_ea_hu_paPayLater(String appealType) {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(appealType)));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You must now pay for this appeal. First [create a service request](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/generateServiceRequest), you can do this by selecting 'Create a service request' from the 'Next step' dropdown list. Then select 'Go'."
                );
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "Once you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.");
    }


    @Test
    void lr_should_return_out_of_time_confirmation_for_paPayNow() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You must now pay for this appeal. First [create a service request](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/generateServiceRequest), you can do this by selecting 'Create a service request' from the 'Next step' dropdown list. Then select 'Go'.");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "Once you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"EA", "HU", "EU", "PA"})
    void lr_should_return_confirmation_for_ea_hu_paPayNow(String appealType) {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(appealType)));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal has been submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### Do this next");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You must now pay for this appeal. First [create a service request](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/generateServiceRequest), you can do this by selecting 'Create a service request' from the 'Next step' dropdown list. Then select 'Go'."
                );
    }

    @Test
    void lr_should_return_confirmation_for_paPayLater() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);


        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal has been submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. First [create a service request](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/generateServiceRequest), you can do this by selecting 'Create a service request' from the 'Next step' dropdown list. Then select 'Go'."
                );
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "EU"})
    void aip_should_return_confirmation_for_pay_offline_by_card_ea(AppealType appealType) {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal has been submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online. You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal."
                );
    }

    @Test
    void aip_should_return_confirmation_for_pay_offline_by_card_pa() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal has been submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online."
                );
    }

    @Test
    void aip_should_return_confirmation_for_pay_later_by_card_pa() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal has been submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "You still have to pay for this appeal. You can do this by selecting Make a payment from the dropdown on the [overview tab]"
                );
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("and following the instructions."
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"NO", "YES"})
    void should_return_confirmation_for_ho_waiver_remission(String flag) {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.valueOf(flag)));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
                .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());


        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");


        if (flag.equals(NO.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get()
                            .contains("# Your appeal has been submitted"));

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be"
                            + " asked to provide more information. Once the review is complete you will be notified if there is any fee to pay.");
        }
        if (flag.equals(YES.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n"
                            + "You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be"
                            + " asked to provide more information. Once the review is complete you will be notified if there is any fee to pay.\n"
                            + "A Tribunal Caseworker will then review the reasons your appeal was submitted out of time and you will be notified if it can proceed."
                    );
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"NO", "YES"})
    public void should_return_confirmation_for_help_with_fees(String flag) {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.valueOf(flag)));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HELP_WITH_FEES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());


        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");

        if (flag.equals(NO.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be"
                                    +
                                    " asked to provide more information. Once the review is complete you will be notified if there is any fee to pay."
                    );
        }
        if (flag.equals(YES.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n"
                                    +
                                    "You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be"
                                    +
                                    " asked to provide more information. Once the review is complete you will be notified if there is any fee to pay.\n"
                                    +
                                    "A Tribunal Caseworker will then review the reasons your appeal was submitted out of time and you will be notified if it can proceed."
                    );
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "EA", "HU", "PA", "RP", "EU"})
    void handle_should_return_default_confirmation_for_payment_feature_disabled_and_in_time(String type) {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal has been submitted");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("You will receive an email confirming that this appeal has been submitted successfully.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "EA", "HU", "PA", "RP", "EU"})
    void handle_should_return_default_confirmation_for_payment_feature_disabled_and_out_of_time(String type) {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("You have submitted this appeal beyond the deadline. "
                        + "The Tribunal Case Officer will decide if it can proceed. "
                        + "You'll get an email telling you whether your appeal can go ahead.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"NO", "YES"})
    void should_return_confirmation_for_exceptional_circumstances_remission(String flag) {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.valueOf(flag)));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(
                Optional.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### What happens next");


        if (flag.equals(NO.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");


            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be"
                                    +
                                    " asked to provide more information. Once the review is complete you will be notified if there is any fee to pay."
                    );

        }
        if (flag.equals(YES.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");


            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n"
                                    +
                                    "You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be"
                                    +
                                    " asked to provide more information. Once the review is complete you will be notified if there is any fee to pay.\n"
                                    +
                                    "A Tribunal Caseworker will then review the reasons your appeal was submitted out of time and you will be notified if it can proceed."
                    );

        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"NO", "YES"})
    void lr_should_return_confirmation_for_age_assessment_appeals(String flag) {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.valueOf(flag)));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        if (flag.equals(NO.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("#### What happens next");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("You will receive an email confirming that this appeal has been submitted successfully."
                            + "\n\nYou can now apply for [interim relief](#).");
        }

        if (flag.equals(YES.toString())) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# Your appeal has been submitted");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("#### What happens next");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("You have submitted this appeal beyond the deadline. The Tribunal Case Officer will decide if it can proceed. You'll get an email "
                            + "telling you whether your appeal can go ahead."
                            + "\n\nYou can now apply for [interim relief](#).");
        }

    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> appealSubmittedConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmittedConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmittedConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_for_missing_appeal_type() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> appealSubmittedConfirmation.handle(callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Appeal type is not present");
    }

    @Test
    void should_send_postSubmit_payment_callback() {
        reset(callback);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(
                Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        PostSubmitCallbackResponse callbackResponse =
                appealSubmittedConfirmation.handle(callback);

        assertNotNull(callbackResponse);

    }
}
