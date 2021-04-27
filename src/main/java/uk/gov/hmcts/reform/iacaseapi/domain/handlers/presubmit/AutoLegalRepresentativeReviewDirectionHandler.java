package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class AutoLegalRepresentativeReviewDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int reviewDueInDaysUk;
    private final int reviewDueInDaysOoc;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public AutoLegalRepresentativeReviewDirectionHandler(
        @Value("${legalRepresentativeReview.dueInDaysUk}") int reviewDueInDaysUk,
        @Value("${legalRepresentativeReview.dueInDaysOoc}") int reviewDueInDaysOoc,
        DateProvider dateProvider,
        DirectionAppender directionAppender
    ) {
        this.reviewDueInDaysUk = reviewDueInDaysUk;
        this.reviewDueInDaysOoc = reviewDueInDaysOoc;
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

        int reviewDueInDays;

        if (asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(NO) == YES) {
            reviewDueInDays = reviewDueInDaysOoc;
        } else {
            reviewDueInDays = reviewDueInDaysUk;
        }

        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                    asylumCase,
                    maybeDirections.orElse(emptyList()),
                "The Home Office has replied to your Appeal Skeleton Argument and evidence. You should review their response.\n\n"
                + "# Next steps\n\n"
                + "Review the Home Office response. "
                + "If you want to respond to what they have said, you should email the Tribunal.\n\n"
                + "If you do not respond by the date indicated below, "
                + "the case will automatically go to hearing.",
                Parties.LEGAL_REPRESENTATIVE,
                dateProvider
                    .now()
                    .plusDays(reviewDueInDays)
                    .toString(),
                DirectionTag.LEGAL_REPRESENTATIVE_REVIEW
            );

        asylumCase.write(DIRECTIONS, allDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
