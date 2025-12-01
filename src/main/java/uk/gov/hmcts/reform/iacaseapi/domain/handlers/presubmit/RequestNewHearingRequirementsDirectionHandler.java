package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isIntegrated;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousHearing;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousHearingAppender;

@Component
@Slf4j
public class RequestNewHearingRequirementsDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int hearingRequirementsDueInDays;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;
    private final PreviousHearingAppender previousHearingAppender;
    private final FeatureToggler featureToggler;

    public RequestNewHearingRequirementsDirectionHandler(
        @Value("${legalRepresentativeHearingRequirements.dueInDays}") int hearingRequirementsDueInDays,
        DateProvider dateProvider,
        DirectionAppender directionAppender,
        PreviousHearingAppender previousHearingAppender,
        FeatureToggler featureToggler
    ) {
        this.hearingRequirementsDueInDays = hearingRequirementsDueInDays;
        this.dateProvider = dateProvider;
        this.directionAppender = directionAppender;
        this.previousHearingAppender = previousHearingAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REQUEST_NEW_HEARING_REQUIREMENTS
               && featureToggler.getValue("reheard-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
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

        String defaultExplanation = "This appeal will be reheard. You should tell the Tribunal if the appellant’s hearing requirements have changed.\n\n"
                                    + "# Next steps\n\n"
                                    + "Visit the online service and use the HMCTS reference to find the case. Use the link on the overview tab to submit the appellant’s hearing requirements.\n\n"
                                    + "The Tribunal will review the hearing requirements and any requests for additional adjustments. You'll then be sent a hearing date.\n\n"
                                    + "If you do not submit the hearing requirements by the date indicated below, the Tribunal may not be able to accommodate the appellant's needs for the hearing.";

        Optional<String> explanation = Optional.of(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class).orElse(defaultExplanation));

        Optional<String> dueDate = Optional.of(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class).orElse(
            dateProvider
                .now()
                .plusDays(hearingRequirementsDueInDays)
                .toString()));

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                    asylumCase,
                existingDirections,
                explanation.get(),
                Parties.LEGAL_REPRESENTATIVE,
                dueDate.get(),
                DirectionTag.REQUEST_NEW_HEARING_REQUIREMENTS
            );

        asylumCase.write(DIRECTIONS, allDirections);
        asylumCase.clear(SEND_DIRECTION_EXPLANATION);
        asylumCase.clear(SEND_DIRECTION_PARTIES);
        asylumCase.clear(SEND_DIRECTION_DATE_DUE);

        writePreviousHearingsToAsylumCase(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    protected void writePreviousHearingsToAsylumCase(AsylumCase asylumCase) {

        Optional<List<IdValue<PreviousHearing>>> maybePreviousHearings =
            asylumCase.read(PREVIOUS_HEARINGS);

        final List<IdValue<PreviousHearing>> existingPreviousHearings =
            maybePreviousHearings.orElse(emptyList());

        final Optional<String> attendingJudge = asylumCase.read(ATTENDING_JUDGE, String.class);

        final Optional<String> attendingAppellant = asylumCase.read(ATTENDING_APPELLANT, String.class);

        final Optional<String> attendingHomeOfficeLegalRepresentative = asylumCase.read(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE, String.class);

        final Optional<HoursAndMinutes> actualCaseHearingLength = asylumCase.read(ACTUAL_CASE_HEARING_LENGTH, HoursAndMinutes.class);

        final HearingCentre listCaseHearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null);

        final boolean decisionWithoutHearing = asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)
                .map(yesOrNo -> YesOrNo.YES == yesOrNo).orElse(false);

        log.debug("listCaseHearingCentre value: " + listCaseHearingCentre);

        String listCaseHearingDate = null;
        String ariaListingReference = null;
        String listCaseHearingLength = null;

        if (listCaseHearingCentre != null && !listCaseHearingCentre.equals(HearingCentre.DECISION_WITHOUT_HEARING) && !decisionWithoutHearing) {
            listCaseHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
                    .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is missing."));

            if (isIntegrated(asylumCase)) {
                listCaseHearingLength = asylumCase.read(LISTING_LENGTH, HoursMinutes.class)
                        .map(listingLength -> String.valueOf(listingLength.convertToIntegerMinutes()))
                        .orElseThrow(() -> new IllegalStateException("listingLength is missing."));
            } else {
                ariaListingReference = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
                        .orElseThrow(() -> new IllegalStateException("ariaListingReference is missing."));

                listCaseHearingLength = asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingLength is missing."));
            }
        }

        final String appealDecision = asylumCase.read(APPEAL_DECISION, String.class)
            .orElseThrow(() -> new IllegalStateException("appealDecision is missing."));

        Optional<List<IdValue<DocumentWithMetadata>>> maybeFinalDecisionAndReasonsDocuments =
            asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments =
            maybeFinalDecisionAndReasonsDocuments.orElse(emptyList());

        log.debug("before previous hearing");

        final PreviousHearing previousHearing = new PreviousHearing(
            attendingJudge,
            attendingAppellant,
            attendingHomeOfficeLegalRepresentative,
            actualCaseHearingLength,
            ariaListingReference,
            listCaseHearingCentre,
            listCaseHearingDate,
            listCaseHearingLength,
            appealDecision,
            finalDecisionAndReasonsDocuments
        );

        log.debug("after previous hearing");

        List<IdValue<PreviousHearing>> allPreviousHearings =
            previousHearingAppender.append(
                existingPreviousHearings,
                previousHearing);

        asylumCase.write(PREVIOUS_HEARINGS, allPreviousHearings);

        log.debug("after write hearing");

        asylumCase.write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.NO);
    }
}
