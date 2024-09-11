package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ACTUAL_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isIntegrated;

import java.util.Collections;
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
            maybePreviousHearings.orElse(Collections.emptyList());

        final Optional<String> attendingJudge = asylumCase.read(ATTENDING_JUDGE, String.class);

        final Optional<String> attendingAppellant = asylumCase.read(ATTENDING_APPELLANT, String.class);

        final Optional<String> attendingHomeOfficeLegalRepresentative = asylumCase.read(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE, String.class);

        final Optional<HoursAndMinutes> actualCaseHearingLength = asylumCase.read(ACTUAL_CASE_HEARING_LENGTH, HoursAndMinutes.class);

        final String ariaListingReference = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .orElseThrow(() -> new IllegalStateException("ariaListingReference is missing."));

        final HearingCentre listCaseHearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is missing."));

        final String listCaseHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is missing."));

        final String listCaseHearingLength = isIntegrated(asylumCase)
            ? asylumCase.read(LISTING_LENGTH, HoursMinutes.class)
            .map(listingLength -> String.valueOf(listingLength.convertToIntegerMinutes()))
            .orElseThrow(() -> new IllegalStateException("listingLength is missing."))
            : asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingLength is missing."));

        final String appealDecision = asylumCase.read(APPEAL_DECISION, String.class)
            .orElseThrow(() -> new IllegalStateException("appealDecision is missing."));

        Optional<List<IdValue<DocumentWithMetadata>>> maybeFinalDecisionAndReasonsDocuments =
            asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments =
            maybeFinalDecisionAndReasonsDocuments.orElse(emptyList());

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

        List<IdValue<PreviousHearing>> allPreviousHearings =
            previousHearingAppender.append(
                existingPreviousHearings,
                previousHearing);

        asylumCase.write(PREVIOUS_HEARINGS, allPreviousHearings);

        asylumCase.write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.NO);
    }
}
