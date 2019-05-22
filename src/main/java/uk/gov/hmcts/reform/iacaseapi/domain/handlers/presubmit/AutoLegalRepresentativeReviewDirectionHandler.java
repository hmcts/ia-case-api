package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.DIRECTIONS;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
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
public class AutoLegalRepresentativeReviewDirectionHandler implements PreSubmitCallbackHandler<CaseDataMap> {

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
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.ADD_APPEAL_RESPONSE;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<Direction>>> maybeDirections = caseDataMap.get(DIRECTIONS);

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                    maybeDirections.orElse(emptyList()),
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

        caseDataMap.write(DIRECTIONS, allDirections);

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
