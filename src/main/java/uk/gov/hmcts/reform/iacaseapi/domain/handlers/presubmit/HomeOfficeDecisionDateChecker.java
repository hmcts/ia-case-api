package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
               && Arrays.asList(
                Event.SUBMIT_APPEAL)
                   .contains(callback.getEvent());
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

        LocalDate homeOfficeDecisionDate = null;
        LocalDate decisionLetterDate = null;

        Optional<String> maybeHomeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE);
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Appeal type is missing"));

        //change this to be ADA appeal
        if (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(NO) == NO) {

            Optional<OutOfCountryDecisionType> maybeOutOfCountryDecisionType = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);

            if (maybeOutOfCountryDecisionType.isPresent()) {

                OutOfCountryDecisionType outOfCountryDecisionType = maybeOutOfCountryDecisionType.get();

                if (outOfCountryDecisionType == OutOfCountryDecisionType.REMOVAL_OF_CLIENT) {
                    Optional<String> maybeHomeOfficeDecisionLetterDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);

                    homeOfficeDecisionDate =
                        parse(maybeHomeOfficeDecisionLetterDate
                            .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));
                } else if (outOfCountryDecisionType == OutOfCountryDecisionType.REFUSAL_OF_PROTECTION) {
                    Optional<String> maybeDateClientLeaveUk = asylumCase.read(DATE_CLIENT_LEAVE_UK);

                    homeOfficeDecisionDate =
                        parse(maybeDateClientLeaveUk
                            .orElseThrow(() -> new RequiredFieldMissingException("dateClientLeaveUk is not present")));
                } else if (outOfCountryDecisionType == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS) {
                    Optional<String> maybeDateEntryClearanceDecision = asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION);

                    homeOfficeDecisionDate =
                        parse(maybeDateEntryClearanceDecision
                            .orElseThrow(() -> new RequiredFieldMissingException("dateEntryClearanceDecision is not present")));
                }
            } else if (appealType.equals(AppealType.AG)) {
                Optional<String> mayBeDecisionLatter = asylumCase.read(DATE_ON_DECISION_LETTER, String.class);
                decisionLetterDate = parse(mayBeDecisionLatter.orElseThrow(() -> new RequiredFieldMissingException("dateOnDecisionLetter is not present")));
            } else {
                homeOfficeDecisionDate = parse(maybeHomeOfficeDecisionDate
                    .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is not present")));
            }
            
            if (!HandlerUtils.isAipJourney(asylumCase)) {
                if (homeOfficeDecisionDate != null
                    && homeOfficeDecisionDate.isBefore(dateProvider.now().minusDays(maybeOutOfCountryDecisionType.isPresent() ? appealOutOfTimeDaysOoc : appealOutOfTimeDaysUk))) {
                    asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
                    asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, NO);
                } else if (decisionLetterDate != null && decisionLetterDate.isBefore(dateProvider.now().minusDays(maybeOutOfCountryDecisionType.isPresent() ? appealOutOfTimeDaysOoc : appealOutOfTimeDaysUk))) {
                    asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
                    asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, NO);
                } else {
                    asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
                }
            }
        } else {
            Optional<String> maybeHomeOfficeDecisionLetterDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);
            homeOfficeDecisionDate =
                parse(maybeHomeOfficeDecisionLetterDate
                    .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));
                    
            ZonedDateTime dueDateTime = dueDateService.calculateDueDate(homeOfficeDecisionDate.atStartOfDay(ZoneOffset.UTC), appealOutOfTimeAcceleratedDetainedWorkingDays);

            if (dueDateTime != null && dueDateTime.toLocalDate().isBefore(dateProvider.now())) {
                asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
                asylumCase.write(RECORDED_OUT_OF_TIME_DECISION, NO);
            } else {
                asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            }
        }



        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
