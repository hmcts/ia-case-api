package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class PinInPostActivatedTest {

    private static final String USER_ID = "userId";

    private PinInPostActivated pinInPostActivated;

    private AsylumCase asylumCase;

    @Mock private Callback<AsylumCase> callback;
    
    @Mock private CaseDetails<AsylumCase> caseDetails;

    @Mock private UserDetailsProvider userDetailsProvider;

    @Mock private UserDetails userDetails;


    @BeforeEach
    public void setUp() throws Exception {
        asylumCase = new AsylumCase();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        pinInPostActivated = new PinInPostActivated(userDetailsProvider);
    }

    @Test
    public void journeyType_is_updated() {
        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        Optional<JourneyType> details = response.getData().read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class);
        assertEquals(JourneyType.AIP, details.get());
    }

    @Test
    public void subscription_should_added_wantsEmail() {
        asylumCase.write(AsylumCaseFieldDefinition.EMAIL,"appellant@examples.com");
        asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, ContactPreference.WANTS_EMAIL);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        Optional<List<IdValue<Subscriber>>> subscriptions = response.getData().read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

        assertTrue(subscriptions.isPresent());
        assertEquals(1, subscriptions.get().size());
        assertEquals(USER_ID, subscriptions.get().get(0).getId());

        Subscriber expectedSubscriber = new Subscriber(
                SubscriberType.APPELLANT,
                "appellant@examples.com",
                YesOrNo.YES,
                null,
                YesOrNo.NO);
        assertThat(subscriptions.get().get(0).getValue()).usingRecursiveComparison().isEqualTo(expectedSubscriber);
    }

    @Test
    public void subscription_should_added_wantsSms() {
        asylumCase.write(AsylumCaseFieldDefinition.EMAIL, Optional.of("appellant@examples.com"));
        asylumCase.write(AsylumCaseFieldDefinition.MOBILE_NUMBER, Optional.of("01234123123"));
        asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, Optional.of(ContactPreference.WANTS_SMS));

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        Optional<List<IdValue<Subscriber>>> subscriptions = response.getData().read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

        assertTrue(subscriptions.isPresent());
        assertEquals(1, subscriptions.get().size());
        assertEquals(USER_ID, subscriptions.get().get(0).getId());

        Subscriber expectedSubscriber = new Subscriber(
                SubscriberType.APPELLANT,
                "appellant@examples.com",
                YesOrNo.NO,
                "01234123123",
                YesOrNo.YES);
        assertThat(subscriptions.get().get(0).getValue()).usingRecursiveComparison().isEqualTo(expectedSubscriber);
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
