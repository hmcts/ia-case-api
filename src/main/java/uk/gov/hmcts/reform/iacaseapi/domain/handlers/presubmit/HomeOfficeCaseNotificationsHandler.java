package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_NOTIFICATIONS_ELIGIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_SEARCH_STATUS;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@CacheConfig(cacheNames = {"caseId","event"})
@Component
@Slf4j
public class HomeOfficeCaseNotificationsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String SUPPRESSION_LOG_FIELDS = "event: {}, "
                                                         + "caseId: {}, "
                                                         + "homeOfficeReferenceNumber: {}, "
                                                         + "homeOfficeSearchStatus: {}, "
                                                         + "homeOfficeNotificationsEligible: {} ";
    private final FeatureToggler featureToggler;
    private final HomeOfficeApi<AsylumCase> homeOfficeApi;
    private Long cacheCaseId = 99999L;
    private String cachedEvent;

    private static final String HO_NOTIFICATION_FEATURE = "home-office-notification-feature";

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public HomeOfficeCaseNotificationsHandler(
        FeatureToggler featureToggler,
        HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.featureToggler = featureToggler;
        this.homeOfficeApi = homeOfficeApi;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithHomeOfficeData =
            callback
                .getCaseDetails()
                .getCaseData();

        AppealType appealType = asylumCaseWithHomeOfficeData.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

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

            // RIA-5683: Prevent multiple notifications for the same event and case ID.
            if (Objects.equals(cachedEvent, callback.getEvent().toString()) && cacheCaseId == caseId) {
                log.info("Home Office notification already invoked: "
                         + SUPPRESSION_LOG_FIELDS,
                        callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                        homeOfficeNotificationsEligible);
                return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
            }
            if ("SUCCESS".equalsIgnoreCase(homeOfficeSearchStatus)
                && homeOfficeNotificationsEligible == YesOrNo.YES) {

                log.info("Start: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS,
                    callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                    homeOfficeNotificationsEligible);

                asylumCaseWithHomeOfficeData = homeOfficeApi.aboutToSubmit(callback);

                log.info("Finish: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS,
                    callback.getEvent(), caseId, homeOfficeReferenceNumber, homeOfficeSearchStatus,
                    homeOfficeNotificationsEligible);
                // We store the case Id to prevent duplicated notifications.
                cacheCaseId = cacheCaseId(callback);
                cachedEvent = cacheEvent(callback);
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

        return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (isAHandleableEvent(callback.getEvent())

                        || (callback.getEvent() == Event.SEND_DIRECTION
                            && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE
                            && getLatestNonStandardRespondentDirection(
                                callback.getCaseDetails().getCaseData()).isPresent())

                        || (callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                            && (Arrays.asList(
                                State.AWAITING_RESPONDENT_EVIDENCE,
                                State.RESPONDENT_REVIEW
                            ).contains(callback.getCaseDetails().getState()))
                            && isDirectionForRespondentParties(callback.getCaseDetails().getCaseData()))
                    )
                    && featureToggler.getValue(HO_NOTIFICATION_FEATURE, false);
    }

    @Cacheable({"caseId"})
    public Long cacheCaseId(Callback callback) {
        long id = callback.getCaseDetails().getId();
        System.out.println(("Cached Case Id = {}" + id));
        return id;
    }

    @Cacheable({"event"})
    public String cacheEvent(Callback callback) {
        String event = callback.getEvent().toString();
        System.out.println(("Cached Event = {}" + event));
        return event;
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

    /*  Returns true when the callback event is handleable. */
    public boolean isAHandleableEvent(Event callbackEvent) {
        return Arrays.asList(
                Event.REQUEST_RESPONDENT_EVIDENCE,
                Event.REQUEST_RESPONDENT_REVIEW,
                Event.LIST_CASE,
                Event.EDIT_CASE_LISTING,
                Event.ADJOURN_HEARING_WITHOUT_DATE,
                Event.SEND_DECISION_AND_REASONS,
                Event.APPLY_FOR_FTPA_APPELLANT,
                Event.APPLY_FOR_FTPA_RESPONDENT,
                Event.LEADERSHIP_JUDGE_FTPA_DECISION,
                Event.RESIDENT_JUDGE_FTPA_DECISION,
                Event.END_APPEAL,
                Event.REQUEST_RESPONSE_AMEND).contains(callbackEvent);
    }
}
