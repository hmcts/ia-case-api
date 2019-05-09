package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RequestRespondentReviewPreparer implements PreSubmitCallbackHandler<CaseDataMap> {

    private final int requestRespondentReviewDueInDays;
    private final DateProvider dateProvider;

    public RequestRespondentReviewPreparer(
        @Value("${requestRespondentReview.dueInDays}") int requestRespondentReviewDueInDays,
        DateProvider dateProvider
    ) {
        this.requestRespondentReviewDueInDays = requestRespondentReviewDueInDays;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        CaseDataMap.setSendDirectionExplanation(
            "You must now review this case.\n\n"
            + "You have " + requestRespondentReviewDueInDays + " days to review the appeal argument and evidence. "
            + "You must explain whether the appellant's appeal argument makes a valid case for overturning the "
            + "original protection decision.\n\n"
            + "You must respond to the case officer and tell them:\n"
            + "- whether you oppose all or part of the appellant's case\n"
            + "- what your grounds are for opposing the case\n"
            + "- which of the issues are agreed or not agreed\n"
            + "- whether there are any further issues you wish to raise\n"
            + "- whether you are prepared to withdraw to grant or reconsider\n"
            + "- whether the appeal can be resolved without a hearing\n\n"
            + "You may find it helpful to use the response template provided.\n\n"
            + "Next steps\n\n"
            + "If you do not respond in time, the case officer will decide how the case should proceed."
        );

        CaseDataMap.setSendDirectionParties(Parties.RESPONDENT);

        CaseDataMap.setSendDirectionDateDue(
            dateProvider
                .now()
                .plusDays(requestRespondentReviewDueInDays)
                .toString()
        );

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
