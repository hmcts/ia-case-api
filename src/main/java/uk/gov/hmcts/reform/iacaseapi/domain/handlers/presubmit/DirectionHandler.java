package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionPartiesResolver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionTagResolver;

@Component
public class DirectionHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final DirectionAppender directionAppender;
    private final DirectionPartiesResolver directionPartiesResolver;
    private final DirectionTagResolver directionTagResolver;

    public DirectionHandler(
        DirectionAppender directionAppender,
        DirectionPartiesResolver directionPartiesResolver,
        DirectionTagResolver directionTagResolver
    ) {
        this.directionAppender = directionAppender;
        this.directionPartiesResolver = directionPartiesResolver;
        this.directionTagResolver = directionTagResolver;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Arrays.asList(
                Event.SEND_DIRECTION,
                Event.REQUEST_CASE_EDIT,
                Event.REQUEST_RESPONDENT_EVIDENCE,
                Event.REQUEST_RESPONDENT_REVIEW
            ).contains(callback.getEvent());
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

        String sendDirectionExplanation =
            caseDataMap
                .get(SEND_DIRECTION_EXPLANATION, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionExplanation is not present"));

        String sendDirectionDateDue =
            caseDataMap
                .get(SEND_DIRECTION_DATE_DUE, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionDateDue is not present"));

        Parties directionParties = directionPartiesResolver.resolve(callback);
        DirectionTag directionTag = directionTagResolver.resolve(callback.getEvent());

        Optional<List<IdValue<Direction>>> maybeExistingDirections =
                caseDataMap.get(DIRECTIONS);

        final List<IdValue<Direction>> existingDirections =
                maybeExistingDirections.orElse(emptyList());

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                sendDirectionExplanation,
                directionParties,
                sendDirectionDateDue,
                directionTag
            );

        caseDataMap.write(DIRECTIONS, allDirections);

        caseDataMap.clear(SEND_DIRECTION_EXPLANATION);
        caseDataMap.clear(SEND_DIRECTION_PARTIES);
        caseDataMap.clear(SEND_DIRECTION_DATE_DUE);

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
