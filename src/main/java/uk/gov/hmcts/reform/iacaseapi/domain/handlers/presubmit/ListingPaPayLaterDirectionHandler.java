package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class ListingPaPayLaterDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int paPayLaterDueDate;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public ListingPaPayLaterDirectionHandler(
        @Value("${paPayLaterDueDate}") int paPayLaterDueDate,
        DateProvider dateProvider,
        DirectionAppender directionAppender
    ) {
        this.paPayLaterDueDate = paPayLaterDueDate;
        this.dateProvider = dateProvider;
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent().equals(Event.LIST_CASE)
            && HandlerUtils.isPayLater(callback.getCaseDetails().getCaseData())
            && !HandlerUtils.isAppealPaid(callback.getCaseDetails().getCaseData());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();
        String feeAmount = HandlerUtils.getFeeAmount(asylumCase);
        String content = "Your appeal is going to be decided by a Judge at a hearing and still requires a fee to be paid of £" + feeAmount +
            ". No payment has been received and to avoid further action being taken to recover the fee you should make a payment of £" +
            feeAmount + " without delay.\n" +
            "Instructions for making a payment are: \n" +
            "For appeals submitted online \n" +
            "Legal Representative to make payment by PBA\n" +
            "Appellants follow these steps to pay the fee:\n" +
            "1. Sign in to your account at: Sign in to the service if you’ve already started your appeal..\n" +
            "2. Select 'Pay for this appeal' under the 'I want to' section and follow the steps to make a new payment.\n" +
            "For appeals submitted by post or email \n" +
            "Follow these steps to pay the fee:\n" +
            "1. Call the tribunal on +44 (0)300 123 1711, then select option 3 \n" +
            "2. Provide your 16-digit online case reference number:\n" +
            "3. Make the payment with a debit or credit card\n";
        AsylumCase finalCase = HandlerUtils.feeDirectionReminder(asylumCase, directionAppender, dateProvider, paPayLaterDueDate, DirectionTag.LISTING_PA_PAY_LATER, content);
        return new PreSubmitCallbackResponse<>(finalCase);
    }
}