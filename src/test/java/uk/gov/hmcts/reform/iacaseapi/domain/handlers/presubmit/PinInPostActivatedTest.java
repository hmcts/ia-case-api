package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
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

@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;

    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    @BeforeEach
    public void setUp() throws Exception {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(AUTH_USER_EMAIL);
        when(userDetails.getId()).thenReturn(USER_ID);
        pinInPostActivated = new PinInPostActivated(userDetailsProvider);
    }

    @Test
    public void journeyType_is_updated() {
        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        Optional<JourneyType> details = response.getData().read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class);
        assertEquals(JourneyType.AIP, details.get());
    }

    @Test
    public void should_build_subscription_with_contact_preference_email() {
        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Optional.of(Collections.emptyList()));
        asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, Optional.of(ContactPreference.WANTS_EMAIL));
        asylumCase.write(AsylumCaseFieldDefinition.MOBILE_NUMBER, Optional.of(APPELLANT_MOBILE_NUMBER));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        Optional<List<IdValue<Subscriber>>> expectedSubscriptions = response.getData().read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

        assertTrue(expectedSubscriptions.isPresent());
        assertEquals(1, expectedSubscriptions.get().size());
        assertEquals(USER_ID, expectedSubscriptions.get().get(0).getId());

        Subscriber expectedSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            AUTH_USER_EMAIL,
            YesOrNo.YES,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.NO);
        assertThat(expectedSubscriptions.get().get(0).getValue()).usingRecursiveComparison().isEqualTo(expectedSubscriber);
    }

    @Test
    public void should_build_subscription_with_contact_preference_sms() {
        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Optional.of(Collections.emptyList()));
        asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, Optional.of(ContactPreference.WANTS_SMS));
        asylumCase.write(AsylumCaseFieldDefinition.MOBILE_NUMBER, Optional.of(APPELLANT_MOBILE_NUMBER));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        Optional<List<IdValue<Subscriber>>> expectedSubscriptions = response.getData().read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

        assertTrue(expectedSubscriptions.isPresent());
        assertEquals(1, expectedSubscriptions.get().size());
        assertEquals(USER_ID, expectedSubscriptions.get().get(0).getId());

        Subscriber expectedSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            AUTH_USER_EMAIL,
            YesOrNo.NO,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.YES);
        assertThat(expectedSubscriptions.get().get(0).getValue()).usingRecursiveComparison().isEqualTo(expectedSubscriber);
    }

    @Test
    public void should_update_existing_subscription() {
        Subscriber existingSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            APPELLANT_EMAIL,
            YesOrNo.YES,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.NO);

        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Optional.of(Arrays.asList(new IdValue<>(USER_ID, existingSubscriber))));
        asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, Optional.of(ContactPreference.WANTS_SMS));
        asylumCase.write(AsylumCaseFieldDefinition.MOBILE_NUMBER, Optional.of(APPELLANT_MOBILE_NUMBER));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        Optional<List<IdValue<Subscriber>>> expectedSubscriptions = response.getData().read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

        assertTrue(expectedSubscriptions.isPresent());
        assertEquals(1, expectedSubscriptions.get().size());
        assertEquals(USER_ID, expectedSubscriptions.get().get(0).getId());

        Subscriber expectedSubscriber = new Subscriber(
            SubscriberType.APPELLANT,
            AUTH_USER_EMAIL,
            YesOrNo.YES,
            APPELLANT_MOBILE_NUMBER,
            YesOrNo.NO);
        assertThat(expectedSubscriptions.get().get(0).getValue()).usingRecursiveComparison().isEqualTo(expectedSubscriber);
    }

    @Test
    public void payment_type_should_be_updated_for_aip() {
        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        asylumCase.write(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, Optional.of("payNow"));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback, callbackResponse
        );

        Optional<String> paymentOption = response.getData().read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION);
        assertFalse(paymentOption.isPresent());

        Optional<String> aipPaymentOption = response.getData().read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION);
        assertTrue(aipPaymentOption.isPresent());
        assertEquals("payNow", aipPaymentOption.get());
    }

    @Test
    public void caseData_should_contain_reason_for_appeal_field() {
        Document document = new Document(
            "documentUrl", "documentBinaryUrl", "documentFileName"
        );
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, "description", "dateUploaded", DocumentTag.CASE_ARGUMENT
        );
        IdValue<DocumentWithMetadata> documentWithMetadataIdValue = new IdValue<>("id1", documentWithMetadata);
        List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments = Arrays.asList(documentWithMetadataIdValue);

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalRepresentativeDocuments));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, documentWithMetadata.getDescription());
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DATE_UPLOADED, documentWithMetadata.getDateUploaded());
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS, Arrays.asList(documentWithMetadataIdValue));
    }

    @Test
    public void when_state_caseBuilding_change_to_awaitingReasonsForAppeal() {
        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback, callbackResponse
        );

        assertEquals(State.AWAITING_REASONS_FOR_APPEAL, response.getState());
    }

    @Test
    public void when_state_caseUnderReview_change_to_reasonsForAppealSubmitted() {
        asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.CASE_UNDER_REVIEW);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        assertEquals(State.REASONS_FOR_APPEAL_SUBMITTED, response.getState());
    }

    @Test
    public void should_not_update_reason_for_appeal_if_already_existis() {
        when(asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION)).thenReturn(Optional.of(REASON_FOR_APPEAL));
        Document document = new Document(
            "documentUrl", "documentBinaryUrl", "documentFileName"
        );
        DocumentWithMetadata documentWithMetadata = new DocumentWithMetadata(
            document, "description", "dateUploaded", DocumentTag.CASE_ARGUMENT
        );
        IdValue<DocumentWithMetadata> documentWithMetadataIdValue = new IdValue<>("id1", documentWithMetadata);
        List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments = Arrays.asList(documentWithMetadataIdValue);

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalRepresentativeDocuments));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback, callbackResponse
        );

        assertNotNull(response);

        verify(asylumCase, times(0)).write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, documentWithMetadata.getDescription());
        verify(asylumCase, times(0)).write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DATE_UPLOADED, documentWithMetadata.getDateUploaded());
        verify(asylumCase, times(0)).write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS, Arrays.asList(documentWithMetadataIdValue));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = pinInPostActivated.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && event == Event.PIP_ACTIVATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }
}