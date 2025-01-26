package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ACTUAL_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_TCW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DOES_THE_CASE_NEED_TO_BE_RELISTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CONDUCTION_OPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEWED_UPDATED_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
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
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@Slf4j
@Component
public class ListEditCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final CaseManagementLocationService caseManagementLocationService;
    private final LocationRefDataService locationRefDataService;
    private final int dueInDaysSinceSubmission;
    private final DirectionAppender directionAppender;
    private final NextHearingDateService nextHearingDateService;
    private final HearingIdListProcessor hearingIdListProcessor;

    public ListEditCaseHandler(
        HearingCentreFinder hearingCentreFinder,
        CaseManagementLocationService caseManagementLocationService,
        @Value("${adaCaseListedDirection.dueInDaysSinceSubmission}")  int dueInDaysSinceSubmission,
        DirectionAppender directionAppender,
        LocationRefDataService locationRefDataService,
        NextHearingDateService nextHearingDateService,
        HearingIdListProcessor hearingIdListProcessor
    ) {
        this.hearingCentreFinder = hearingCentreFinder;
        this.caseManagementLocationService = caseManagementLocationService;
        this.dueInDaysSinceSubmission = dueInDaysSinceSubmission;
        this.directionAppender = directionAppender;
        this.locationRefDataService = locationRefDataService;
        this.nextHearingDateService = nextHearingDateService;
        this.hearingIdListProcessor = hearingIdListProcessor;
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isCaseUsingLocationRefData(asylumCase)) {
            DynamicList listingLocation = asylumCase.read(LISTING_LOCATION, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("Listing location is missing"));
            String listingLocationId = listingLocation.getValue().getCode();

            HearingCentre listCaseHearingCentre = HearingCentre
                .fromEpimsId(listingLocationId, true)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No Hearing Centre found for Listing location with Epimms ID: %s",
                        listingLocationId)));
            asylumCase.write(LIST_CASE_HEARING_CENTRE, listCaseHearingCentre);

            if (locationRefDataService.isCaseManagementLocation(listingLocationId)) {
                HearingCentre hearingCentre = HearingCentre.fromEpimsId(listingLocationId, false)
                    .orElseThrow(() -> new IllegalStateException(
                        String.format("No Hearing Centre found for Listing location with Epimms ID: %s",
                            listingLocationId)));

                Optional<DynamicList> optionalHearingCentreDynamicList =
                    asylumCase.read(HEARING_CENTRE_DYNAMIC_LIST, DynamicList.class);

                if (optionalHearingCentreDynamicList.isPresent()) {
                    DynamicList hearingCentreDynamicList = optionalHearingCentreDynamicList.get();
                    optionalHearingCentreDynamicList.get().getListItems().stream()
                        .filter(value -> Objects.equals(value.getCode(), hearingCentre.getEpimsId()))
                        .findFirst().ifPresent(hearingCentreDynamicList::setValue);

                    asylumCase.write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
                }

                HandlerUtils.setSelectedHearingCentreRefDataField(asylumCase, listingLocation.getValue().getLabel());
                asylumCase.write(HEARING_CENTRE, hearingCentre);
                addBaseLocationAndStaffLocation(asylumCase, hearingCentre);
            }

            asylumCase.write(LIST_CASE_HEARING_CENTRE_ADDRESS, locationRefDataService
                .getHearingCentreAddress(listingLocation.getValue().getCode()));

            asylumCase.clear(IS_DECISION_WITHOUT_HEARING);


        } else {
            HearingCentre listCaseHearingCentre =
                asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.NEWPORT);
            HearingCentre hearingCentre =
                asylumCase.read(HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.NEWPORT);

            if (listCaseHearingCentre.equals(HearingCentre.REMOTE_HEARING)) {
                if (!hearingCentreFinder.hearingCentreIsActive(hearingCentre)) {
                    asylumCase.write(HEARING_CENTRE, HearingCentre.NEWPORT);
                }
            } else if (!hearingCentreFinder.hearingCentreIsActive(listCaseHearingCentre)) {
                asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
                asylumCase.write(HEARING_CENTRE, HearingCentre.NEWPORT);
            } else if (!hearingCentreFinder.isListingOnlyHearingCentre(listCaseHearingCentre)) {
                //Should also update the designated hearing centre
                asylumCase.write(HEARING_CENTRE, listCaseHearingCentre);
            }

            asylumCase.write(LIST_CASE_HEARING_CENTRE_ADDRESS, locationRefDataService
                .getHearingCentreAddress(listCaseHearingCentre));

            addBaseLocationAndStaffLocation(asylumCase);
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

            if (callback.getEvent() == Event.LIST_CASE) {
                addDirection(asylumCase);
            }
        }

        if (nextHearingDateService.enabled()) {
            log.debug("Next hearing date feature enabled");
            if (HandlerUtils.isIntegrated(asylumCase)) {
                asylumCase.write(NEXT_HEARING_DETAILS,
                    nextHearingDateService.calculateNextHearingDateFromHearings(callback));
            } else {
                asylumCase.write(NEXT_HEARING_DETAILS,
                    nextHearingDateService.calculateNextHearingDateFromCaseData(callback));
            }
        } else {
            log.debug("Next hearing date feature not enabled");
        }

        hearingIdListProcessor.processHearingIdList(callback, asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addBaseLocationAndStaffLocation(AsylumCase asylumCase) {

        HearingCentre hearingCentre = asylumCase
            .read(HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.TAYLOR_HOUSE);
        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION,
            caseManagementLocationService.getCaseManagementLocation(staffLocationName));
    }

    private void addBaseLocationAndStaffLocation(AsylumCase asylumCase, HearingCentre hearingCentre) {

        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION_REF_DATA,
            caseManagementLocationService.getRefDataCaseManagementLocation(staffLocationName));
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
                resolvePartiesForListCase(asylumCase),
                directionDueDate.toString(),
                DirectionTag.ADA_LIST_CASE,
                Event.LIST_CASE.toString()
            );

        asylumCase.write(DIRECTIONS, allDirections);

        return asylumCase;
    }

    private Parties resolvePartiesForListCase(AsylumCase asylumCase) {
        if (isInternalCase(asylumCase) && isAppellantInDetention(asylumCase)) {
            return Parties.APPELLANT;
        }
        return Parties.LEGAL_REPRESENTATIVE;
    }
}
