package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class PinInPostActivated implements PreSubmitCallbackHandler<AsylumCase> {

    private UserDetailsProvider userDetailsProvider;

    public PinInPostActivated(UserDetailsProvider userDetailsProvider) {
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.PIP_ACTIVATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        updateJourneyType(asylumCase);
        updateSubscription(asylumCase);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void updateJourneyType(AsylumCase asylumCase) {
        asylumCase.write(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.AIP);
    }

    private void updateSubscription(AsylumCase asylumCase) {
        Optional<ContactPreference> contactPreference = asylumCase.read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        Optional<String> mobileNumber = asylumCase.read(AsylumCaseFieldDefinition.MOBILE_NUMBER);
        Optional<String> email = asylumCase.read(AsylumCaseFieldDefinition.EMAIL);

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT,
            email.orElse(null),
            contactPreference.filter(p -> p.equals(ContactPreference.WANTS_EMAIL)).map(p -> YesOrNo.YES).orElse(YesOrNo.NO),
            mobileNumber.orElse(null),
            contactPreference.filter(p -> p.equals(ContactPreference.WANTS_SMS)).map(p -> YesOrNo.YES).orElse(YesOrNo.NO));

        asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Arrays.asList(
            new IdValue<>(userDetailsProvider.getUserDetails().getId(), subscriber)));
    }
}
