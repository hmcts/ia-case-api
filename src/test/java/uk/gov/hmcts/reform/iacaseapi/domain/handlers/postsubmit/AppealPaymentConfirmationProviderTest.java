package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealPaymentConfirmationProviderTest {

    @Mock
    private AsylumCase asylumCase;

    private AppealPaymentConfirmationProvider appealPaymentConfirmationProvider =
        new AppealPaymentConfirmationProvider();

    @Test
    void should_return_correct_value_for_payment_reference() {

        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.of("Some account reference"));

        assertThat(appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase))
            .isEqualTo("Some account reference");
    }

    @Test
    void should_return_correct_value_for_account_number() {

        when(asylumCase.read(PBA_NUMBER, String.class)).thenReturn(Optional.of("Some account number"));

        assertThat(appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase))
            .isEqualTo("Some account number");
    }

    @Test
    void should_return_correct_value_for_fee_amount_for_display() {

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));

        assertThat(appealPaymentConfirmationProvider.getFee(asylumCase)).isEqualTo("140");
    }

    @Test
    void should_return_correct_fee_for_fee_without_hearing() {

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithoutHearing"));
        when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));

        assertThat(appealPaymentConfirmationProvider.getFee(asylumCase)).isEqualTo("80");
    }

    @Test
    void should_return_correct_value_for_payment_status() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        assertThat(appealPaymentConfirmationProvider.getPaymentStatus(asylumCase))
            .isEqualTo(Optional.of(PaymentStatus.PAID));
    }
}
