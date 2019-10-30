package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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

    private final int legalRepresentativeReviewDueInDays;
    private final DateProvider dateProvider;

    public RequestResponseReviewHandler(
            @Value("${legalRepresentativeReview.dueInDays}") int legalRepresentativeReviewDueInDays,
            DateProvider dateProvider
    ) {
        this.legalRepresentativeReviewDueInDays = legalRepresentativeReviewDueInDays;
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
                "The respondent has replied to your appeal argument and evidence. You must now review their response.\n\n"
                       + "Next steps\n"
                       + "If you would like to respond, you must email the Tribunal caseworker within 5 days.\n"
                       + "If you do not respond within 5 days, the case will automatically go to hearing."
        );

        asylumCase.write(SEND_DIRECTION_PARTIES, Parties.LEGAL_REPRESENTATIVE);

        asylumCase.write(SEND_DIRECTION_DATE_DUE,
                dateProvider
                        .now()
                        .plusDays(legalRepresentativeReviewDueInDays)
                        .toString()
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
