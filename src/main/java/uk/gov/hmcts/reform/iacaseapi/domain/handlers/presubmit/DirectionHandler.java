package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_TRANSFERRED_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionPartiesResolver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionTagResolver;

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

        Set<Event> eligibleEvents =  Sets.newHashSet(Event.SEND_DIRECTION,
            Event.REQUEST_CASE_EDIT,
            Event.REQUEST_RESPONDENT_EVIDENCE,
            Event.REQUEST_RESPONDENT_REVIEW,
            Event.REQUEST_CASE_BUILDING,
            Event.FORCE_REQUEST_CASE_BUILDING,
            Event.REQUEST_REASONS_FOR_APPEAL,
            Event.REQUEST_RESPONSE_AMEND);

        if (!isExAdaWithSubmittedHearingRequirements(callback)) {
            eligibleEvents.add(Event.REQUEST_RESPONSE_REVIEW);
        }

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && eligibleEvents.contains(callback.getEvent());
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
        asylumCase.clear(SEND_DIRECTION_DATE_DUE);
        asylumCase.clear(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isExAdaWithSubmittedHearingRequirements(Callback<AsylumCase> callback) {

        boolean isExAda = callback.getCaseDetails().getCaseData()
            .read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YesOrNo.YES))
            .orElse(false);

        boolean hasSubmittedAdaHearingRequirements = callback.getCaseDetails().getCaseData()
            .read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YesOrNo.YES))
            .orElse(false);

        return isExAda && hasSubmittedAdaHearingRequirements;
    }
}
