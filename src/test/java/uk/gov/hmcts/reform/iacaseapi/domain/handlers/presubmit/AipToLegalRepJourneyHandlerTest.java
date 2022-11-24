package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AipToLegalRepJourneyHandlerTest {

    private static final String APPELLANT_EMAIL = "appellant@examples.com";
    private static final String APPELLANT_MOBILE_NUMBER = "111222333";
    private static final String USER_ID = "userId";
    private AipToLegalRepJourneyHandler aipToLegalRepJourneyHandler;
    private List<IdValue<Subscriber>> subscriptions;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    @BeforeEach
    public void setUp() throws Exception {
        aipToLegalRepJourneyHandler = new AipToLegalRepJourneyHandler();
        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, APPELLANT_EMAIL, YesOrNo.YES, APPELLANT_MOBILE_NUMBER, YesOrNo.NO);
        subscriptions = Arrays.asList(new IdValue<>(USER_ID, subscriber));

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.of(subscriptions));
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = aipToLegalRepJourneyHandler.canHandle(callbackStage, callback);

                if (event == Event.NOC_REQUEST
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void it_should_not_handle_callback_for_rep_journey() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = aipToLegalRepJourneyHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void it_should_convert_case_to_rep_journey() {
        aipToLegalRepJourneyHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        verify(asylumCase, times(1)).remove(JOURNEY_TYPE.value());
    }

    @Test
    void state_is_reasonsForAppealSubmitted_transition_to_caseUnderReview() {
        when(caseDetails.getState()).thenReturn(State.REASONS_FOR_APPEAL_SUBMITTED);

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(response.getState(), State.CASE_UNDER_REVIEW);
    }

    @Test
    void state_is_awaitingReasonsForAppeal_transition_to_caseBuilding() {
        when(caseDetails.getState()).thenReturn(State.AWAITING_REASONS_FOR_APPEAL);

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(response.getState(), State.CASE_BUILDING);
    }

    @Test
    void state_is_awaitingClarifyingQuestionsAnswers_transition_to_previousState() {
        when(caseDetails.getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);
        when(asylumCase.read(PRE_CLARIFYING_STATE, State.class)).thenReturn(Optional.of(State.APPEAL_SUBMITTED));

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.APPEAL_SUBMITTED, response.getState());
    }

    @Test
    void state_is_awaitingClarifyingQuestionsAnswers_transition_to_legal_rep_valid_state() {
        when(caseDetails.getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);
        when(asylumCase.read(PRE_CLARIFYING_STATE, State.class)).thenReturn(Optional.of(State.REASONS_FOR_APPEAL_SUBMITTED));

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.CASE_UNDER_REVIEW, response.getState());
    }

    @Test
    void should_throw_when_subscription_is_not_present() {
        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aipToLegalRepJourneyHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Subscription must not be null")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_update_appellant_contact_details_with_email_preference() {

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1)).write(EMAIL, APPELLANT_EMAIL);
        verify(asylumCase, times(1)).write(MOBILE_NUMBER, APPELLANT_MOBILE_NUMBER);
        verify(asylumCase, times(1)).write(CONTACT_PREFERENCE, ContactPreference.WANTS_EMAIL);
        verify(asylumCase, times(1)).write(CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
        verify(asylumCase, times(1)).clear(SUBSCRIPTIONS);
    }

    @Test
    void should_update_appellant_contact_details_with_sms_preference() {
        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, APPELLANT_EMAIL, YesOrNo.NO, APPELLANT_MOBILE_NUMBER, YesOrNo.YES);
        subscriptions = Arrays.asList(new IdValue<>(USER_ID, subscriber));

        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.of(subscriptions));


        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1)).write(EMAIL, APPELLANT_EMAIL);
        verify(asylumCase, times(1)).write(MOBILE_NUMBER, APPELLANT_MOBILE_NUMBER);
        verify(asylumCase, times(1)).write(CONTACT_PREFERENCE, ContactPreference.WANTS_SMS);
        verify(asylumCase, times(1)).write(CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_SMS.getDescription());
        verify(asylumCase, times(1)).clear(SUBSCRIPTIONS);
    }
}
