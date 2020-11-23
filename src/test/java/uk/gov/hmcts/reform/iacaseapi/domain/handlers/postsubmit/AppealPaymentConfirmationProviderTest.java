package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PBA_NUMBER;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AppealPaymentConfirmationProviderTest {

    @Mock
    private AsylumCase asylumCase;

    private AppealPaymentConfirmationProvider appealPaymentConfirmationProvider =
        new AppealPaymentConfirmationProvider();

    @Test
    public void should_return_correct_value_for_payment_reference() {

        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.of("Some account reference"));

        assertThat(appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase))
            .isEqualTo("Some account reference");
    }

    @Test
    public void should_return_correct_value_for_account_number() {

        when(asylumCase.read(PBA_NUMBER, String.class)).thenReturn(Optional.of("Some account number"));

        assertThat(appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase))
            .isEqualTo("Some account number");
    }

    @Test
    public void should_return_correct_value_for_fee_amount_for_display() {

        when(asylumCase.read(FEE_AMOUNT_FOR_DISPLAY, String.class)).thenReturn(Optional.of("£10"));

        assertThat(appealPaymentConfirmationProvider.getFeeWithFormat(asylumCase)).isEqualTo("£10");
    }

    @Test
    public void should_return_correct_value_for_payment_status() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        assertThat(appealPaymentConfirmationProvider.getPaymentStatus(asylumCase))
            .isEqualTo(Optional.of(PaymentStatus.PAID));
    }
}
