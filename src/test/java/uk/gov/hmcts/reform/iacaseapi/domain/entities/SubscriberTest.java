package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

class SubscriberTest {

    private final String email = "test@email.com";
    private final YesOrNo wantsEmail = YesOrNo.YES;
    private final String mobileNumber = "07445111222";
    private final YesOrNo wantsSms = YesOrNo.YES;

    private Subscriber subscriber;

    @BeforeEach
    public void setUp() {

        subscriber = new Subscriber(
            SubscriberType.SUPPORTER,
            email,
            wantsEmail,
            mobileNumber,
            wantsSms
        );
    }

    @Test
    void should_hold_onto_values() {
        assertThat(subscriber.getSubscriber()).isEqualTo(SubscriberType.SUPPORTER);
        assertThat(subscriber.getEmail()).isEqualTo(email);
        assertThat(subscriber.getWantsEmail()).isEqualTo(wantsEmail);
        assertThat(subscriber.getMobileNumber()).isEqualTo(mobileNumber);
        assertThat(subscriber.getWantsSms()).isEqualTo(wantsSms);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> new Subscriber(null, "", YesOrNo.NO, "", YesOrNo.NO))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
