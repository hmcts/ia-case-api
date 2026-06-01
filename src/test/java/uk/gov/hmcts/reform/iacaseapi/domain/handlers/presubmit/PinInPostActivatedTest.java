package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREV_JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DATE_UPLOADED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
public class PinInPostActivatedTest {

    private static final String APPELLANT_EMAIL = "appellant@examples.com";
    private static final String APPELLANT_MOBILE_NUMBER = "111222333";
    private static final String AUTH_USER_EMAIL = "authuser@examples.com";
    private static final String REASON_FOR_APPEAL = "reason for appeal";
    private static final String USER_ID = "userId";
    private PinInPostActivated pinInPostActivated;

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private UserDetails userDetails;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock
    private Document document;
    @Captor
    private ArgumentCaptor<List<IdValue<Subscriber>>> subscriptionCaptor;

    @BeforeEach
    public void setUp() throws Exception {
        pinInPostActivated = new PinInPostActivated(userDetailsProvider);
    }

    @Test
    public void fields_are_updated_if_rep_journey() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verifyTimesJourneyTypeUpdated(1);
        verifyTimesLrDetailsCleared(1);
        verify(asylumCase).read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
    }

    @Test
    public void only_subscriptions_are_updated_if_aip_journey() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verifyTimesJourneyTypeUpdated(0);
        verifyTimesLrDetailsCleared(0);
        verify(asylumCase, never()).read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
        verify(asylumCase, never()).read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
    }

    void verifyTimesJourneyTypeUpdated(int times) {
        verify(asylumCase, times(times)).write(JOURNEY_TYPE, JourneyType.AIP);
        verify(asylumCase, times(times)).write(PREV_JOURNEY_TYPE, JourneyType.REP);
    }

    void verifyTimesLrDetailsCleared(int times) {
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_NAME);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_NAME);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID);
        verify(asylumCase, times(times)).clear(AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID);
    }

    static Stream<Arguments> updateCaseStateScenarios() {
        return Stream.of(
            Arguments.of(State.CASE_BUILDING, State.AWAITING_REASONS_FOR_APPEAL),
            Arguments.of(State.CASE_UNDER_REVIEW, State.REASONS_FOR_APPEAL_SUBMITTED)
        );
    }

    @ParameterizedTest
    @MethodSource("updateCaseStateScenarios")
    public void updates_case_building_state(State initialState, State expectedState) {
        when(caseDetails.getState()).thenReturn(initialState);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        assertEquals(expectedState, response.getState());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, mode = EnumSource.Mode.EXCLUDE, names = {"CASE_BUILDING", "CASE_UNDER_REVIEW"})
    public void should_not_update_case_building_state(State state) {
        when(caseDetails.getState()).thenReturn(state);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        assertEquals(state, response.getState());
    }

    @ParameterizedTest
    @CsvSource({
        "payNow,payNow",
        "payLater,payLater",
        "otherValue,payLater"
    })
    void should_updatePaymentOption_if_present(String initialPaymentOption, String expectedAipPaymentOption) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.of(initialPaymentOption));
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
        verify(asylumCase).write(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, expectedAipPaymentOption);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION);
    }

    @Test
    void should_not_updatePaymentOption_if_not_present() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
        verify(asylumCase, never()).write(eq(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION), anyString());
        verify(asylumCase, never()).clear(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION);
    }

    @Test
    void should_buildSubscriptions_if_none_existing_no_contact_preference() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase).read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, ContactPreference.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.MOBILE_NUMBER, String.class);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.SUBSCRIPTIONS), subscriptionCaptor.capture());
        List<IdValue<Subscriber>> actualSubscriptions = subscriptionCaptor.getValue();
        assertEquals(1, actualSubscriptions.size());
        assertEquals(USER_ID, actualSubscriptions.getFirst().getId());
        Subscriber actualSubscriber = actualSubscriptions.getFirst().getValue();
        assertEquals(SubscriberType.APPELLANT, actualSubscriber.getSubscriber());
        assertEquals(AUTH_USER_EMAIL, actualSubscriber.getEmail());
        assertEquals(YesOrNo.NO, actualSubscriber.getWantsEmail());
        assertNull(actualSubscriber.getMobileNumber());
        assertEquals(YesOrNo.NO, actualSubscriber.getWantsSms());

        verify(asylumCase).clear(AsylumCaseFieldDefinition.EMAIL);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.MOBILE_NUMBER);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION);
    }

    static Stream<Arguments> buildSubscriptionsScenarios() {
        return Stream.of(
            Arguments.of(ContactPreference.WANTS_EMAIL, YesOrNo.YES, YesOrNo.NO, null),
            Arguments.of(ContactPreference.WANTS_SMS, YesOrNo.NO, YesOrNo.YES, null),
            Arguments.of(null, YesOrNo.NO, YesOrNo.NO, "123456789")
        );
    }

    @ParameterizedTest
    @MethodSource("buildSubscriptionsScenarios")
    void should_buildSubscriptions(ContactPreference contactPreference,
                                   YesOrNo wantsEmail,
                                   YesOrNo wantsSms,
                                   String mobileNumber) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.ofNullable(contactPreference));
        when(asylumCase.read(AsylumCaseFieldDefinition.MOBILE_NUMBER, String.class))
            .thenReturn(Optional.ofNullable(mobileNumber));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase).read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, ContactPreference.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.MOBILE_NUMBER, String.class);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.SUBSCRIPTIONS), subscriptionCaptor.capture());
        List<IdValue<Subscriber>> actualSubscriptions = subscriptionCaptor.getValue();
        assertEquals(1, actualSubscriptions.size());
        assertEquals(USER_ID, actualSubscriptions.getFirst().getId());
        Subscriber actualSubscriber = actualSubscriptions.getFirst().getValue();
        assertEquals(SubscriberType.APPELLANT, actualSubscriber.getSubscriber());
        assertEquals(AUTH_USER_EMAIL, actualSubscriber.getEmail());
        assertEquals(wantsEmail, actualSubscriber.getWantsEmail());
        assertEquals(mobileNumber, actualSubscriber.getMobileNumber());
        assertEquals(wantsSms, actualSubscriber.getWantsSms());

        verify(asylumCase).clear(AsylumCaseFieldDefinition.EMAIL);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.MOBILE_NUMBER);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION);
    }

    @Test
    void should_updateExistingSubscription_if_existing() {
        Subscriber existingSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            APPELLANT_EMAIL,
            YesOrNo.YES,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.NO);
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.of(List.of(new IdValue<>("existingId", existingSubscriber))));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.SUBSCRIPTIONS), subscriptionCaptor.capture());
        List<IdValue<Subscriber>> actualSubscriptions = subscriptionCaptor.getValue();
        assertEquals(1, actualSubscriptions.size());
        assertEquals(USER_ID, actualSubscriptions.getFirst().getId());
        Subscriber actualSubscriber = actualSubscriptions.getFirst().getValue();
        assertEquals(SubscriberType.APPELLANT, actualSubscriber.getSubscriber());
        assertEquals(AUTH_USER_EMAIL, actualSubscriber.getEmail());
        assertEquals(YesOrNo.YES, actualSubscriber.getWantsEmail());
        assertEquals(APPELLANT_MOBILE_NUMBER, actualSubscriber.getMobileNumber());
        assertEquals(YesOrNo.NO, actualSubscriber.getWantsSms());
    }

    @Test
    void should_not_updateExistingSubscription_if_existing_but_wants_sms() {
        Subscriber existingSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            APPELLANT_EMAIL,
            YesOrNo.NO,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.YES);
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.of(List.of(new IdValue<>("existingId", existingSubscriber))));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase, never()).clear(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase, never()).write(eq(AsylumCaseFieldDefinition.SUBSCRIPTIONS), anyList());
    }

    @Test
    void should_not_updateExistingSubscription_if_existing_but_email_is_same() {
        Subscriber existingSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            APPELLANT_EMAIL,
            YesOrNo.YES,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.NO);
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.of(List.of(new IdValue<>("existingId", existingSubscriber))));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(APPELLANT_EMAIL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase, never()).clear(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        verify(asylumCase, never()).write(eq(AsylumCaseFieldDefinition.SUBSCRIPTIONS), anyList());
    }

    @Test
    void should_not_updateReasonForAppeal_if_decision_not_empty() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.of(REASON_FOR_APPEAL));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.empty());

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase, never()).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DECISION), anyString());
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DATE_UPLOADED), anyString());
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DOCUMENTS), anyList());
    }

    @Test
    void should_not_updateReasonForAppeal_if_decision_empty_no_legal_rep_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.empty());

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DECISION), anyString());
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DATE_UPLOADED), anyString());
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DOCUMENTS), anyList());
    }

    @ParameterizedTest
    @EnumSource(value = DocumentTag.class, names = {"CASE_ARGUMENT"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_updateReasonForAppeal_if_decision_empty_no_case_argument_documents(DocumentTag documentTag) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.empty());
        List<IdValue<DocumentWithMetadata>> legalRepDocuments = List.of(
            new IdValue<>("1", new DocumentWithMetadata(document,
                "someDescription", "someDateUploaded", documentTag))
        );
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(legalRepDocuments));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DECISION), anyString());
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DATE_UPLOADED), anyString());
        verify(asylumCase, never()).write(eq(REASONS_FOR_APPEAL_DOCUMENTS), anyList());
    }

    @Test
    void should_updateReasonForAppeal_if_decision_empty_with_case_argument_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS)).thenReturn(Optional.empty());
        String docDescription = "someDescription";
        String docDateUploaded = "someDateUploaded";
        List<IdValue<DocumentWithMetadata>> caseArgumentDocuments = List.of(
            new IdValue<>("1", new DocumentWithMetadata(document,
                docDescription, docDateUploaded, DocumentTag.CASE_ARGUMENT))
        );
        List<IdValue<DocumentWithMetadata>> legalRepDocuments = new ArrayList<>(List.of(
            new IdValue<>("2", new DocumentWithMetadata(document,
                docDescription, docDateUploaded, DocumentTag.ADA_SUITABILITY))
        ));
        legalRepDocuments.addAll(caseArgumentDocuments);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(legalRepDocuments));

        pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        verify(asylumCase).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(asylumCase).write(REASONS_FOR_APPEAL_DECISION, docDescription);
        verify(asylumCase).write(REASONS_FOR_APPEAL_DATE_UPLOADED, docDateUploaded);
        verify(asylumCase).write(REASONS_FOR_APPEAL_DOCUMENTS, caseArgumentDocuments);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        assertTrue(pinInPostActivated.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE,
        names = {"PIP_ACTIVATION"})
    void it_cannot_handle_callback_invalid_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(pinInPostActivated.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_SUBMIT"})
    void it_cannot_handle_callback_invalid_callbackStage(PreSubmitCallbackStage callbackStage) {
        assertFalse(pinInPostActivated.canHandle(callbackStage, callback));
    }

    @Test
    void should_throw_exception_when_cannot_handle() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> pinInPostActivated.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse));
        assertEquals("Cannot handle callback", exception.getMessage());
    }
}