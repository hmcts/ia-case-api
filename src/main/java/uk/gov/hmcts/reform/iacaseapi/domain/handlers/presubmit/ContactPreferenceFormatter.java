package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ContactPreferenceFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();
        Subscriber subscriber = new Subscriber();
        subscriber.setSubscriber(SubscriberType.APPELLANT);

        Optional<ContactPreference> contactPreference = asylumCase.read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, ContactPreference.class);
        if (contactPreference.isPresent() && contactPreference.get().toString().equals(ContactPreference.WANTS_EMAIL.toString())) {
            subscriber.setWantsEmail(YesOrNo.YES);
            subscriber.setEmail(asylumCase.read(EMAIL).toString());
        } else {
            subscriber.setWantsEmail(YesOrNo.NO);
            subscriber.setEmail("");
        }

        if (contactPreference.isPresent() && contactPreference.get().getValue().equals(ContactPreference.WANTS_SMS.toString())) {
            subscriber.setWantsSms(YesOrNo.YES);
            subscriber.setMobileNumber(asylumCase.read(MOBILE_NUMBER).toString());
        } else {
            subscriber.setWantsSms(YesOrNo.NO);
            subscriber.setMobileNumber("");
        }

        List<IdValue<Subscriber>> subscriptions = new ArrayList<>();
        subscriptions.add(new IdValue<>("1",subscriber));

        asylumCase.write(
            SUBSCRIPTIONS,
            new ArrayList<>(subscriptions)
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

