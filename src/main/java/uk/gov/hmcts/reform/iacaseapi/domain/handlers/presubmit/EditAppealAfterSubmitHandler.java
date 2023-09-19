package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EditAppealAfterSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final int appealOutOfTimeDaysUk;
    private final int appealOutOfTimeDaysOoc;

    public EditAppealAfterSubmitHandler(
        DateProvider dateProvider,
        @Value("${appealOutOfTimeDaysUk}") int appealOutOfTimeDaysUk,
        @Value("${appealOutOfTimeDaysOoc}") int appealOutOfTimeDaysOoc
    ) {
        this.dateProvider = dateProvider;
        this.appealOutOfTimeDaysUk = appealOutOfTimeDaysUk;
        this.appealOutOfTimeDaysOoc = appealOutOfTimeDaysOoc;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT;
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

        LocalDate decisionDate = null;
        Optional<String> maybeHomeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE);

        Optional<OutOfCountryDecisionType> maybeOutOfCountryDecisionType = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);

        if (maybeOutOfCountryDecisionType.isPresent()) {
            OutOfCountryDecisionType decisionType = maybeOutOfCountryDecisionType.get();

            if (decisionType == OutOfCountryDecisionType.REMOVAL_OF_CLIENT) {
                Optional<String> maybeHomeOfficeDecisionLetterDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE);

                decisionDate =
                    parse(maybeHomeOfficeDecisionLetterDate
                        .orElseThrow(() -> new RequiredFieldMissingException("decisionLetterReceivedDate is not present")));
            } else if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_PROTECTION) {
                Optional<String> maybeDateClientLeaveUk = asylumCase.read(DATE_CLIENT_LEAVE_UK);

                decisionDate =
                    parse(maybeDateClientLeaveUk
                        .orElseThrow(() -> new RequiredFieldMissingException("dateClientLeaveUk is not present")));
            } else if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS) {
                Optional<String> maybeDateEntryClearanceDecision = asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION);

                decisionDate =
                    parse(maybeDateEntryClearanceDecision
                        .orElseThrow(() -> new RequiredFieldMissingException("dateEntryClearanceDecision is not present")));
            }

            if (decisionType == OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS || decisionType == OutOfCountryDecisionType.REFUSE_PERMIT) {
                asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER).orElse(asylumCase.read(GWF_REFERENCE_NUMBER).orElse(null)));
            }
        } else {
            decisionDate =
                parse(maybeHomeOfficeDecisionDate
                    .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is missing")));
        }


        if (decisionDate != null
            && decisionDate.isBefore(dateProvider.now().minusDays(maybeOutOfCountryDecisionType.isPresent() ? appealOutOfTimeDaysOoc : appealOutOfTimeDaysUk))) {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
        } else {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
            asylumCase.clear(RECORDED_OUT_OF_TIME_DECISION);
        }


        if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
            changeEditAppealApplicationsToCompleted(asylumCase);
            asylumCase.clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
            State maybePreviousState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
                .orElse(State.UNKNOWN);
            asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, maybePreviousState);
            clearNewMatters(asylumCase);
        }


        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearNewMatters(AsylumCase asylumCase) {
        YesOrNo hasNewMatters = asylumCase.read(HAS_NEW_MATTERS, YesOrNo.class).orElse(NO);
        if (NO.equals(hasNewMatters)) {
            asylumCase.clear(NEW_MATTERS);
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
}
