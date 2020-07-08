package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.PAYMENT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String paymentReferenceNumber = asylumCase
                .read(PAYMENT_REFERENCE, String.class)
                .orElse("");
        String paymentAccountNumber = asylumCase
                .read(PBA_NUMBER, String.class)
                .orElse("");
        BigDecimal fee = asylumCase
                .read(FEE_AMOUNT, BigDecimal.class)
                .orElseThrow(() -> new IllegalStateException("Fee amount is not present"));

        postSubmitResponse.setConfirmationHeader("# You have paid for the appeal \n# You still need to submit it");
        postSubmitResponse.setConfirmationBody(
                "#### Do this next\n\n"
                        + "You still need to [submit your appeal](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() +  "/trigger/submitAppeal)"
                        + "\n\n\n#### Payment successful"
                        + "\n\n#### Payment reference number\n"
                        + paymentReferenceNumber
                        + "\n\n#### Payment by account number\n"
                        + paymentAccountNumber
                        + "\n\n#### Fee\n"
                        + "£" + fee
        );

        return postSubmitResponse;
    }
}
