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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RequestRespondentReviewPreparer implements PreSubmitCallbackHandler<AsylumCase> {

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
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW;
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

        asylumCase.write(SEND_DIRECTION_EXPLANATION,
                "You have until the date indicated below to review the appellant's argument and evidence. "
                        + "You must explain whether the appellant makes a valid case for overturning the original decision.\n"
                        + "\n"
                        + "You must respond to the Tribunal and tell them:\n"
                        + "\n"
                        + "- whether you oppose all or parts of the appellant's case\n"
                        + "- what your grounds are for opposing the case\n"
                        + "- which of the issues are agreed or not agreed\n"
                        + "- whether there are any further issues you wish to raise\n"
                        + "- whether you are prepared to withdraw to grant\n"
                        + "- whether the appeal can be resolved without a hearing\n\n"
                        + "Next steps\n\n"
                        + "If you do not respond in time the Tribunal will decide how the case should proceed."
        );

        asylumCase.write(DIRECTION_PARTIES, Parties.RESPONDENT);

        asylumCase.write(SEND_DIRECTION_DATE_DUE,
            dateProvider
                .now()
                .plusDays(requestRespondentReviewDueInDays)
                .toString()
        );

        asylumCase.write(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
