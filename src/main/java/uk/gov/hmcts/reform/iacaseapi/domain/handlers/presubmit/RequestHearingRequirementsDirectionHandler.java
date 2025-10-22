package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
@Slf4j
public class RequestHearingRequirementsDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int hearingRequirementsDueInDays;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public RequestHearingRequirementsDirectionHandler(
        @Value("${legalRepresentativeHearingRequirements.dueInDays}") int hearingRequirementsDueInDays,
        DateProvider dateProvider,
        DirectionAppender directionAppender
    ) {
        this.hearingRequirementsDueInDays = hearingRequirementsDueInDays;
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
               && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE;
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

        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);

        final List<IdValue<Direction>> existingDirections =
            maybeDirections.orElse(emptyList());

        List<IdValue<Direction>> allDirections =
                directionAppender.append(
                        asylumCase,
                    existingDirections,
                    "The appeal is going to a hearing and you should tell the Tribunal if the appellant has any hearing requirements.\n\n"
                    + "# Next steps\n\n"
                    + "Visit the online service and use the HMCTS reference to find the case. You'll be able to submit the hearing requirements by following the instructions on the overview tab.\n\n"
                    + "The Tribunal will review the hearing requirements and any requests for additional adjustments. You'll then be sent a hearing date.\n\n"
                    + "If you do not submit the hearing requirements by the date indicated below, the Tribunal may not be able to accommodate the appellant's needs for the hearing.\n",
                    resolvePartiesForHearingRequirements(asylumCase),
                    dateProvider
                        .now()
                        .plusDays(hearingRequirementsDueInDays)
                        .toString(),
                    DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS,
                        callback.getEvent().toString()
                );

        asylumCase.write(DIRECTIONS, allDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Parties resolvePartiesForHearingRequirements(AsylumCase asylumCase) {

        boolean isInternalDetainedNonAda = (isInternalCase(asylumCase) && isAppellantInDetention(asylumCase) && !isAcceleratedDetainedAppeal(asylumCase));
        boolean isEjpUnrepNonDetained = (isEjpCase(asylumCase) && !isAppellantInDetention(asylumCase) && !isLegallyRepresentedEjpCase(asylumCase));

        if (HandlerUtils.isAipJourney(asylumCase) || isInternalDetainedNonAda || isEjpUnrepNonDetained) {
            log.info("---------------APPELLANT");
            return Parties.APPELLANT;
        }
        log.info("---------------LEGAL_REPRESENTATIVE");
        return Parties.LEGAL_REPRESENTATIVE;
    }
}
