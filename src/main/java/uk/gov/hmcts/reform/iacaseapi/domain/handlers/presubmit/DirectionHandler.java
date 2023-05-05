package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.*;

@Component
public class DirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

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
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Arrays.asList(
                Event.SEND_DIRECTION,
                Event.REQUEST_CASE_EDIT,
                Event.REQUEST_RESPONDENT_EVIDENCE,
                Event.REQUEST_RESPONDENT_REVIEW,
                Event.REQUEST_CASE_BUILDING,
                Event.FORCE_REQUEST_CASE_BUILDING,
                Event.REQUEST_REASONS_FOR_APPEAL,
                Event.REQUEST_RESPONSE_REVIEW,
                Event.REQUEST_RESPONSE_AMEND
            ).contains(callback.getEvent());
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

        String sendDirectionExplanation =
            asylumCase
                .read(SEND_DIRECTION_EXPLANATION, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionExplanation is not present"));

        String sendDirectionDateDue =
            asylumCase
                .read(SEND_DIRECTION_DATE_DUE, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionDateDue is not present"));

        Parties directionParties = directionPartiesResolver.resolve(callback);
        DirectionTag directionTag = directionTagResolver.resolve(callback.getEvent());

        Optional<List<IdValue<Direction>>> maybeExistingDirections =
                asylumCase.read(DIRECTIONS);

        final List<IdValue<Direction>> existingDirections =
                maybeExistingDirections.orElse(emptyList());

        List<IdValue<Direction>> allDirections;
        if (Arrays.asList(
                Event.REQUEST_RESPONDENT_EVIDENCE,
                Event.REQUEST_CASE_BUILDING,
                Event.REQUEST_REASONS_FOR_APPEAL,
                Event.REQUEST_RESPONDENT_REVIEW,
                Event.SEND_DIRECTION
        ).contains(callback.getEvent())) {
            allDirections =
                    directionAppender.append(
                            asylumCase,
                            existingDirections,
                            sendDirectionExplanation,
                            directionParties,
                            sendDirectionDateDue,
                            directionTag,
                            callback.getEvent().toString()
                    );

        } else {

            allDirections =
                    directionAppender.append(
                            asylumCase,
                            existingDirections,
                            sendDirectionExplanation,
                            directionParties,
                            sendDirectionDateDue,
                            directionTag
                    );
        }

        asylumCase.write(DIRECTIONS, allDirections);

        asylumCase.clear(SEND_DIRECTION_EXPLANATION);
        asylumCase.clear(SEND_DIRECTION_PARTIES);
        asylumCase.clear(SEND_DIRECTION_AIP_PARTIES);
        asylumCase.clear(SEND_DIRECTION_DATE_DUE);
        asylumCase.clear(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
