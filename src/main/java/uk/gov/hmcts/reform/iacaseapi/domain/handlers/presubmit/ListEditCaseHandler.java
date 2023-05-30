package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;


@Component
public class ListEditCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final CaseManagementLocationService caseManagementLocationService;
    private final int dueInDaysSinceSubmission;
    private final DirectionAppender directionAppender;

    public ListEditCaseHandler(HearingCentreFinder hearingCentreFinder,
                               CaseManagementLocationService caseManagementLocationService,
                               @Value("${adaCaseListedDirection.dueInDaysSinceSubmission}")  int dueInDaysSinceSubmission,
                               DirectionAppender directionAppender) {
        this.hearingCentreFinder = hearingCentreFinder;
        this.caseManagementLocationService = caseManagementLocationService;
        this.dueInDaysSinceSubmission = dueInDaysSinceSubmission;
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.LIST_CASE || callback.getEvent() == Event.EDIT_CASE_LISTING);
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

        HearingCentre listCaseHearingCentre =
            asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.NEWPORT);

        HearingCentre hearingCentre =
            asylumCase.read(HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.NEWPORT);

        if (!listCaseHearingCentre.equals(HearingCentre.REMOTE_HEARING)) {
            if (!hearingCentreFinder.hearingCentreIsActive(listCaseHearingCentre)) {
                asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
                asylumCase.write(HEARING_CENTRE, HearingCentre.NEWPORT);
            } else {
                if (!hearingCentreFinder.isListingOnlyHearingCentre(listCaseHearingCentre)) {
                    //Should also update the designated hearing centre
                    asylumCase.write(HEARING_CENTRE, listCaseHearingCentre);
                }
            }
        } else {
            if (!hearingCentreFinder.hearingCentreIsActive(hearingCentre)) {
                asylumCase.write(HEARING_CENTRE, HearingCentre.NEWPORT);
            }
        }

        asylumCase.write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        asylumCase.clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        asylumCase.clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);
        asylumCase.clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
        asylumCase.clear(ATTENDING_TCW);
        asylumCase.clear(ATTENDING_JUDGE);
        asylumCase.clear(ATTENDING_APPELLANT);
        asylumCase.clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        asylumCase.clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        asylumCase.clear(ACTUAL_CASE_HEARING_LENGTH);
        asylumCase.clear(HEARING_CONDUCTION_OPTIONS);
        asylumCase.clear(HEARING_RECORDING_DOCUMENTS);
        asylumCase.clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
        addBaseLocationAndStaffLocationFromHearingCentre(asylumCase);

        boolean isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);

        if (isAcceleratedDetainedAppeal) {
            asylumCase.write(ACCELERATED_DETAINED_APPEAL_LISTED, YesOrNo.YES);

            // Set flag for first submission of hearing requirements' event for ADA
            asylumCase.write(ADA_HEARING_REQUIREMENTS_SUBMITTABLE, YesOrNo.YES);

            //Enable editCaseListing event for ADA
            asylumCase.write(ADA_EDIT_LISTING_AVAILABLE, YesOrNo.YES);

            // reset flag that makes ListCase available for accelerated detained appeals in
            // awaitingRespondentEvidence
            asylumCase.write(LISTING_AVAILABLE_FOR_ADA, YesOrNo.NO);

            addDirection(asylumCase);
        }


        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addBaseLocationAndStaffLocationFromHearingCentre(AsylumCase asylumCase) {
        HearingCentre hearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class)
            .orElse(HearingCentre.TAYLOR_HOUSE);
        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION,
            caseManagementLocationService.getCaseManagementLocation(staffLocationName));
    }

    private AsylumCase addDirection(AsylumCase asylumCase) {

        LocalDate appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)
            .map(LocalDate::parse)
            .orElseThrow(() -> new IllegalStateException("appealSubmissionDate is missing"));

        LocalDate directionDueDate = appealSubmissionDate
            .plusDays(dueInDaysSinceSubmission); // 15

        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);

        final List<IdValue<Direction>> existingDirections =
            maybeDirections.orElse(emptyList());

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                asylumCase,
                existingDirections,
                "You have a direction for this case.\n"
                + "\n"
                + "The accelerated detained appeal has been listed and you should tell the Tribunal if the appellant has any hearing requirements.\n"
                + "\n"
                + "# Next steps\n"
                + "Log in to the service and select the case from your case list. You’ll be able to submit the hearing requirements by selecting Submit hearing requirements from the Next step dropdown on the overview tab.\n"
                + "\n"
                + "The Tribunal will review the hearing requirements and any requests for additional adjustments.\n"
                + "\n"
                + "If you do not submit the hearing requirements by the date indicated below, the Tribunal may not be able to accommodate the appellant’s needs for the hearing.",
                Parties.LEGAL_REPRESENTATIVE,
                directionDueDate.toString(),
                DirectionTag.ADA_LIST_CASE,
                Event.LIST_CASE.toString()
            );

        asylumCase.write(DIRECTIONS, allDirections);

        return asylumCase;
    }
}
