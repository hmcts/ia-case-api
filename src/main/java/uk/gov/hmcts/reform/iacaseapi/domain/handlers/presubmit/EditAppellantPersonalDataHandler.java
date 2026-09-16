package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

@Component
public class EditAppellantPersonalDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.EDIT_APPELLANT_PERSONAL_DATA;
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

        Optional<OutOfCountryDecisionType> outOfCountryDecisionTypeOptional =
            asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        YesOrNo appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).orElse(NO);
        boolean isRefusalOfHuOrPermit = outOfCountryDecisionTypeOptional
            .map(outOfCountryDecisionType ->
                Set.of(
                    OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS,
                    OutOfCountryDecisionType.REFUSE_PERMIT
                ).contains(outOfCountryDecisionType))
            .orElse(false);
        boolean isInternalOocCase = isInternalCase(asylumCase) && appellantInUk.equals(NO);
        if (isRefusalOfHuOrPermit || isInternalOocCase) {
            asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER)
                .orElse(asylumCase.read(GWF_REFERENCE_NUMBER).orElse(null)));
        }

        changeEditAppealApplicationsToCompleted(asylumCase);
        asylumCase.clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        State maybePreviousState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
            .orElse(State.UNKNOWN);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, maybePreviousState);

        return new PreSubmitCallbackResponse<>(asylumCase);
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
