package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSE_PERMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEjpCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@Component
public class EditAppealAfterSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final int appealOutOfTimeDaysUk;
    private final int appealOutOfTimeDaysOoc;
    private final int appealOutOfTimeAcceleratedDetainedWorkingDays;

    private final DueDateService dueDateService;
    private static final String HOME_OFFICE_DECISION_PAGE_ID = "homeOfficeDecision";

    public EditAppealAfterSubmitHandler(
        DateProvider dateProvider,
        DueDateService dueDateService,
        @Value("${appealOutOfTimeDaysUk}") int appealOutOfTimeDaysUk,
        @Value("${appealOutOfTimeDaysOoc}") int appealOutOfTimeDaysOoc,
        @Value("${appealOutOfTimeAcceleratedDetainedWorkingDays}") int appealOutOfTimeAcceleratedDetainedWorkingDays
    ) {
        this.dateProvider = dateProvider;
        this.appealOutOfTimeDaysUk = appealOutOfTimeDaysUk;
        this.appealOutOfTimeDaysOoc = appealOutOfTimeDaysOoc;
        this.appealOutOfTimeAcceleratedDetainedWorkingDays = appealOutOfTimeAcceleratedDetainedWorkingDays;
        this.dueDateService = dueDateService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (((callbackStage == PreSubmitCallbackStage.MID_EVENT && callback.getPageId().equals(HOME_OFFICE_DECISION_PAGE_ID))
            || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
            && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT);
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

        Optional<OutOfCountryDecisionType> outOfCountryDecisionTypeOptional = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        YesOrNo appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).orElse(NO);

        if (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
                .orElse(NO) == YES) {

            handleAccelaratedDetainedAppeal(asylumCase);
            handleAdaSuitabilityFields(asylumCase);
        } else if (outOfCountryDecisionTypeOptional.isPresent()) {

            handleOutOfCountryAppeal(asylumCase, outOfCountryDecisionTypeOptional.get());
        } else if (HandlerUtils.isAgeAssessmentAppeal(asylumCase)) {

            handleInCountryAgeAssessmentAppeal(asylumCase);
            clearLitigationFriendDetails(asylumCase);
        } else if (isInternalCase(asylumCase) && appellantInUk.equals(NO)) {
            handleOutOfCountryAppeal(asylumCase, REFUSE_PERMIT);
        } else {
            handleInCountryAppeal(asylumCase);
        }

        if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
            changeEditAppealApplicationsToCompleted(asylumCase);
            asylumCase.clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
            State maybePreviousState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
                .orElse(State.UNKNOWN);
            asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, maybePreviousState);
            clearNewMatters(asylumCase);
            if (isInternalCase(asylumCase)) {
                clearLegalRepFields(asylumCase);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearNewMatters(AsylumCase asylumCase) {
        YesOrNo hasNewMatters = asylumCase.read(HAS_NEW_MATTERS, YesOrNo.class).orElse(NO);
        if (NO.equals(hasNewMatters)) {
            asylumCase.clear(NEW_MATTERS);
        }
    }

    private void clearLitigationFriendDetails(AsylumCase asylumCase) {
        YesOrNo hasLitigationFriend = asylumCase.read(LITIGATION_FRIEND, YesOrNo.class).orElse(NO);
        if (hasLitigationFriend.equals(NO)) {
            asylumCase.clear(LITIGATION_FRIEND_GIVEN_NAME);
            asylumCase.clear(LITIGATION_FRIEND_FAMILY_NAME);
            asylumCase.clear(LITIGATION_FRIEND_COMPANY);
            asylumCase.clear(LITIGATION_FRIEND_CONTACT_PREFERENCE);
            asylumCase.clear(LITIGATION_FRIEND_EMAIL);
            asylumCase.clear(LITIGATION_FRIEND_PHONE_NUMBER);
        } else if (hasLitigationFriend.equals(YES)) {
            asylumCase.read(LITIGATION_FRIEND_CONTACT_PREFERENCE, ContactPreference.class).ifPresent(
                    contactPreference -> {
                        if (contactPreference.equals(ContactPreference.WANTS_EMAIL)) {
                            asylumCase.clear(LITIGATION_FRIEND_PHONE_NUMBER);
                        } else {
                            asylumCase.clear(LITIGATION_FRIEND_EMAIL);
                        }
                    }
            );
        }
    }

    private void clearLegalRepFields(AsylumCase asylumCase) {
        YesOrNo appellantsRepresentation = asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class).orElse(NO);
        if (YES.equals(appellantsRepresentation)) {
            asylumCase.clear(APPEAL_WAS_NOT_SUBMITTED_REASON);
            asylumCase.clear(APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS);
            asylumCase.clear(LEGAL_REP_COMPANY_PAPER_J);
            asylumCase.clear(LEGAL_REP_GIVEN_NAME);
            asylumCase.clear(LEGAL_REP_FAMILY_NAME_PAPER_J);
            asylumCase.clear(LEGAL_REP_EMAIL);
            asylumCase.clear(LEGAL_REP_REF_NUMBER_PAPER_J);

            asylumCase.clear(LEGAL_REP_ADDRESS_U_K);
            asylumCase.clear(OOC_ADDRESS_LINE_1);
            asylumCase.clear(OOC_ADDRESS_LINE_2);
            asylumCase.clear(OOC_ADDRESS_LINE_3);
            asylumCase.clear(OOC_ADDRESS_LINE_4);
            asylumCase.clear(OOC_COUNTRY_LINE);
            asylumCase.clear(OOC_LR_COUNTRY_GOV_UK_ADMIN_J);
            asylumCase.clear(LEGAL_REP_HAS_ADDRESS);
        }
    }

    private void changeEditAppealApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.EDIT_APPEAL_AFTER_SUBMIT.toString().equals(applicationType)) {

                    return new IdValue<>(application.getId(), new Application(
                        application.getValue().getApplicationDocuments(),
                        application.getValue().getApplicationSupplier(),
                        applicationType,
                        application.getValue().getApplicationReason(),
                        application.getValue().getApplicationDate(),
                        application.getValue().getApplicationDecision(),
                        application.getValue().getApplicationDecisionReason(),
                        application.getValue().getApplicationDateOfDecision(),
                        "Completed"
                    ));
                }

                return application;
            })
            .collect(Collectors.toList())
        );
    }

    private void handleAccelaratedDetainedAppeal(AsylumCase asylumCase) {
        Optional<String> homeOfficeDecisionLetterDateOptional = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);
        LocalDate decisionDate =
            parse(homeOfficeDecisionLetterDateOptional
                .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));

        ZonedDateTime dueDateTime = dueDateService.calculateDueDate(decisionDate.atStartOfDay(ZoneOffset.UTC), appealOutOfTimeAcceleratedDetainedWorkingDays);

        if (dueDateTime != null && dueDateTime.toLocalDate().isBefore(dateProvider.now())) {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
            asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, NO);
        } else {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
            asylumCase.clear(RECORDED_OUT_OF_TIME_DECISION);
        }
    }

    private void handleOutOfCountryAppeal(AsylumCase asylumCase, OutOfCountryDecisionType decisionType) {
        LocalDate decisionDate;

        switch (decisionType) {
            case REMOVAL_OF_CLIENT :
                Optional<String> homeOfficeDecisionLetterDateOptional = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);
                decisionDate =
                    parse(homeOfficeDecisionLetterDateOptional
                        .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));
                break;
            case REFUSAL_OF_PROTECTION :
                Optional<String> dateClientLeaveUkOptional = asylumCase.read(DATE_CLIENT_LEAVE_UK);

                decisionDate =
                    parse(dateClientLeaveUkOptional
                        .orElseThrow(() -> new RequiredFieldMissingException("dateClientLeaveUk is not present")));
                break;
            case REFUSAL_OF_HUMAN_RIGHTS:
                Optional<String> dateEntryClearanceDecisionOptional = asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION);

                decisionDate =
                    parse(dateEntryClearanceDecisionOptional
                        .orElseThrow(() -> new RequiredFieldMissingException("dateEntryClearanceDecision is not present")));
                break;
            default:
                decisionDate = null;
        }

        if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS || decisionType == OutOfCountryDecisionType.REFUSE_PERMIT) {
            asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER).orElse(asylumCase.read(GWF_REFERENCE_NUMBER).orElse(null)));
        }

        if (decisionDate != null
            && decisionDate.isBefore(dateProvider.now().minusDays(appealOutOfTimeDaysOoc))) {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
        } else {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
            asylumCase.clear(RECORDED_OUT_OF_TIME_DECISION);
        }
    }

    private void handleInCountryAgeAssessmentAppeal(AsylumCase asylumCase) {

        Optional<String> dateOnDecisionLetterOptional = asylumCase.read(DATE_ON_DECISION_LETTER, String.class);
        LocalDate decisionDate = parse(dateOnDecisionLetterOptional.orElseThrow(() -> new RequiredFieldMissingException("dateOnDecisionLetter is not present")));

        if (decisionDate.isBefore(dateProvider.now().minusDays(appealOutOfTimeDaysUk))) {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
        } else {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
            asylumCase.clear(RECORDED_OUT_OF_TIME_DECISION);
        }
    }

    private void handleInCountryAppeal(AsylumCase asylumCase) {

        Optional<String> homeOfficeDecisionDateOptional = asylumCase.read(HOME_OFFICE_DECISION_DATE);

        if (!isEjpCase(asylumCase)) {
            LocalDate decisionDate =
                parse(homeOfficeDecisionDateOptional
                    .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is missing")));
            if (decisionDate.isBefore(dateProvider.now().minusDays(appealOutOfTimeDaysUk))) {
                asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
            } else {
                asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
                asylumCase.clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
                asylumCase.clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
                asylumCase.clear(RECORDED_OUT_OF_TIME_DECISION);
            }
        }
    }

    private void handleAdaSuitabilityFields(AsylumCase asylumCase) {

        YesOrNo adaSuitabilityHearingType = asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class).orElse(NO);
        YesOrNo suitabilityAppellantAttendanceYesOrNo1 = asylumCase.read(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1, YesOrNo.class).orElse(NO);
        YesOrNo suitabilityAppellantAttendanceYesOrNo2 = asylumCase.read(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2, YesOrNo.class).orElse(NO);

        if (adaSuitabilityHearingType.equals(YES)) {
            asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
        } else {
            asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
        }

        // For either attendance value edited to 'No', clear interpreter screen fields
        // Midevent will write old value to No by default
        if ((suitabilityAppellantAttendanceYesOrNo1.equals(NO) && suitabilityAppellantAttendanceYesOrNo2.equals(NO))) {
            asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
            asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
        }
    }
}
