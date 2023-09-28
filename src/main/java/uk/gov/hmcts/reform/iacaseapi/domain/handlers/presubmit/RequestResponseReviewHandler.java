package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RequestResponseReviewHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int reviewDueInDays;
    private final DateProvider dateProvider;

    public RequestResponseReviewHandler(
        @Value("${legalRepresentativeReview.dueInDays}") int reviewDueInDays,
        DateProvider dateProvider
    ) {
        this.reviewDueInDays = reviewDueInDays;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        asylumCase.write(SEND_DIRECTION_EXPLANATION,
                "The Home Office has replied to your Appeal Skeleton Argument and evidence. You should review their response.\n\n"
                       + "# Next steps\n\n"
                       + "Review the Home Office response. If you want to respond to what they have said, you should email the Tribunal.\n\n"
                       + "If you do not respond by the date indicated below, the case will automatically go to hearing."
        );

        if (
                isInternalCase(asylumCase)
                && isAppellantInDetention(asylumCase)
                && !isAcceleratedDetainedAppeal(asylumCase)
        ) {
            asylumCase.write(SEND_DIRECTION_PARTIES, Parties.APPELLANT);
        } else {
            asylumCase.write(SEND_DIRECTION_PARTIES, Parties.LEGAL_REPRESENTATIVE);
        }

        asylumCase.write(SEND_DIRECTION_DATE_DUE,
                dateProvider
                        .now()
                        .plusDays(reviewDueInDays)
                        .toString()
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
