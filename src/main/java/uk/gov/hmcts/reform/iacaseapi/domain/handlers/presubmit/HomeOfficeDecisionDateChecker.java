package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEjpCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@Component
public class HomeOfficeDecisionDateChecker implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final int appealOutOfTimeDaysUk;
    private final int appealOutOfTimeDaysOoc;
    private final int appealOutOfTimeAcceleratedDetainedWorkingDays;

    private final DueDateService dueDateService;

    public HomeOfficeDecisionDateChecker(
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

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && Event.SUBMIT_APPEAL.equals(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<OutOfCountryDecisionType> outOfCountryDecisionTypeOptional = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        Optional<OutOfCountryCircumstances> outOfCountryCircumstances = asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class);

        // If case data has oocDecisionType && ADA=YES, it means appeal was moving from in-country to OOC
        if (HandlerUtils.isAcceleratedDetainedAppeal(asylumCase) && outOfCountryDecisionTypeOptional.isEmpty()) {
            handleAdaAppeal(asylumCase);
        } else if (isEjpCase(callback.getCaseDetails().getCaseData())) {
            //For EJP cases, Out of time is always NO
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
        } else if (!HandlerUtils.isAipJourney(asylumCase)) {
            boolean isOutOfCountry = outOfCountryDecisionTypeOptional.isPresent();
            boolean isOutOfCountryCircumstances = outOfCountryCircumstances.isPresent();

            AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Appeal type is missing"));
            LocalDate decisionDate;

            if (isInternalCase(asylumCase)) {
                decisionDate = isOutOfCountryCircumstances
                    ? handleOutOfCountryInternalAppeal(asylumCase, outOfCountryCircumstances.get())
                    : handleInCountryAppeal(asylumCase, appealType);
            } else {
                decisionDate = isOutOfCountry
                    ? handleOutOfCountryAppeal(asylumCase, outOfCountryDecisionTypeOptional.get())
                    : handleInCountryAppeal(asylumCase, appealType);
            }

            if (isDecisionDateBeforeAppealOutOfTimeDate(decisionDate, isOutOfCountry, isOutOfCountryCircumstances)) {
                asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
                asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, NO);
            } else {
                asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            }
            if ((isOutOfCountry) &&
                (outOfCountryDecisionTypeOptional.get() == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS
                 || outOfCountryDecisionTypeOptional.get() == OutOfCountryDecisionType.REFUSE_PERMIT)) {
                asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER).orElse(asylumCase.read(GWF_REFERENCE_NUMBER).orElse(null)));
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void handleAdaAppeal(AsylumCase asylumCase) {
        Optional<String> homeOfficeDecisionLetterDateOptional = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);
        if (homeOfficeDecisionLetterDateOptional.isEmpty()) {
            throw new RequiredFieldMissingException("decisionLetterReceivedDate is not present");
        }
        LocalDate homeOfficeDecisionDate =
            parse(homeOfficeDecisionLetterDateOptional.get());

        ZonedDateTime dueDateTime = dueDateService.calculateDueDate(homeOfficeDecisionDate.atStartOfDay(ZoneOffset.UTC), appealOutOfTimeAcceleratedDetainedWorkingDays);

        if (dueDateTime != null && dueDateTime.toLocalDate().isBefore(dateProvider.now())) {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
            asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, NO);
        } else {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
        }
    }

    private LocalDate handleOutOfCountryAppeal(AsylumCase asylumCase, OutOfCountryDecisionType outOfCountryDecisionType) {
        switch (outOfCountryDecisionType) {
            case REMOVAL_OF_CLIENT:
                Optional<String> homeOfficeDecisionLetterDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);
                return parse(homeOfficeDecisionLetterDate
                    .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));

            case REFUSAL_OF_PROTECTION:
                Optional<String> dateClientLeaveUk = asylumCase.read(DATE_CLIENT_LEAVE_UK);
                return parse(dateClientLeaveUk
                    .orElseThrow(() -> new RequiredFieldMissingException("dateClientLeaveUk is not present")));

            case REFUSAL_OF_HUMAN_RIGHTS:
            case REFUSE_PERMIT:
                Optional<String> dateEntryClearanceDecision = asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION);
                return parse(dateEntryClearanceDecision
                    .orElseThrow(() -> new RequiredFieldMissingException("dateEntryClearanceDecision is not present")));

            default:
                throw new RequiredFieldMissingException("decisionLetterReceivedDate is not present");
        }
    }

    private LocalDate handleInCountryAppeal(AsylumCase asylumCase, AppealType appealType) {
        if (appealType.equals(AppealType.AG)) {
            Optional<String> decisionLetterDate = asylumCase.read(DATE_ON_DECISION_LETTER, String.class);
            return parse(decisionLetterDate.orElseThrow(() -> new RequiredFieldMissingException("dateOnDecisionLetter is not present")));
        } else {
            Optional<String> homeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE);
            return parse(homeOfficeDecisionDate
                .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is not present")));
        }
    }

    private LocalDate handleOutOfCountryInternalAppeal(AsylumCase asylumCase, OutOfCountryCircumstances outOfCountryCircumstances) {
        switch (outOfCountryCircumstances) {
            case ENTRY_CLEARANCE_DECISION:
                Optional<String> dateEntryClearanceDecision = asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION);
                return parse(dateEntryClearanceDecision
                    .orElseThrow(() -> new RequiredFieldMissingException("dateEntryClearanceDecision is not present")));

            case LEAVE_UK:
            case NONE:
                Optional<String> homeOfficeDecisionLetterDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);
                return parse(homeOfficeDecisionLetterDate
                    .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));

            default:
                throw new RequiredFieldMissingException("decisionLetterReceivedDate is not present");
        }
    }

    private boolean isDecisionDateBeforeAppealOutOfTimeDate(LocalDate decisionDate, boolean isOutOfCountry, boolean isOutOfCountryCircumstances) {
        return decisionDate.isBefore(dateProvider.now().minusDays(isOutOfCountry || isOutOfCountryCircumstances ? appealOutOfTimeDaysOoc : appealOutOfTimeDaysUk));
    }
}
