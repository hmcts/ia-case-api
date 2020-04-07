package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("squid:S1700")
public class Subscriber {

    private SubscriberType subscriber;
    private String email;
    private YesOrNo wantsEmail;
    private String mobileNumber;
    private YesOrNo wantsSms;

    public Subscriber() {
        //No-op constructor
    }

    public Subscriber(final SubscriberType subscriber, final String email, final YesOrNo wantsEmail, final String mobileNumber, final YesOrNo wantsSms) {
        requireNonNull(subscriber);
        this.subscriber = subscriber;
        this.email = email;
        this.wantsEmail = wantsEmail;
        this.mobileNumber = mobileNumber;
        this.wantsSms = wantsSms;
    }

    public SubscriberType getSubscriber() {
        requireNonNull(subscriber);
        return subscriber;
    }

    public void setSubscriber(SubscriberType subscriber) {
        this.subscriber = subscriber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWantsEmail(YesOrNo wantsEmail) {
        this.wantsEmail = wantsEmail;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setWantsSms(YesOrNo wantsSms) {
        this.wantsSms = wantsSms;
    }

    public String getEmail() {
        return email;
    }

    public YesOrNo getWantsEmail() {
        return wantsEmail;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public YesOrNo getWantsSms() {
        return wantsSms;
    }
}
