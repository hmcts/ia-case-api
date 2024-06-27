package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_APPLICATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isNotificationTurnedOff;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;


@Component
public class RecordApplicationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final List<State> editListingStates = newArrayList(
        State.PREPARE_FOR_HEARING,
        State.FINAL_BUNDLING,
        State.PRE_HEARING,
        State.DECISION
    );

    private final List<State> timeExtensionStates = newArrayList(
        State.AWAITING_RESPONDENT_EVIDENCE,
        State.CASE_BUILDING,
        State.CASE_UNDER_REVIEW,
        State.RESPONDENT_REVIEW,
        State.SUBMIT_HEARING_REQUIREMENTS
    );

    private final List<State> updateHearingRequirementsStates = newArrayList(
        State.PRE_HEARING,
        State.FINAL_BUNDLING,
        State.PREPARE_FOR_HEARING,
        State.DECISION
    );

    private final List<State> changeHearingCentreStates = newArrayList(
        State.APPEAL_SUBMITTED,
        State.AWAITING_RESPONDENT_EVIDENCE,
        State.CASE_BUILDING,
        State.CASE_UNDER_REVIEW,
        State.RESPONDENT_REVIEW,
        State.SUBMIT_HEARING_REQUIREMENTS,
        State.LISTING
    );

    private final List<State> editAppealApplicationStates = newArrayList(
        State.APPEAL_SUBMITTED,
        State.AWAITING_RESPONDENT_EVIDENCE,
        State.CASE_BUILDING,
        State.CASE_UNDER_REVIEW,
        State.RESPONDENT_REVIEW,
        State.SUBMIT_HEARING_REQUIREMENTS,
        State.LISTING,
        State.PREPARE_FOR_HEARING,
        State.FINAL_BUNDLING,
        State.PRE_HEARING
    );


    private final Appender<Application> appender;
    private final DateProvider dateProvider;
    private final NotificationSender<AsylumCase> notificationSender;

    public RecordApplicationHandler(
        Appender<Application> appender,
        DateProvider dateProvider,
        NotificationSender<AsylumCase> notificationSender
    ) {
        this.appender = appender;
        this.dateProvider = dateProvider;
        this.notificationSender = notificationSender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(RECORD_APPLICATION);
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

        String applicationSupplier = asylumCase
            .read(APPLICATION_SUPPLIER, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationSupplier is not present"));

        String applicationType = asylumCase
            .read(APPLICATION_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationType is not present"));

        String applicationReason = asylumCase
            .read(APPLICATION_REASON, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationReason is not present"));

        String applicationDate = asylumCase
            .read(APPLICATION_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationDate is not present"));

        String applicationDecision = asylumCase
            .read(APPLICATION_DECISION, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationDecision is not present"));

        String applicationDecisionReason = asylumCase
            .read(APPLICATION_DECISION_REASON, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationDecisionReason is not present"));

        Optional<List<IdValue<Document>>> maybeApplicationDocuments = asylumCase
            .read(APPLICATION_DOCUMENTS);

        Optional<List<IdValue<Application>>> maybeExistingApplictions =
            asylumCase.read(APPLICATIONS);

        final Application newApplication = new Application(
            maybeApplicationDocuments.orElseThrow(() -> new IllegalStateException("applicationDocuments is not present")),
            applicationSupplier,
            applicationType,
            applicationReason,
            applicationDate,
            applicationDecision,
            applicationDecisionReason,
            dateProvider.now().toString(),
            GRANTED.toString().equalsIgnoreCase(applicationDecision) ? "In progress" : "Completed"
        );

        State currentState = callback.getCaseDetails().getState();

        if (!isAllowedToRecordApplication(applicationType, currentState)) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("You can't record application with '" + applicationType + "' type when case is in '" + currentState.name() + "' state");

            return response;
        }

        List<IdValue<Application>> allApplications =
            appender.append(newApplication, maybeExistingApplictions.orElse(emptyList()));

        if (GRANTED.toString().equalsIgnoreCase(applicationDecision)) {

            if (WITHDRAW.toString().equalsIgnoreCase(applicationType)) {

                // withdraw take precedence on time extension or edit listing
                asylumCase.write(APPLICATION_WITHDRAW_EXISTS, "Yes");
                asylumCase.write(APPLICATION_TIME_EXTENSION_EXISTS, "No");
                asylumCase.write(APPLICATION_EDIT_LISTING_EXISTS, "No");

            } else if (TIME_EXTENSION.toString().equalsIgnoreCase(applicationType)) {

                if (!asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class).isPresent()) {
                    asylumCase.write(APPLICATION_TIME_EXTENSION_EXISTS, "Yes");
                }
            } else if (ADJOURN.toString().equalsIgnoreCase(applicationType)
                || EXPEDITE.toString().equalsIgnoreCase(applicationType)
                || TRANSFER.toString().equalsIgnoreCase(applicationType)) {

                if (!asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class).isPresent()) {
                    asylumCase.write(APPLICATION_EDIT_LISTING_EXISTS, "Yes");
                }
            } else if (UPDATE_HEARING_REQUIREMENTS.toString().equalsIgnoreCase(applicationType)) {
                if (!asylumCase.read(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS, String.class).isPresent()) {
                    asylumCase.write(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS, "Yes");
                }
            } else if (CHANGE_HEARING_CENTRE.toString().equalsIgnoreCase(applicationType)
                && !asylumCase.read(APPLICATION_CHANGE_HEARING_CENTRE_EXISTS, String.class).isPresent()) {
                asylumCase.write(APPLICATION_CHANGE_HEARING_CENTRE_EXISTS, "Yes");
            } else if (EDIT_APPEAL_AFTER_SUBMIT.toString().equalsIgnoreCase(applicationType)
                && !asylumCase.read(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS, String.class).isPresent()) {
                asylumCase.write(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS, "Yes");
            }

            asylumCase.write(DISABLE_OVERVIEW_PAGE, "Yes");
            asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
        }

        asylumCase.write(APPLICATIONS, allApplications);

        // call ia-case-notification-api in this handler because we need to do some cleanups after call and use above validation
        AsylumCase asylumCaseWithNotificationMarker = isNotificationTurnedOff(asylumCase)
                ? asylumCase : notificationSender.send(
            new Callback<>(
                new CaseDetails<>(
                    callback.getCaseDetails().getId(),
                    callback.getCaseDetails().getJurisdiction(),
                    callback.getCaseDetails().getState(),
                    asylumCase,
                    callback.getCaseDetails().getCreatedDate(),
                    callback.getCaseDetails().getSecurityClassification(),
                    callback.getCaseDetails().getSupplementaryData()
                ),
                callback.getCaseDetailsBefore(),
                callback.getEvent()
            )
        );

        asylumCaseWithNotificationMarker.clear(APPLICATION_SUPPLIER);
        asylumCaseWithNotificationMarker.clear(APPLICATION_REASON);
        asylumCaseWithNotificationMarker.clear(APPLICATION_DATE);
        asylumCaseWithNotificationMarker.clear(APPLICATION_DECISION_REASON);
        asylumCaseWithNotificationMarker.clear(APPLICATION_DOCUMENTS);

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }

    private boolean isAllowedToRecordApplication(String applicationType, State state) {

        if (TIME_EXTENSION.toString().equalsIgnoreCase(applicationType)) {

            if (timeExtensionStates.contains(state)) {
                return true;
            } else {
                return false;
            }
        } else if (ADJOURN.toString().equalsIgnoreCase(applicationType)
                   || EXPEDITE.toString().equalsIgnoreCase(applicationType)
                   || TRANSFER.toString().equalsIgnoreCase(applicationType)) {

            if (editListingStates.contains(state)) {
                return true;
            } else {
                return false;
            }
        } else if (UPDATE_HEARING_REQUIREMENTS.toString().equalsIgnoreCase(applicationType)) {
            if (!updateHearingRequirementsStates.contains(state)) {
                return false;
            } else {
                return true;
            }
        } else if (CHANGE_HEARING_CENTRE.toString().equalsIgnoreCase(applicationType)) {
            return changeHearingCentreStates.contains(state);
        } else if (EDIT_APPEAL_AFTER_SUBMIT.toString().equalsIgnoreCase(applicationType)) {
            return editAppealApplicationStates.contains(state);
        }

        return true;
    }
}
