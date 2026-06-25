package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@Component
@Slf4j
public class HomeOfficeCaseNotificationsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String SUPPRESSION_LOG_FIELDS = "event: {}, "
                                                         + "caseId: {}, "
                                                         + "homeOfficeReferenceNumber: {}, "
                                                         + "homeOfficeSearchStatus: {}, "
                                                         + "homeOfficeNotificationsEligible: {} ";

    private static final String SUPPRESSION_LOG_FIELDS_NEW = "event: {}, "
                                                         + "CCD case ID: {}, "
                                                         + "HMCTS appeal ref: {}, "
                                                         + "Home Office reference no: {}, "
                                                         + "Home Office API response code: {}";
    private final FeatureToggler featureToggler;
    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    public HomeOfficeCaseNotificationsHandler(
        FeatureToggler featureToggler,
        HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.featureToggler = featureToggler;
        this.homeOfficeApi = homeOfficeApi;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = Lists.newArrayList(
            Event.REQUEST_RESPONDENT_EVIDENCE,
            Event.REQUEST_RESPONDENT_REVIEW,
            Event.LIST_CASE,
            Event.ADJOURN_HEARING_WITHOUT_DATE,
            Event.SEND_DECISION_AND_REASONS,
            Event.APPLY_FOR_FTPA_APPELLANT,
            Event.APPLY_FOR_FTPA_RESPONDENT,
            Event.LEADERSHIP_JUDGE_FTPA_DECISION,
            Event.RESIDENT_JUDGE_FTPA_DECISION,
            Event.END_APPEAL,
            Event.REQUEST_RESPONSE_AMEND,
            Event.DECIDE_FTPA_APPLICATION);

        if (notifyHomeOfficeOnEditCaseListingEvent(callback)) {
            targetEvents.add(Event.EDIT_CASE_LISTING);
        }

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (targetEvents.contains(callback.getEvent())
               || (callback.getEvent() == Event.SEND_DIRECTION
                   && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE
                   && getLatestNonStandardRespondentDirection(
                        callback.getCaseDetails().getCaseData()).isPresent())

               || (callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                   && (Arrays.asList(
                        State.AWAITING_RESPONDENT_EVIDENCE,
                        State.RESPONDENT_REVIEW
                        ).contains(callback.getCaseDetails().getState()))
                   && isDirectionForRespondentParties(callback.getCaseDetails().getCaseData())
                  )
               )
               && !HandlerUtils.isNotificationTurnedOff(callback.getCaseDetails().getCaseData());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithHomeOfficeData = callback.getCaseDetails().getCaseData();

        AppealType appealType = asylumCaseWithHomeOfficeData.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        // Check whether the new  applications/v1/{id}  Home Office endpoint has already been called
        if (asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class).isPresent()) {
            // Always proceed if the new  applications/v1/{id}  Home Office endpoint has already been called

            // Retrieve the UAN or GWF from the case record
            final String homeOfficeReferenceNumber = HandlerUtils.getUanOrGwf(asylumCaseWithHomeOfficeData);
            if (homeOfficeReferenceNumber.isEmpty()) {
                throw new IllegalStateException("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed");
            }
            // Ensure this is present before calling the Home Office API (where it will be needed)
            final String appealReferenceNumber = asylumCaseWithHomeOfficeData.read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));
            // Details for logging purposes only
            final String homeOfficeAppellantApiResponseStatus = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, String.class)
                .orElse("");
            final long caseId = callback.getCaseDetails().getId();

            log.info("Start: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS_NEW,
                callback.getEvent(), caseId, appealReferenceNumber, homeOfficeReferenceNumber, homeOfficeAppellantApiResponseStatus);

            asylumCaseWithHomeOfficeData = homeOfficeApi.aboutToSubmit(callback);

            log.info("Finish: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS_NEW,
                callback.getEvent(), caseId, appealReferenceNumber, homeOfficeReferenceNumber, homeOfficeAppellantApiResponseStatus);

        } else {
            // For older cases, only proceed if various restrictions on the case have been met
            // (feature-flags set, in-country, old validation API endpoint returned SUCCESS and so on)
            if (!HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType)) {
                return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
            }

            final String homeOfficeSearchStatus = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_SEARCH_STATUS, String.class)
                .orElse("");
            final YesOrNo homeOfficeNotificationsEligible
                = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)
                .orElse(YesOrNo.NO);
            final long caseId = callback.getCaseDetails().getId();
            final String homeOfficeReferenceNumber
                = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse("");

            if (asylumCaseWithHomeOfficeData.read(APPELLANT_IN_UK, YesOrNo.class).map(
                value -> value.equals(YesOrNo.YES)).orElse(true)) {

                if ("SUCCESS".equalsIgnoreCase(homeOfficeSearchStatus)
                    && homeOfficeNotificationsEligible == YesOrNo.YES) {

                    log.info("Start: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS,
                        callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                        homeOfficeNotificationsEligible);

                    asylumCaseWithHomeOfficeData = homeOfficeApi.aboutToSubmit(callback);

                    log.info("Finish: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS,
                        callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                        homeOfficeNotificationsEligible);
                } else {

                    log.info("Home Office notification was NOT invoked due to unsuccessful validation search - "
                            + SUPPRESSION_LOG_FIELDS,
                        callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                        homeOfficeNotificationsEligible);

                }
            } else {
                log.info("Home Office notification was NOT invoked as Appellant is NOT in the UK - "
                        + SUPPRESSION_LOG_FIELDS,
                    callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                    homeOfficeNotificationsEligible);

            }
        }

        return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
    }

    protected Optional<Direction> getLatestNonStandardRespondentDirection(AsylumCase asylumCase) {

        Optional<List<IdValue<Direction>>> maybeExistingDirections = asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS);

        return maybeExistingDirections
            .orElseThrow(() -> new IllegalStateException("directions not present"))
            .stream()
            .max(Comparator.comparingInt(s -> Integer.parseInt(s.getId())))
            .filter(idValue -> idValue.getValue().getTag().equals(DirectionTag.NONE))
            .filter(idValue -> idValue.getValue().getParties().equals(Parties.RESPONDENT))
            .map(IdValue::getValue);
    }

    protected boolean isDirectionForRespondentParties(AsylumCase asylumCase) {

        Parties parties = asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_EDIT_PARTIES, Parties.class)
            .orElseThrow(() -> new IllegalStateException("sendDirectionParties is not present"));

        return parties.equals(Parties.RESPONDENT);

    }

    private boolean notifyHomeOfficeOnEditCaseListingEvent(Callback<AsylumCase> callback) {
        // Home office is not notified if the update is remote to remote hearing channel update
        // (VID to TEL or TEL to VID)
        return !HandlerUtils.isOnlyRemoteToRemoteHearingChannelUpdate(callback);
    }
}
