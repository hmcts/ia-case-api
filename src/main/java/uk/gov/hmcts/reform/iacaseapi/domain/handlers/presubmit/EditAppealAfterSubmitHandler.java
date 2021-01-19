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
    private final int appealOutOfTimeDays;

    public EditAppealAfterSubmitHandler(
        DateProvider dateProvider,
        @Value("${appealOutOfTimeDays}") int appealOutOfTimeDays
    ) {
        this.dateProvider = dateProvider;
        this.appealOutOfTimeDays = appealOutOfTimeDays;
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


        Optional<String> maybeHomeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE);

        LocalDate homeOfficeDecisionDate = null;

        if (!asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class).map(
            value -> OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS.equals(value)).orElse(false)) {

            homeOfficeDecisionDate =
                parse(maybeHomeOfficeDecisionDate
                    .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is missing")));
        }

        if (homeOfficeDecisionDate != null && homeOfficeDecisionDate.isBefore(dateProvider.now().minusDays(appealOutOfTimeDays))) {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, YES);
        } else {
            asylumCase.write(SUBMISSION_OUT_OF_TIME, NO);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
            asylumCase.clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
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
