package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class AutoLegalRepresentativeReviewDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int reviewDueInDays;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public AutoLegalRepresentativeReviewDirectionHandler(
        @Value("${legalRepresentativeReview.dueInDays}") int reviewDueInDays,
        DateProvider dateProvider,
        DirectionAppender directionAppender
    ) {
        this.reviewDueInDays = reviewDueInDays;
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
               && callback.getEvent() == Event.ADD_APPEAL_RESPONSE;
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

        final List<IdValue<Direction>> existingDirections =
            asylumCase
                .getDirections()
                .orElse(Collections.emptyList());

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                "The respondent has replied to your appeal argument and evidence. You must now review their response.\n\n"
                + "Next steps\n"
                + "You have " + reviewDueInDays + " days to review the response. "
                + "If you want to respond to what the Home Office has said, you should email the case officer.\n\n"
                + "If you do not respond within " + reviewDueInDays + " days, "
                + "the case will automatically go to hearing.",
                Parties.LEGAL_REPRESENTATIVE,
                dateProvider
                    .now()
                    .plusDays(reviewDueInDays)
                    .toString(),
                DirectionTag.LEGAL_REPRESENTATIVE_REVIEW
            );

        asylumCase.setDirections(allDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
